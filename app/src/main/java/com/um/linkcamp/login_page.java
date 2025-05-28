package com.um.linkcamp;

import static function.function.hashPassword;
import static function.function.random;
import static function.function.sendmail;
import static function.function.verify_email;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import data.gui;
import data.register;

public class login_page extends AppCompatActivity {
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_login_page);
        final TextView BtnCreate = findViewById(R.id.BtnCreate);
        final TextView BtnForgot =findViewById(R.id.BtnForgot);
        BtnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(login_page.this,Create_account.class);
            startActivity(intent);
            finish();
        });
        BtnForgot.setOnClickListener(v -> {
            Intent intent = new Intent(login_page.this,forgot_password.class);
            startActivity(intent);
            finish();
        });
        Button btnLogin = findViewById(R.id.button);
        btnLogin.setOnClickListener(v -> {
            TextInputEditText txt_email = findViewById(R.id.text_input_email);
            TextInputEditText txt_password = findViewById(R.id.text_input_password);
            String email = Objects.requireNonNull(txt_email.getText()).toString();
            String pass = Objects.requireNonNull(txt_password.getText()).toString();
            email = email.toLowerCase();
            boolean check = false;

            TextInputLayout l_email = findViewById(R.id.text_input_layout_email);
            TextInputLayout l_password = findViewById(R.id.text_input_layout_password);
            if(email.isEmpty() ){
                l_email.setHelperText("Please fill you email");
                l_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                check = true;
            }else{
                if(!verify_email(email)){
                    l_email.setHelperText("Invalid email");
                    l_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    check = true;
                }else{
                    l_email.setHelperText(null);
                }
            }
            if(pass.isEmpty()){
                l_password.setHelperText("Please fill you Password");
                l_password.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                check = true;
            }else {
                l_password.setHelperText(null);
            }

            if(check){
                return;
            }
            db = FirebaseFirestore.getInstance();
            String finalEmail = email;

            db.collection("Users").whereEqualTo("email",finalEmail).get()
                    .addOnSuccessListener(querySnapshot ->{
                        if(querySnapshot.isEmpty()){
                            Dialog dialog = new Dialog(login_page.this);
                            dialog.setContentView(R.layout.create_dialog);
                            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
                            TextView title = dialog.findViewById(R.id.tittle);
                            TextView detail = dialog.findViewById(R.id.detail);
                            title.setText("Warning");
                            detail.setText("Email or Password Invalid");
                            Button close = dialog.findViewById(R.id.confirm);
                            close.setText("Close");
                            close.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                            close.setOnClickListener(v1 -> {
                                dialog.dismiss();
                            });
                            dialog.show();
                        }else{
                            String salt = null,dPass = null;
                            int status = 0;
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                    salt = document.getString("salt");
                                    dPass = document.getString("password");
                                    status = document.getLong("status").intValue();
                                String fPass;
                                try {
                                    fPass = hashPassword(pass, salt);
                                } catch (NoSuchAlgorithmException e) {
                                    throw new RuntimeException(e);
                                }
                                if (fPass.equals(dPass)) {

                                    // Handle pending status
                                    if (status == 1) {
                                        Dialog dialog = new Dialog(login_page.this);
                                        dialog.setContentView(R.layout.create_dialog);
                                        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
                                        TextView title = dialog.findViewById(R.id.tittle);
                                        TextView detail = dialog.findViewById(R.id.detail);
                                        title.setText("Pending");
                                        detail.setText("Please waiting admin approve!");
                                        Button close = dialog.findViewById(R.id.confirm);
                                        close.setText("Close");
                                        close.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                                        close.setOnClickListener(v1 -> {
                                            dialog.dismiss();
                                        });
                                        dialog.show();
                                        return;
                                    }

                                    // Handle blocked status
                                    if (status != 2) {
                                        Dialog dialog = new Dialog(login_page.this);
                                        dialog.setContentView(R.layout.create_dialog);
                                        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
                                        TextView title = dialog.findViewById(R.id.tittle);
                                        TextView detail = dialog.findViewById(R.id.detail);
                                        title.setText("Block");
                                        detail.setText("You account is block");
                                        Button close = dialog.findViewById(R.id.confirm);
                                        close.setText("Close");
                                        close.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                                        close.setOnClickListener(v1 -> {
                                            dialog.dismiss();
                                        });
                                        dialog.show();
                                        return;
                                    }

                                    // Generate OTP
                                    String otp1 = String.valueOf(random());
                                    String otp2 = String.valueOf(random());
                                    String otpHash;
                                    try {
                                        otpHash = hashPassword(otp1, otp2);
                                    } catch (NoSuchAlgorithmException e) {
                                        throw new RuntimeException(e);
                                    }

                                    // Send OTP email
                                    gui gui = new gui();
                                    try {
                                        sendmail(finalEmail, gui.otp_t, gui.otp_content_login(otp1));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                    // Store user data in the register singleton
                                    register register = data.register.getInstance();
                                    register.clear();
                                    register.setName(document.getString("name"));
                                    register.setEmail(finalEmail);
                                    register.setPassword(fPass);
                                    register.setIc(document.getString("ic"));
                                    String role = document.getString("role");
                                    register.setRole(role);
                                    register.setProfile(document.getString("profile"));

                                    if ("Company".equals(role)) {
                                        register.setCertificates(document.getString("certificates"));
                                    } else {
                                        register.setIc_front(document.getString("front"));
                                        register.setIc_back(document.getString("back"));
                                        if (!"User".equals(role)) {
                                            register.setCertificates(document.getString("certificates"));
                                        }
                                    }

                                    register.setPage(3);
                                    register.setOtp(3);
                                    register.setSalt(otp2);
                                    register.setHashOTp(otpHash);

                                    // Navigate to OTP page
                                    Intent intent = new Intent(login_page.this, OTPv.class);
                                    startActivity(intent);
                                    finish();
                                    return;
                                } else {
                                    Dialog dialog = new Dialog(login_page.this);
                                    dialog.setContentView(R.layout.create_dialog);
                                    Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
                                    TextView title = dialog.findViewById(R.id.tittle);
                                    TextView detail = dialog.findViewById(R.id.detail);
                                    title.setText("Warning");
                                    detail.setText("Email or Password Invalid");
                                    Button close = dialog.findViewById(R.id.confirm);
                                    close.setText("Close");
                                    close.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                                    close.setOnClickListener(v1 -> {
                                        dialog.dismiss();
                                    });
                                    dialog.show();
                                }
                            }


                        }

                    })
                    .addOnFailureListener(e -> {
                        Dialog dialog = new Dialog(login_page.this);
                        dialog.setContentView(R.layout.create_dialog);
                        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
                        TextView title = dialog.findViewById(R.id.tittle);
                        TextView detail = dialog.findViewById(R.id.detail);
                        title.setText("Error");
                        detail.setText("Failed to connect. Please try again later.");
                        Button close = dialog.findViewById(R.id.confirm);
                        close.setText("Close");
                        close.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                        close.setOnClickListener(v1 -> {
                            dialog.dismiss();
                        });
                        dialog.show();
                    });


        });
    }
}