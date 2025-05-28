package com.um.linkcamp;

import static function.function.check_ic_format;
import static function.function.check_ssm_format;
import static function.function.hashPassword;
import static function.function.random;
import static function.function.sendmail;
import static function.function.verify_email;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import data.gui;

public class forgot_password extends AppCompatActivity {
    private static final List<String> Role = new ArrayList<>();
    String role = null,passwordHash;
    TextInputLayout email_l,ic_l;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        db = FirebaseFirestore.getInstance();

        email_l = findViewById(R.id.text_input_layout_email);
        ic_l = findViewById(R.id.text_input_layout_ic);

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
            }else{
                ic_l.setHint("Identification Number");
                ic_l.setHelperText("Identification Number Without '-'");

            }
            email_l.setVisibility(View.VISIBLE);
            ic_l.setVisibility(View.VISIBLE);
        });

        TextView BtnBack = findViewById(R.id.back);
        BtnBack.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(forgot_password.this,login_page.class);
            startActivity(intent);
            finish();
        });

        Button BtnNext = findViewById(R.id.next);
        BtnNext.setOnClickListener(v -> {
            TextInputLayout txtrole = findViewById(R.id.text_input_layout_role);
            if(role == null){
                txtrole.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                return;
            }
            txtrole.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            TextInputEditText txt_email = findViewById(R.id.text_input_email);
            TextInputEditText txt_ic = findViewById(R.id.text_input_ic);

            String email = Objects.requireNonNull(txt_email.getText()).toString();
            email = email.toLowerCase();
            String ic = Objects.requireNonNull(txt_ic.getText()).toString();
            if(!valid(ic,email,ic_l,email_l)){
                return;
            }

            String finalEmail = email;
            checkIfExists(email,ic, Exists ->{
                if(Exists){
                    int number = random();
                    int number_2 = random();
                    data.gui gui = new data.gui();
                    sendmail(finalEmail,gui.otp_t, gui.otp_content_reset(String.valueOf(number)));
                    String password =  hashPassword(String.valueOf(number),String.valueOf(number_2));

                    register.setProfile(passwordHash);
                    register.setIc(ic);
                    register.setEmail(finalEmail);
                    register.setPassword(password);
                    register.setSalt(String.valueOf(number_2));
                    register.setOtp(3);
                    register.setPage(2);

                    Intent intent =new Intent(forgot_password.this, OTPv.class);
                    startActivity(intent);
                    finish();
                }else{
                    Dialog dialog = new Dialog(forgot_password.this);
                    dialog.setContentView(R.layout.create_dialog);
                    Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
                    TextView title = dialog.findViewById(R.id.tittle);
                    TextView detail = dialog.findViewById(R.id.detail);
                    title.setText("Warning");
                    if(role.equals("Company")){
                        detail.setText("Role , Registration Number or Email is Wrong");
                    }else{
                        detail.setText("Role , Identification Number or Email is Wrong");
                    }
                    Button close = dialog.findViewById(R.id.confirm);
                    close.setText("Close");
                    close.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    close.setOnClickListener(v1 -> {
                        dialog.dismiss();
                    });
                    dialog.show();
                }
            });


        });
    }

    private boolean valid(String ic, String email, TextInputLayout ly_ic,TextInputLayout ly_email) {
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
            }
        }else if(role.equals("Company")){
            if(!check_ssm_format(ic)){
                ly_ic.setHelperText("Invalid Registration Number!");
                ly_ic.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                number++;
            }
        } else{
            ly_ic.setHelperText(null);
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
    private void checkIfExists(String email,String ic, FirebaseCallback callback) {
        db.collection("Users")
                .whereEqualTo("email", email)
                .whereEqualTo("ic",ic)
                .whereEqualTo("role",role)
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
                            for (QueryDocumentSnapshot document : querySnapshot) {

                                passwordHash = document.getString("password");
                            }
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