package de.tum.ase.kleo.app.group;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.dto.SessionDTO;
import de.tum.ase.kleo.app.support.ui.ArrayAdapterItem;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class GroupDetailsSessionListNewRecordViewHolder {

    private static final String DATE_TIME_PICKER_TAG = "date_time_picker";

    private static final @LayoutRes int layout
            = R.layout.fragment_group_details_session_list_new_record;

    private final View view;
    private final FragmentManager fragmentManager;
    private final Context context;

    private final EditText dateInput;
    private final EditText timeBeginsInput;
    private final EditText timeEndsInput;
    private final Spinner typeChooser;
    private final EditText locationInput;

    private GroupDetailsSessionListNewRecordViewHolder(Context context,
                                                       LayoutInflater inflater,
                                                       FragmentManager fragmentManager) {
        this.view = inflater.inflate(layout, null);
        this.context = context;
        this.fragmentManager = fragmentManager;

        dateInput = view.findViewById(R.id.group_details_session_list_new_record_date_input);
        timeBeginsInput = view.findViewById(R.id.group_details_session_list_new_record_time_begins_input);
        timeEndsInput = view.findViewById(R.id.group_details_session_list_new_record_time_ends_input);
        typeChooser = view.findViewById(R.id.group_details_session_list_new_record_session_type_chooser);
        locationInput = view.findViewById(R.id.group_details_session_list_new_record_location_input);

        setupDataTimePickers();
        setupTypeChooser();
    }

    public static GroupDetailsSessionListNewRecordViewHolder within(Fragment baseFragment) {
        return new GroupDetailsSessionListNewRecordViewHolder(baseFragment.getContext(),
                baseFragment.getActivity().getLayoutInflater(), baseFragment.getFragmentManager());
    }

    public Optional<OffsetDateTime> getSessionBeginsTime() {
        final String rawDate = dateInput.getText().toString();
        final String rawTimeBegins = timeBeginsInput.getText().toString();

        if (isBlank(rawDate) || isBlank(rawTimeBegins)) {
            return Optional.empty();
        }

        final LocalDate date = LocalDate.parse(rawDate);
        final LocalTime time = LocalTime.parse(rawTimeBegins);
        return Optional.of(OffsetDateTime.of(date, time, OffsetDateTime.now().getOffset()));
    }

    public Optional<OffsetDateTime> getSessionEndsTime() {
        final String rawDate = dateInput.getText().toString();
        final String rawTimeEnds = timeEndsInput.getText().toString();

        if (isBlank(rawDate) || isBlank(rawTimeEnds)) {
            return Optional.empty();
        }

        final LocalDate date = LocalDate.parse(rawDate);
        final LocalTime time = LocalTime.parse(rawTimeEnds);
        return Optional.of(OffsetDateTime.of(date, time, OffsetDateTime.now().getOffset()));
    }

    public Optional<String> getSessionLocation() {
        final String location = locationInput.getText().toString();

        if (isBlank(location)) {
            return Optional.empty();
        } else {
            return Optional.of(location);
        }
    }

    public Optional<SessionDTO.TypeEnum> getSessionType() {
        return ArrayAdapterItem.getSelectedItemValue(typeChooser, SessionDTO.TypeEnum.class);
    }

    private void setupDataTimePickers() {
        dateInput.setOnClickListener(l -> {
            final DatePickerDialog datePickerDialog =
                    DatePickerDialog.newInstance((v, year, monthOfYear, dayOfMonth)
                            -> dateInput.setText(LocalDate.of(year, monthOfYear + 1,
                            dayOfMonth).toString()));

            datePickerDialog.show(fragmentManager, DATE_TIME_PICKER_TAG);
        });

        timeBeginsInput.setOnClickListener(l -> {
            final TimePickerDialog timePickerDialog =
                    TimePickerDialog.newInstance((v, hourOfDay, minute, second)
                                    -> timeBeginsInput.setText(LocalTime.of(hourOfDay, minute, second).toString()),
                            true);

            timePickerDialog.show(fragmentManager, DATE_TIME_PICKER_TAG);
        });

        timeEndsInput.setOnClickListener(l -> {
            final TimePickerDialog timePickerDialog =
                    TimePickerDialog.newInstance((v, hourOfDay, minute, second)
                            -> timeEndsInput.setText(LocalTime.of(hourOfDay, minute,
                            second).toString()),true);

            timePickerDialog.show(fragmentManager, DATE_TIME_PICKER_TAG);
        });
    }

    private void setupTypeChooser() {
        final List<ArrayAdapterItem<SessionDTO.TypeEnum>> sessionTypeChooserItems =
                stream(SessionDTO.TypeEnum.values())
                        .map(type -> ArrayAdapterItem.of(type.toString(), type)).collect(toList());
        final ArrayAdapter<ArrayAdapterItem<SessionDTO.TypeEnum>> sessionTypeChooserAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                        sessionTypeChooserItems);
        sessionTypeChooserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        typeChooser.setAdapter(sessionTypeChooserAdapter);
    }

    public View getView() {
        return view;
    }
}
