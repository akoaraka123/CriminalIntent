package com.example.criminalintent;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {

    public static final String REQUEST_KEY_DATE = "requestKeyDate";
    public static final String BUNDLE_KEY_DATE = "bundleKeyDate";

    private static final String ARG_DATE = "argDate";

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Date date = new Date();
        Bundle args = getArguments();
        if (args != null) {
            Object serializable = args.getSerializable(ARG_DATE);
            if (serializable instanceof Date) {
                date = (Date) serializable;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar resultCalendar = Calendar.getInstance();
                resultCalendar.set(Calendar.YEAR, year);
                resultCalendar.set(Calendar.MONTH, month);
                resultCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                resultCalendar.set(Calendar.HOUR_OF_DAY, 0);
                resultCalendar.set(Calendar.MINUTE, 0);
                resultCalendar.set(Calendar.SECOND, 0);
                resultCalendar.set(Calendar.MILLISECOND, 0);

                Bundle result = new Bundle();
                result.putSerializable(BUNDLE_KEY_DATE, resultCalendar.getTime());
                getParentFragmentManager().setFragmentResult(REQUEST_KEY_DATE, result);
            }
        };

        return new DatePickerDialog(requireContext(), listener, year, month, day);
    }
}
