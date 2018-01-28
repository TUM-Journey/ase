package de.tum.ase.kleo.app.group.details.session;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.dto.SessionDTO;
import de.tum.ase.kleo.app.support.ResourceListLayoutFragment;
import io.reactivex.Completable;
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

    public static final String ARG_BUNDLE_GROUP_ID = "group_details_sessions_id";

    private String groupId;

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

        groupId = getArguments().getString(ARG_BUNDLE_GROUP_ID);
        if (groupId == null) {
            throw new IllegalStateException("GroupDetailsSessionListFragment requires group id arg");
        }

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
                                    createNewSession(newSession)
                                            .subscribe(this::appendResource,
                                                    this::showErrorMessage)));
        }
    }

    @Override
    protected Observable<List<SessionDTO>> fetchResources() {
        return backendClient.as(GroupsApi.class).getGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(group -> defaultIfNull(group.getSessions(), emptyList()));
    }

    @Override
    protected void populateListItem(View view, SessionDTO session, int position) {
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
                removeSession(session.getId())
                        .subscribe(() ->
                                removeResourceIf(resource
                                        -> resource.getId().equals(session.getId())),
                                this::showErrorMessage);
            });
            sessionChangeLocation.setOnClickListener(v -> {
                askForSessionLocationUpdate(session.getLocation())
                        .subscribe(newLocation -> {
                            updateSessionLocation(session.getId(), newLocation)
                                    .subscribe((updatedSession) -> {
                                        updateResource(position, updatedSession);
                                    }, this::showErrorMessage);
                        });
            });
            sessionChangeType.setOnClickListener(v -> {
                askForSessionTypeUpdate(session.getType())
                        .subscribe(newType -> {
                            updateSessionType(session.getId(), newType)
                                    .subscribe((updatedSession) -> {
                                        updateResource(position, updatedSession);
                                    }, this::showErrorMessage);
                        });
            });
        }
    }

    private Maybe<String> askForSessionLocationUpdate(String oldLocation) {
        return Maybe.create(emitter -> {
            final EditText alertLocationInput = new EditText(getContext());
            alertLocationInput.setText(oldLocation);

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.group_details_session_list_change_location_popup_title)
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
                    .setCancelable(true)
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

                    createNewSession(newSession).subscribe(this::appendResource);

                    dialog.dismiss();
                }
            });
        });
    }


    private Observable<SessionDTO> createNewSession(SessionDTO newSession) {
        return backendClient.as(GroupsApi.class)
                .addGroupSession(groupId, newSession)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar);
    }

    private Completable removeSession(String sessionId) {
        return backendClient.as(GroupsApi.class)
                .deleteGroupSession(groupId, sessionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar);
    }

    private Observable<SessionDTO> updateSessionLocation(String sessionId, String newLocation) {
        return backendClient.as(GroupsApi.class)
                .updateGroupSession(groupId, sessionId, new SessionDTO().location(newLocation))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar);
    }

    private Observable<SessionDTO> updateSessionType(String sessionId, SessionDTO.TypeEnum newSessionType) {
        return backendClient.as(GroupsApi.class)
                .updateGroupSession(groupId, sessionId, new SessionDTO().type(newSessionType))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar);
    }
}
