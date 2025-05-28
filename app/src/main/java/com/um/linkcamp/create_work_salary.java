package com.um.linkcamp;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import data.DatabaseHelper;
import data.Work;
import function.DecimalDigitsInputFilter;
import function.VerifyLogin;

public class create_work_salary extends AppCompatActivity {
    private static final List<String> Pay = new ArrayList<>();
    String pay = null,min = null,max = null,des = null ,key =null,re =null,ic=null;
    private DatabaseHelper dbHelper;
    TextInputEditText txt_min,txt_max,txt_des,txt_key,txt_req;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_work_salary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(create_work_salary.this);
        VerifyLogin verifyLogin = new VerifyLogin(create_work_salary.this);
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
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.auto_complete_pay_cycle);

        db.collection("Pay")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Pay.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String type = document.getString("pay");
                        if (type != null) {
                            Pay.add(type);
                        }
                    }
                });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Pay);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) ->{
            pay = adapter.getItem(position);
        });

        txt_min = findViewById(R.id.text_input_minimum_salary);
        txt_max = findViewById(R.id.text_input_job_max_salary);
        txt_des = findViewById(R.id.text_input_description);
        txt_key = findViewById(R.id.text_input_responsibility);
        txt_req = findViewById(R.id.text_input_requirement);

        txt_min.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        txt_max.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});

        Work work = Work.getInstance();
        txt_min.setText(work.getMinimum());
        txt_max.setText(work.getMaximum());
        txt_des.setText(work.getDescription());
        txt_key.setText(work.getKey());
        txt_req.setText(work.getRequirement());
        pay =work.getPay();
        autoCompleteTextView.setText(work.getPay(),false);

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            work.clear();
            finish();
        });

        TextView pre = findViewById(R.id.btnPrevious);
        pre.setOnClickListener(v -> {
            getValue();
            work.setPay(pay);
            work.setMaximum(max);
            work.setMinimum(min);
            work.setKey(key);
            work.setDescription(des);
            work.setRequirement(re);
            Intent intent = new Intent(create_work_salary.this,Create_work.class);
            startActivity(intent);
            finish();
        });

        Button next = findViewById(R.id.next);
        next.setOnClickListener(v -> {
            getValue();
            if(checkEmpty()){
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                CollectionReference postsRef = firebaseFirestore.collection("Work");
                String postId = postsRef.document().getId();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("publisher", ic);
                hashMap.put("title", work.getTitle());
                hashMap.put("job_title", work.getJod_title());
                hashMap.put("type", work.getType());
                hashMap.put("timestamp", FieldValue.serverTimestamp());
                hashMap.put("location", work.getLocation());
                hashMap.put("pay", pay);
                hashMap.put("minimum", min);
                hashMap.put("maximum", max);
                hashMap.put("description", des);
                hashMap.put("key", key);
                hashMap.put("requirement", re);

                hashMap.put("id", postId);

                postsRef.document(postId).set(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            showDialog("Success", "Successfully Post Recruitment!");
                        })
                        .addOnFailureListener(e -> {
                            showDialog("Failed", "Please contact admin!");
                        });
            }
        });
    }
    public void getValue(){
        min = Objects.requireNonNull(txt_min.getText()).toString().trim();
        max = Objects.requireNonNull(txt_max.getText()).toString().trim();
        des = Objects.requireNonNull(txt_des.getText()).toString().trim();
        key = Objects.requireNonNull(txt_key.getText()).toString().trim();
        re = Objects.requireNonNull(txt_req.getText()).toString().trim();
    }
    public boolean checkEmpty(){
        boolean check = true;

        TextInputLayout l_pay = findViewById(R.id.text_input_layout_pay_cycle);
        TextInputLayout l_min = findViewById(R.id.text_input_layout_minimum_salary);
        TextInputLayout l_des = findViewById(R.id.text_input_layout_description);
        TextInputLayout l_key = findViewById(R.id.text_input_layout_responsibility);
        TextInputLayout l_req = findViewById(R.id.text_input_layout_requirement);

        if(pay == null){
            check = false;
            l_pay.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_pay.setHelperText("Please select Pay Cycle");
        }else if (pay.isEmpty()){
            check = false;
            l_pay.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_pay.setHelperText("Please select Pay Cycle");
        }else{
            l_pay.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_pay.setHelperText("");
        }

        if(min == null){
            check = false;
            l_min.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_min.setHelperText("Please Fill in Minimun Salary");
        }else if (min.isEmpty()){
            check = false;
            l_min.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_min.setHelperText("Please Fill in Minimun Salary");
        }else{
            l_min.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_min.setHelperText("");
        }

        if(des == null){
            check = false;
            l_des.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_des.setHelperText("Please Fill in Job Description");
        }else if (des.isEmpty()){
            check = false;
            l_des.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_des.setHelperText("Please Fill in Job Description");
        }else{
            l_des.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_des.setHelperText("");
        }

        if(key == null){
            check = false;
            l_key.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_key.setHelperText("Please Fill in Key Responsibility");
        }else if (key.isEmpty()){
            check = false;
            l_key.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_key.setHelperText("Please Fill in Key Responsibility");
        }else{
            l_key.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_key.setHelperText("");
        }

        if(re == null){
            check = false;
            l_req.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_req.setHelperText("Please Fill in Requirement");
        }else if (re.isEmpty()){
            check = false;
            l_req.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_req.setHelperText("Please Fill in Requirement");
        }else{
            l_req.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_req.setHelperText("");
        }

        return check;
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(create_work_salary.this);
        dialog.setContentView(R.layout.create_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));


        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);

        t.setText(title);
        d.setText(message);
        btnC.setText("Close");
        btnC.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(create_work_salary.this, Home.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}