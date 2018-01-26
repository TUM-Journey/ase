package de.tum.ase.kleo.app.group.details.session;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;
import de.tum.ase.kleo.app.support.ui.ResourceListLayoutFragment;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.DateTimeFormatters.simpleTime;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class GroupDetailsSessionListFragment extends ResourceListLayoutFragment<SessionDTO> {

    public static final String ARG_BUNDLE_GROUP = "group_details";

    private GroupDTO group;

    public GroupDetailsSessionListFragment() {
        super(R.layout.fragment_group_details_session_list,
                R.id.group_details_session_list_view,
                R.layout.fragment_group_details_session_list_item,
                R.id.group_details_session_list_progressbar,
                R.id.group_details_session_list_no_records);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Serializable rawGroup = getArguments().getSerializable(ARG_BUNDLE_GROUP);

        if (rawGroup == null) {
            throw new IllegalStateException("GroupDetailsSessionListFragment requires group arg");
        } else if (!GroupDTO.class.equals(rawGroup.getClass())) {
            throw new IllegalStateException("GroupDetailsSessionListFragment 'group' arg is not " +
                    "of GroupDTO type");
        }

        group = (GroupDTO) rawGroup;
        backendClient = ((KleoApplication) getActivity().getApplication()).backendClient();
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        final FloatingActionButton createSessionFab
                = view.findViewById(R.id.group_details_session_list_new_record_btn);

        if (!backendClient.principal().isTutor()) {
            createSessionFab.setVisibility(View.INVISIBLE);
        } else {
            createSessionFab.setOnClickListener(l ->
                    askForNewSession()
                            .subscribe(newSession ->
                                    createNewSession(group.getId(), newSession)
                                            .subscribe(this::appendResource,
                                                    this::showErrorMessage)));
        }
    }

    @Override
    protected Observable<List<SessionDTO>> fetchResources() {
        return Observable.just(defaultIfNull(group.getSessions(), emptyList()));
    }

    @Override
    protected void populateListItem(View view, SessionDTO session) {
        final TextView sessionTime =
                view.findViewById(R.id.group_details_session_list_item_interval_txt);
        final TextView sessionLocation =
                view.findViewById(R.id.group_details_session_list_item_location_txt);
        final TextView sessionType =
                view.findViewById(R.id.group_details_session_list_item_type_txt);

        final ImageButton sessionRemoveBtn =
                view.findViewById(R.id.group_details_session_list_item_remove_img_btn);
        final ImageButton sessionChangeLocation =
                view.findViewById(R.id.group_details_session_list_item_change_location_img_btn);
        final ImageButton sessionChangeType =
                view.findViewById(R.id.group_details_session_list_item_change_type_img_btn);

        sessionTime.setText(format("%s - %s",
                simpleTime(session.getBegins()),
                simpleTime(session.getEnds())));
        sessionLocation.setText(session.getLocation());
        sessionType.setText(session.getType().toString());

        if (!backendClient.principal().isTutor()) {
            sessionRemoveBtn.setVisibility(View.INVISIBLE);
            sessionChangeLocation.setVisibility(View.INVISIBLE);
            sessionChangeType.setVisibility(View.INVISIBLE);
        } else {
            sessionRemoveBtn.setOnClickListener(v -> {
                removeResourceIf(resource -> resource.getId().equals(session.getId()));
                removeSession(session);
            });
            sessionChangeLocation.setOnClickListener(v -> {
                askForSessionLocationUpdate(session.getLocation())
                        .subscribe(newLocation -> {
                            updateSessionLocation(session, newLocation);
                            sessionLocation.setText(newLocation);
                        });
            });
            sessionChangeType.setOnClickListener(v -> {
                askForSessionTypeUpdate(session.getType())
                        .subscribe(newType -> {
                            updateSessionType(session, newType);
                            sessionType.setText(newType.toString());
                        });
            });
        }
    }

    private Maybe<String> askForSessionLocationUpdate(String oldLocation) {
        return Maybe.create(emitter -> {
            final EditText alertLocationInput = new EditText(getContext());
            alertLocationInput.setText(oldLocation);

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.user_list_item_roles_change_popup_title)
                    .setView(alertLocationInput)
                    .setPositiveButton(R.string.save, (dialog, which) -> {
                        final String newLocation = alertLocationInput.getText().toString();

                        if (oldLocation.equals(newLocation)) {
                            emitter.onComplete();
                        } else {
                            emitter.onSuccess(newLocation);
                        }

                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .setOnDismissListener(dialog -> emitter.onComplete())
                    .show();
        });
    }

    private Maybe<SessionDTO.TypeEnum> askForSessionTypeUpdate(SessionDTO.TypeEnum oldSessionType) {
        return Maybe.create(emitter -> {
            final String[] sessionTypesNames
                    = stream(SessionDTO.TypeEnum.values())
                        .map(SessionDTO.TypeEnum::getValue).toArray(String[]::new);

            final int currentSessionTypeChecked
                    = asList(sessionTypesNames).indexOf(oldSessionType.getValue());

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.group_details_session_list_change_type_popup_title)
                    .setSingleChoiceItems(sessionTypesNames, currentSessionTypeChecked,
                            (dialog, which) -> {
                                final SessionDTO.TypeEnum newSessionType
                                        = SessionDTO.TypeEnum.fromValue(sessionTypesNames[which]);

                                if (oldSessionType == newSessionType) {
                                    emitter.onComplete();
                                } else {
                                    emitter.onSuccess(newSessionType);
                                }

                                dialog.dismiss();
                    })
                    .setCancelable(true)
                    .setOnDismissListener(dialog -> emitter.onComplete())
                    .show();
        });
    }

    private Maybe<SessionDTO> askForNewSession() {
        return Maybe.create(emitter -> {
            final GroupDetailsSessionListNewRecordViewHolder viewHolder
                    = GroupDetailsSessionListNewRecordViewHolder.within(this);

            final AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.group_details_session_list_new_record_popup_title)
                    .setView(viewHolder.getView())
                    .setPositiveButton(R.string.create, (d, which) -> {})
                    .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
                    .setCancelable(false)
                    .setOnDismissListener(d -> emitter.onComplete())
                    .create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(l -> {
                final Optional<OffsetDateTime> beginsTimeOpt
                        = viewHolder.getSessionBeginsTime();
                final Optional<OffsetDateTime> endsTimeOpt
                        = viewHolder.getSessionEndsTime();
                final Optional<String> sessionLocationOpt
                        = viewHolder.getSessionLocation();
                final Optional<SessionDTO.TypeEnum> sessionTypeOpt
                        = viewHolder.getSessionType();

                if (!beginsTimeOpt.isPresent()) {
                    Toast.makeText(getContext(),
                            R.string.group_details_session_list_new_record_warn_start_time_blank,
                            Toast.LENGTH_SHORT).show();
                } else if (!endsTimeOpt.isPresent()) {
                    Toast.makeText(getContext(),
                            R.string.group_details_session_list_new_record_warn_ends_time_blank,
                            Toast.LENGTH_SHORT).show();
                } else if (!sessionLocationOpt.isPresent()) {
                    Toast.makeText(getContext(), R.string.group_details_session_list_new_record_warn_location_blank,
                            Toast.LENGTH_SHORT).show();
                } else if (!sessionTypeOpt.isPresent()) {
                    Toast.makeText(getContext(), R.string.group_details_session_list_new_record_warn_bad_session_type_chosen,
                            Toast.LENGTH_SHORT).show();
                } else {

                    final SessionDTO newSession = new SessionDTO()
                            .begins(beginsTimeOpt.get())
                            .ends(endsTimeOpt.get())
                            .location(sessionLocationOpt.get())
                            .type(sessionTypeOpt.get());

                    createNewSession(group.getId(), newSession)
                            .subscribe(this::appendResource);

                    dialog.dismiss();
                }
            });
        });
    }


    private Observable<SessionDTO> createNewSession(String groupId, SessionDTO newSession) {
        return backendClient.as(GroupsApi.class)
                .addGroupSession(groupId, newSession)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar);
    }

    private void removeSession(SessionDTO session) {
        group.getSessions().removeIf(s -> s.getId().equals(session.getId()));
        backendClient.as(GroupsApi.class)
                .deleteGroupSession(group.getId(), session.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar)
                .subscribe(() -> {}, this::showErrorMessage);
    }

    private void updateSessionLocation(SessionDTO session, String newLocation) {
        group.getSessions().stream()
                .filter(s -> s.getId().equals(session.getId()))
                .findAny().ifPresent(sessionToUpdate -> {
                    sessionToUpdate.setLocation(newLocation);
                    syncGroupWithBackend();
        });
    }

    private void updateSessionType(SessionDTO session, SessionDTO.TypeEnum newSessionType) {
        group.getSessions().stream()
                .filter(s -> s.getId().equals(session.getId()))
                .findAny().ifPresent(sessionToUpdate -> {
                    sessionToUpdate.setType(newSessionType);
                    syncGroupWithBackend();
        });
    }

    private void syncGroupWithBackend() {
        backendClient.as(GroupsApi.class)
                .updateGroup(group.getId(), group)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar)
                .subscribe((g) -> {}, this::showErrorMessage);
    }
}
