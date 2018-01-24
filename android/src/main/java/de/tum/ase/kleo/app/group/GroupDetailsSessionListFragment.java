package de.tum.ase.kleo.app.group;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;
import de.tum.ase.kleo.app.support.ui.ResourceListLayoutFragment;
import io.reactivex.Observable;

import static de.tum.ase.kleo.app.support.DateTimeFormatters.simpleTime;
import static java.lang.String.format;

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
    protected void onCreateLayout(View view, Bundle savedInstanceState) {
        final Serializable rawGroup = savedInstanceState.getSerializable(ARG_BUNDLE_GROUP);

        if (rawGroup == null) {
            throw new IllegalStateException("GroupDetailsSessionListFragment requires group arg");
        } else if (!GroupDTO.class.equals(rawGroup.getClass())) {
            throw new IllegalStateException("GroupDetailsSessionListFragment 'group' arg is not " +
                    "of GroupDTO type");
        }

        group = (GroupDTO) rawGroup;
    }

    @Override
    protected Observable<List<SessionDTO>> fetchResources() {
        return Observable.just(group.getSessions());
    }

    @Override
    protected void populateListItem(View view, SessionDTO session) {
        final TextView sessionDescription =
                view.findViewById(R.id.group_details_session_list_item_interval_txt);
        final ImageButton sessionRemoveBtn =
                view.findViewById(R.id.group_details_session_list_item_remove_img_btn);

        sessionDescription.setText(format("%s - %s",
                simpleTime(session.getBegins()),
                simpleTime(session.getEnds())));

        sessionRemoveBtn.setOnClickListener(v -> {

        });
    }
}
