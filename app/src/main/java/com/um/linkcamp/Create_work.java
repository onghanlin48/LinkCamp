package com.um.linkcamp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import data.DatabaseHelper;
import data.Work;
import function.VerifyLogin;

public class Create_work extends AppCompatActivity {
    private static final List<String> Type = new ArrayList<>();
    String type = null,title = null,job = null,location = null;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_work);

        dbHelper = new DatabaseHelper(Create_work.this);
        VerifyLogin verifyLogin = new VerifyLogin(Create_work.this);
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.auto_complete_type);

        db.collection("Job_type")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Type.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String type = document.getString("type");
                        if (type != null) {
                            Type.add(type);
                        }
                    }
                });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Type);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) ->{
            type = adapter.getItem(position);
        });

        TextInputEditText txt_title = findViewById(R.id.text_input_title);
        TextInputEditText txt_job = findViewById(R.id.text_input_job_title);
        TextInputEditText txt_location = findViewById(R.id.text_input_location);


        Work work = Work.getInstance();
        if(work.getTitle() != null){
            txt_title.setText(work.getTitle());
            type = work.getType();
            autoCompleteTextView.setText(work.getType(),false);
            txt_job.setText(work.getJod_title());
            txt_location.setText(work.getLocation());
        }

        TextView back = findViewById(R.id.back);
        Button next = findViewById(R.id.next);

        back.setOnClickListener(v -> {
            finish();
        });
        next.setOnClickListener(v -> {
            job = Objects.requireNonNull(txt_job.getText()).toString().trim();
            title = Objects.requireNonNull(txt_title.getText()).toString().trim();
            location = Objects.requireNonNull(txt_location.getText()).toString().trim();

            if(checkEmpty()){
                work.setLocation(location);
                work.setTitle(title);
                work.setType(type);
                work.setJod_title(job);

                Intent intent = new Intent(Create_work.this,create_work_salary.class);
                startActivity(intent);
                finish();
            }
        });
    }
    public boolean checkEmpty(){
        boolean check = true;

        TextInputLayout l_title = findViewById(R.id.text_input_layout_title);
        TextInputLayout l_job = findViewById(R.id.text_input_layout_job_title);
        TextInputLayout l_location = findViewById(R.id.text_input_layout_location);
        TextInputLayout l_type = findViewById(R.id.text_input_layout_type);

        if(title == null){
            check = false;
            l_title.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_title.setHelperText("Please fill in Title");
        }else if(title.isEmpty()){
            check = false;
            l_title.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_title.setHelperText("Please fill in Title");
        }else{
            l_title.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_title.setHelperText("");
        }

        if(job == null){
            check = false;
            l_job.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_job.setHelperText("Front-End Development\nPlease fill in Job Title");
        }else if(job.isEmpty()){
            check = false;
            l_job.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_job.setHelperText("Front-End Development\nPlease fill in Job Title");
        }else{
            l_job.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_job.setHelperText("Front-End Development");
        }

        if(location == null){
            check = false;
            l_location.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_location.setHelperText("Office LinkCamp UM Kuala Lumpur / Remote\nPlease fill in Location");
        }else if(location.isEmpty()){
            check = false;
            l_location.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_location.setHelperText("Office LinkCamp UM Kuala Lumpur / Remote\nPlease fill in Location");
        }else{
            l_location.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_location.setHelperText("Office LinkCamp UM Kuala Lumpur / Remote");
        }

        if(type == null){
            check = false;
            l_type.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_type.setHelperText("Please select Job Type");
        }else if(type.isEmpty()){
            check = false;
            l_type.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_type.setHelperText("Please select Job Type");
        }else{
            l_type.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_type.setHelperText("");
        }

        return check;
    }
}