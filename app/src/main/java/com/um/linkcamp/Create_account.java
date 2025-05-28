package com.um.linkcamp;

import static function.function.check_ic_format;
import static function.function.check_ssm_format;
import static function.function.hashPassword;
import static function.function.random;
import static function.function.sendmail;
import static function.function.verify_email;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Create_account extends AppCompatActivity {

    private static final List<String> Role = new ArrayList<>();
    String role = null;
    TextInputLayout name_l,email_l,ic_l;

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        db = FirebaseFirestore.getInstance();

        final TextView BtnAlready = findViewById(R.id.already);
        final Button BtnNext = findViewById(R.id.next);
        final ConstraintLayout constraintLayout = findViewById(R.id.main);

        name_l = findViewById(R.id.text_input_layout_name);
        email_l = findViewById(R.id.text_input_layout_email);
        ic_l = findViewById(R.id.text_input_layout_ic);

        name_l.setVisibility(View.GONE);
        email_l.setVisibility(View.GONE);
        ic_l.setVisibility(View.GONE);

        db.collection("Role")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Role.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String role = document.getString("role");
                        if (role != null) {
                            Role.add(role);
                        }
                    }
                });
        data.register register = data.register.getInstance();
        register.clear();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Role);
        AutoCompleteTextView textView = findViewById(R.id.text_input_role);
        textView.setAdapter(adapter);

        textView.setOnItemClickListener((parent, view, position, id) -> {
            role = adapter.getItem(position);
            assert role != null;
            if(role.equals("Company")){
                ic_l.setHint("Registration Number");
                ic_l.setHelperText("Please Enter Your Company Registration Number");
                name_l.setHint("Company Name");
                name_l.setHelperText("Please Enter Your Company Name follow the Registration!");
            }else{
                ic_l.setHint("Identification Number");
                ic_l.setHelperText("Identification Number Without '-'");
                name_l.setHint("Name");
                name_l.setHelperText("Please Enter Your Name Follow Identification Card!");
            }
            name_l.setVisibility(View.VISIBLE);
            email_l.setVisibility(View.VISIBLE);
            ic_l.setVisibility(View.VISIBLE);
        });

        BtnAlready.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(Create_account.this,login_page.class);
            startActivity(intent);
            finish();
        });

        BtnNext.setOnClickListener(v -> {
            TextInputLayout txtrole = findViewById(R.id.text_input_layout_role);
            if(role == null){

                txtrole.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            txtrole.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            TextInputEditText txt_name = findViewById(R.id.text_input_name);
            TextInputEditText txt_email = findViewById(R.id.text_input_email);
            TextInputEditText txt_ic = findViewById(R.id.text_input_ic);

            String name = Objects.requireNonNull(txt_name.getText()).toString();
            String email = Objects.requireNonNull(txt_email.getText()).toString();
            String ic = Objects.requireNonNull(txt_ic.getText()).toString();
            email = email.toLowerCase();

            if(!valid(ic,name,email,ic_l,name_l,email_l)){
               return;
            }

            String finalEmail = email;
            checkIfEmailExists(email, emailExists -> {

                if (emailExists) {
                    email_l.setHelperText("Email already exist!\nExample : abc@abc.com");
                    email_l.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                }else{
                    email_l.setHelperText(null);
                }
                checkIfICExists(ic,icExists ->{
                    if (icExists) {
                        if(role.equals("Company")){
                            ic_l.setHelperText("Registration Number already exist");
                            ic_l.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        }else{
                            ic_l.setHelperText("Identification Number already exist");
                            ic_l.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        }
                    }else{
                        ic_l.setHelperText(null);
                    }
                    if(!emailExists && !icExists){
                        try {
                            int number = random();
                            int number_2 = random();
                            data.gui gui = new data.gui();
                            String password =  hashPassword(String.valueOf(number),String.valueOf(number_2));
                            sendmail(finalEmail,gui.otp_t, gui.otp_content(String.valueOf(number)));
                            Snackbar.make(constraintLayout,"OTP send successfully!",Snackbar.LENGTH_SHORT).show();

                            register.setIc(ic);
                            register.setEmail(finalEmail);
                            register.setName(name);
                            register.setRole(role);
                            register.setPassword(password);
                            register.setSalt(String.valueOf(number_2));
                            register.setOtp(3);
                            register.setPage(1);

                            Intent intent =new Intent(Create_account.this, OTPv.class);
                            startActivity(intent);
                            finish();
                        } catch (IOException | NoSuchAlgorithmException e) {
                            Snackbar.make(constraintLayout,"Failed to send OTP!",Snackbar.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }
                });


            });


        });
    }

    private boolean valid(String ic,String name, String email, TextInputLayout ly_ic,TextInputLayout ly_name, TextInputLayout ly_email) {
        int number = 0;
        if(ic.isEmpty()){
            if(role.equals("Company")){
                ly_ic.setHelperText("Please fill in Registration Number!");
            }else{
                ly_ic.setHelperText("Please fill in Identification Number Without '-' !");
            }
            ly_ic.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            number++;
        } else if (!role.equals("Company")) {
            if(!check_ic_format(ic)){
                ly_ic.setHelperText("Invalid Identification Number!");
                ly_ic.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                number++;
            }else{
                ly_ic.setHelperText(null);
            }
        }else if(role.equals("Company")){
            if(!check_ssm_format(ic)){
                ly_ic.setHelperText("Invalid Registration Number!");
                ly_ic.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                number++;
            }else{
                ly_ic.setHelperText(null);
            }
        } else{
            ly_ic.setHelperText(null);
        }
        if(name.isEmpty()){
            if(role.equals("Company")){
                ly_name.setHelperText("Please fill in company name follow the SSM!");
            }else{
                ly_name.setHelperText("Please fill in your name follow you identification card!");
            }

            ly_name.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            number ++;
        }else{
            ly_name.setHelperText(null);
        }
        if(email.isEmpty()){
            ly_email.setHelperText("Please fill in your email!\nExample : abc@abc.com");
            ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            number ++;
        } else if (!verify_email(email)) {
            ly_email.setHelperText("Invalid email!\nExample : abc@abc.com");
            ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            number ++;
        } else{

            ly_email.setHelperText(null);
        }
        return number <= 0;
    }

    private void checkIfEmailExists(String emailToCheck, FirebaseCallback callback) {
        db.collection("Users")
                .whereEqualTo("email", emailToCheck) // Example condition
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        try {
                            callback.onCallback(false);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            callback.onCallback(true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    try {
                        callback.onCallback(true);
                    } catch (IOException ev) {
                        throw new RuntimeException(ev);
                    } catch (NoSuchAlgorithmException ev) {
                        throw new RuntimeException(ev);
                    }
                });
    }
    private void checkIfICExists(String i, FirebaseCallback callback) {
        db.collection("Users")
                .whereEqualTo("ic", i) // Example condition
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        try {
                            callback.onCallback(false);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            callback.onCallback(true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    try {
                        callback.onCallback(true);
                    } catch (IOException ev) {
                        throw new RuntimeException(ev);
                    } catch (NoSuchAlgorithmException ev) {
                        throw new RuntimeException(ev);
                    }
                });
    }
}