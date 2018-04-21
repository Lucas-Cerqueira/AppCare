package com.lf.appcare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatePickerFragment extends DialogFragment
{

//    String dates[];
//    NumberPicker datePicker, hourPicker, minutePicker;
//    Button doneButton, currentTimeButton;
//    Calendar rightNow = Calendar.getInstance();
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        dates = getDatesFromCalender();
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//    {
//        Dialog getDialog = new Dialog(getActivity());
//        View view = inflater.inflate(R.layout.custom, container);
//        //datePicker = (NumberPicker) view.findViewById(R.id.datePicker);
//        hourPicker = (NumberPicker) view.findViewById(R.id.hourPicker);
//        minutePicker = (NumberPicker) view.findViewById(R.id.minutePicker);
//        //doneButton = (Button) view.findViewById(R.id.doneButton);
//        //currentTimeButton = (Button) view.findViewById(R.id.currentTimeButton);
//        //datePicker.setMinValue(0);
//        //datePicker.setMaxValue(dates.length - 1);
////        datePicker.setFormatter(new NumberPicker.Formatter()
////        {
////            @Override
////            public String format(int value) {
////                return dates[value];
////            }
////        });
//
//        //datePicker.setDisplayedValues(dates);
//        hourPicker.setMinValue(0);
//        hourPicker.setMaxValue(23);
//        hourPicker.setValue(rightNow.get(Calendar.HOUR_OF_DAY));
//        minutePicker.setMinValue(0);
//        minutePicker.setMaxValue(59);
//        minutePicker.setValue(rightNow.get(Calendar.MINUTE));
//        //doneButton.setOnClickListener(this);
//        //currentTimeButton.setOnClickListener(this);
//        getDialog.setTitle("choose_time");
//        return view;
//    }


}
