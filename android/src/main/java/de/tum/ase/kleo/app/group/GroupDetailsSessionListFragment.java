package de.tum.ase.kleo.app.group;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

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
                    .setTitle(R.string.user_list_item_roles_change_popup_title)
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
                    .setCancelable(false)
                    .setOnDismissListener(dialog -> emitter.onComplete())
                    .show();
        });
    }

    private void removeSession(SessionDTO session) {
        group.getSessions().removeIf(s -> s.getId().equals(session.getId()));
        backendClient.as(GroupsApi.class)
                .deleteGroupSession(group.getId(), session.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(r -> showProgressBar())
                .doOnComplete(this::hideProgressBar)
                .doOnError(this::showErrorMessage)
                .subscribe();
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
                .doOnError(this::showErrorMessage)
                .subscribe();
    }
}
