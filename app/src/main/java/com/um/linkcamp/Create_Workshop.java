package com.um.linkcamp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;


import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import data.DatabaseHelper;
import data.Workshop;
import function.VerifyLogin;

public class Create_Workshop extends AppCompatActivity {
    String title = null,location = null,date = null,stime = null,etime = null,close = null,today = null;
    String sdate = null , edate = null;
    int tutor = 0;
    Calendar calendar = Calendar.getInstance();
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_workshop);
        dbHelper = new DatabaseHelper(Create_Workshop.this);
        VerifyLogin verifyLogin = new VerifyLogin(Create_Workshop.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    finish();
                }else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    finish();
                }
            });
        }else{
            finish();
            return;
        }
        today();
        Workshop workshop = Workshop.getInstance();

        TextInputEditText edit_title = findViewById(R.id.text_input_title);
        TextInputEditText edit_location = findViewById(R.id.text_input_location);
        TextInputEditText edit_tutor = findViewById(R.id.text_input_tutor);
        TextInputEditText edit_date = findViewById(R.id.text_input_date);
        TextInputEditText edit_stime = findViewById(R.id.text_input_stime);
        TextInputEditText edit_etime = findViewById(R.id.text_input_etime);
        TextInputEditText edit_close = findViewById(R.id.text_input_close_date);

        if(workshop.getTitle() != null){
            String date_v = workshop.getDate();
            String[] dates = date_v.split(" - ");
            if (dates.length == 2) {
                sdate = dates[0].trim();
                edate = dates[1].trim();
            } else {
                sdate = date_v;
                edate = date_v;
            }
            edit_title.setText(workshop.getTitle());
            edit_location.setText(workshop.getLocation());
            edit_tutor.setText(String.valueOf(workshop.getTutor()));
            edit_date.setText(workshop.getDate());
            edit_stime.setText(workshop.getTime());
            edit_etime.setText(workshop.getEtime());
        }

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            workshop.clear();
            finish();
        });
        edit_date.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                openDate(edit_date);
            }
        });
        edit_close.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                openClose(edit_close);
            }
        });

        edit_stime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openStime(edit_stime);
            }
        });

        edit_etime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openStime(edit_etime);
            }
        });

        Button btnNext = findViewById(R.id.next);

        btnNext.setOnClickListener(v -> {
            title = Objects.requireNonNull(edit_title.getText()).toString();
            location = Objects.requireNonNull(edit_location.getText()).toString();
            if(!Objects.requireNonNull(edit_tutor.getText()).toString().isEmpty()){
                tutor  = Integer.parseInt(Objects.requireNonNull(edit_tutor.getText()).toString());
                System.out.println(tutor);
            }
            date = Objects.requireNonNull(edit_date.getText()).toString();
            stime = Objects.requireNonNull(edit_stime.getText()).toString();
            etime = Objects.requireNonNull(edit_etime.getText()).toString();
            close = Objects.requireNonNull(edit_close.getText()).toString();

            if(!check_empty()){
                workshop.setTitle(title);
                workshop.setLocation(location);
                workshop.setTutor(tutor);
                if(sdate.equals(edate)){
                    workshop.setDate(sdate);
                }else{
                    workshop.setDate(date);
                }
                workshop.setTime(stime);
                workshop.setEtime(etime);
                workshop.setClose(close);
                Intent intent = new Intent(Create_Workshop.this,Create_Workshop_Cover.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private boolean check_empty(){
        boolean check = false;

        TextInputLayout l_title = findViewById(R.id.text_input_layout_title);
        TextInputLayout l_location = findViewById(R.id.text_input_layout_location);
        TextInputLayout l_tutor = findViewById(R.id.text_input_layout_tutor);
        TextInputLayout l_date = findViewById(R.id.text_input_layout_date);
        TextInputLayout l_stime = findViewById(R.id.text_input_layout_stime);
        TextInputLayout l_etime = findViewById(R.id.text_input_layout_etime);
        TextInputLayout l_close = findViewById(R.id.text_input_layout_close_date);


        if(tutor == 0){
            check = true;
            l_tutor.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_tutor.setHelperText("Please fill in number for tutor");
        } else if (tutor <= 0) {
            check = true;
            l_tutor.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_tutor.setHelperText("Number for tutor must be more than 1");
        }else{
            l_tutor.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_tutor.setHelperText("Number Tutor in this workshop");
        }

        if(title.isEmpty()){
            check = true;
            l_title.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_title.setHelperText("Please fill in Title");
        }else{
            l_title.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_title.setHelperText(null);
        }

        if(location.isEmpty()){
            check = true;
            l_location.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_location.setHelperText("Online / Location (FSKTM UM)\nPlease fill in Location");
        }else{
            l_location.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_location.setHelperText("Online / Location (FSKTM UM)");
        }

        if(date.isEmpty()){
            check = true;
            l_date.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_date.setHelperText("Date of the Workshop\nPlease fill in Date");
        }else{
            l_date.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_date.setHelperText("Date of the Workshop");
        }

        if(stime.isEmpty()){
            check = true;
            l_stime.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_stime.setHelperText("Please fill in Start Time");
        }else{
            l_stime.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_stime.setHelperText(null);
        }

        if(etime.isEmpty()){
            check = true;
            l_etime.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_etime.setHelperText("Please fill in End Time");
        }else{
            l_etime.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_etime.setHelperText(null);
        }
        if (!etime.isEmpty() && !stime.isEmpty() && !sdate.isEmpty() && !edate.isEmpty()){
            DateTimeFormatter formatter = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("HH:mm");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalTime startTime = LocalTime.parse(stime, formatter);
                LocalTime endTime = LocalTime.parse(etime, formatter);

                if(sdate.equals(edate)){
                    if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                        check = true;
                        l_stime.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        l_stime.setHelperText("If Workshop same Start and End Date the End Time must be later then Start Time!");
                    } else {
                        l_stime.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                        l_stime.setHelperText(null);
                    }
                }
            }
        }
        if(close.isEmpty()){
            check = true;
            l_close.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_close.setHelperText("Please fill in CLose Date");
        }else{
            if(!sdate.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date parsedDate1 = dateFormat.parse(close);
                    Date parsedDate2 = dateFormat.parse(sdate);
                    System.out.println(close);
                    System.out.println(sdate);
                    if (parsedDate1.compareTo(parsedDate2) > 0) {
                        check = true;
                        l_close.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        l_close.setHelperText("Closing date after start date!!\nClosing date must be before start date");
                    } else if (parsedDate1.compareTo(parsedDate2) < 0) {
                        l_close.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                        l_close.setHelperText(null);
                    } else {
                        l_close.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                        l_close.setHelperText("Closing date same with Start date");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!sdate.isEmpty()){
            if(checkDate(l_date,sdate)){
                check = true;
            }
        }
        if(!close.isEmpty()){
            if(checkDate(l_close,close)){
                check = true;
            }
        }


        return check;
    }
    private void openDate(TextInputEditText date){
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range Workshop")
                .setSelection(null)
                .build();

        picker.show(Create_Workshop.this.getSupportFragmentManager(),"TAG");

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                Long startDate = selection.first;
                Long endDate = selection.second;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdate = startDate != null ? dateFormat.format(new Date(startDate)) : "";
                edate = endDate != null ? dateFormat.format(new Date(endDate)) : "";

                date.setText(sdate + " - " + edate);
            }
           date.clearFocus();
        });
        picker.addOnDismissListener(selection -> {
            date.clearFocus();
        });
    }

    private void openStime(TextInputEditText time) {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
             time.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            time.clearFocus();
         }, 15, 0, true);
        dialog.setOnDismissListener(dialogInterface -> {
           time.clearFocus();
        });
        dialog.show();
    }
    private void openClose(TextInputEditText date){
        DatePickerDialog dialog = new DatePickerDialog(Create_Workshop.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setText(String.valueOf(year)+ "-" + String.valueOf(month + 1) + "-" + String.valueOf(dayOfMonth));
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        dialog.setOnDismissListener(dialogInterface -> {
            date.clearFocus();
        });
        dialog.show();
    }
    private void today (){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("serverTime").setValue(ServerValue.TIMESTAMP);
        databaseReference.child("serverTime").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Long serverTimeMillis = task.getResult().getValue(Long.class);
                if (serverTimeMillis != null) {

                    Date serverDate = new Date(serverTimeMillis);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    today = sdf.format(serverDate);
                }
            } else {
                System.out.println("Failed to fetch server time.");
            }
        });
    }
    private Boolean checkDate(TextInputLayout l,String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date parsedDate1 = dateFormat.parse(today);
            Date parsedDate2 = dateFormat.parse(date);
            if (parsedDate1.compareTo(parsedDate2) > 0) {
                l.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                l.setHelperText("Date must be after Today date!");
                return true;
            } else if (parsedDate1.compareTo(parsedDate2) < 0) {
                l.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                l.setHelperText(null);
                return false;
            } else {
                l.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                l.setHelperText("Date must be after Today date!");
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}