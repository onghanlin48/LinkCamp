package com.um.linkcamp;

import android.app.Dialog;
import android.content.Intent;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import java.util.Objects;

import data.DatabaseHelper;


public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final Button btnLogin = findViewById(R.id.login);
        final Button btnregis = findViewById(R.id.register);

        data.register register = data.register.getInstance();
        if(register.getOtp() == 1){
            register.clear();
            showDialog("Success", "Your password is change!");
        }else if(register.getOtp() == 2){
            register.clear();
            showDialog("Success", "Your password is reset!");
        }else if(register.getOtp() == 3){
            register.clear();
            showDialog("Success", "Your email is change!");
        }

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,login_page.class);
            startActivity(intent);

        });

        btnregis.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,Create_account.class);
            startActivity(intent);
        });
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.create_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
        dialog.setCancelable(false);

        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);

        t.setText(title);
        d.setText(message);

        btnC.setOnClickListener(v -> {
           dialog.dismiss();
        });

        dialog.show();
    }

}