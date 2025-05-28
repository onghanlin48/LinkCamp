package com.um.linkcamp;

import static function.function.sendmail;
import static function.function.verify_email;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import data.gui;
import data.register;

public class Change_email extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        TextInputEditText txt_email = findViewById(R.id.text_input_email);

        Button next = findViewById(R.id.next);
        next.setOnClickListener(v -> {
            String email = txt_email.getText().toString().trim();
            email = email.toLowerCase();
            TextInputLayout ly_email = findViewById(R.id.text_input_layout_email);
            if(email == null){
                ly_email.setHelperText("Please fill in your email!\nExample : abc@abc.com");
                ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            } else if(email.isEmpty()){
                ly_email.setHelperText("Please fill in your email!\nExample : abc@abc.com");
                ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));

            } else if (!verify_email(email)) {
                ly_email.setHelperText("Invalid email!\nExample : abc@abc.com");
                ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));

            } else{
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String finalEmail = email;
                db.collection("Users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot.isEmpty()) {
                                next(finalEmail);
                            } else {
                                ly_email.setHelperText("Email already exist!\nExample : abc@abc.com");
                                ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                            }
                        })
                        .addOnFailureListener(e -> {
                            ly_email.setHelperText("Email already exist!\nExample : abc@abc.com");
                            ly_email.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        });



            }
        });
    }
    private void next(String email){
        gui gui = new gui();
        String number = String.valueOf(function.function.random());
        String number2 = String.valueOf(function.function.random());
        try {
            sendmail(email,gui.otp_t, gui.otp_new(number));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        register r = register.getInstance();
        try {
            String password = function.function.hashPassword(number, number2);
            r.setPassword(password);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        r.setEmail(email);
        r.setOtp(3);
        r.setPage(6);
        r.setSalt(number2);
        Intent intent = new Intent(Change_email.this,OTPv.class);
        startActivity(intent);
        finish();
    }
}