package com.um.linkcamp;

import static function.function.hashPassword;
import static function.function.isValidPassword;
import static function.function.random_password;
import static function.function.remove;

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Create_password extends AppCompatActivity {
    TextInputLayout l_pass;
    int page;
    data.register register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_password);


        register = data.register.getInstance();

        page = register.getPage();

        Button btnNext = findViewById(R.id.next);
        TextView btnAlready = findViewById(R.id.already);

        l_pass = findViewById(R.id.text_input_layout_pass);
        TextInputLayout c_pass = findViewById(R.id.text_input_layout_cpass);
        TextInputEditText t_pass = findViewById(R.id.text_input_pass);
        TextInputEditText t_cpass = findViewById(R.id.text_input_cpass);

        TextView title = findViewById(R.id.create_text);
        TextView detail = findViewById(R.id.create_note_text);

        if(page == 2){
            title.setText("Reset Password");
            detail.setText("Forgot your password? \nDon't worry, reset your password here");
        }
        if(page == 7){
            title.setText("Change Password");
            detail.setVisibility(View.GONE);
            btnNext.setText("Change");
            btnAlready.setText("Back");
            btnAlready.setOnClickListener(v -> {
                finish();
            });
        }

        if(page == 1){
            btnAlready.setOnClickListener(v -> {
                register.clear();
                Intent intent = new Intent(Create_password.this,login_page.class);
                startActivity(intent);
                finish();
            });
        }else{
            btnAlready.setVisibility(View.GONE);
        }


        btnNext.setOnClickListener(v -> {
            l_pass.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.green1)));
            String pass = Objects.requireNonNull(t_pass.getText()).toString();

            if(!isValidPassword(pass,l_pass)){
                return;
            }

            String cpass = Objects.requireNonNull(t_cpass.getText()).toString();
            c_pass.setHelperText(null);
            if(!cpass.equals(pass)){
                c_pass.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                c_pass.setHelperText("The Password and Confirm Password no matches!");
                return;
            }
            String salt = random_password();
            try {
                String pass_salt = hashPassword(pass,salt);
                if (page == 2 || page == 7) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    String email = Objects.requireNonNull(register.getEmail());
                    String Password_old = Objects.requireNonNull(register.getProfile());
                    String OldHash= hashPassword(email, Password_old);
                    String loginHash = hashPassword(email, pass_salt);

                    OldHash = remove(OldHash);
                    loginHash = remove(loginHash);

                    DocumentReference oldLoginRef = db.collection("Login").document(OldHash);
                    DocumentReference newLoginRef = db.collection("Login").document(loginHash);

                    oldLoginRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                oldLoginRef.delete().addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        newLoginRef.set(snapshot.getData()).addOnCompleteListener(setTask -> {
                                            if (setTask.isSuccessful()) {
                                                updateUserData(pass_salt, salt, register.getIc());
                                            } else {
                                                showDialog("Failed", "Please contact admin!");
                                            }
                                        });
                                    } else {
                                        showDialog("Failed", "Could not remove old login. Please contact admin!");
                                    }
                                });
                            } else {
                                Map<String, Object> defaultData = new HashMap<>();
                                defaultData.put("ic", register.getIc());
                                defaultData.put("status", "new");

                                newLoginRef.set(defaultData).addOnCompleteListener(setTask -> {
                                    if (setTask.isSuccessful()) {
                                        updateUserData(pass_salt, salt, register.getIc());
                                    } else {
                                        showDialog("Failed", "Please contact admin!");
                                    }
                                });
                            }
                        } else {
                            showDialog("Failed", "Please contact admin!");
                        }
                    });

                    return;
                }


                register.setSalt(salt);
                register.setPassword(pass_salt);
                System.out.println(pass_salt);
                String role = register.getRole();
                if(role.equals("Company")){
                    Intent intent = new Intent(Create_password.this,SSM_Certificates.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                Intent intent = new Intent(Create_password.this,Upload_IC_Front.class);
                startActivity(intent);
                finish();

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        });
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Create_password.this);
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
            if (page == 7){
                finish();
            }else{
                Intent intent = new Intent(Create_password.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        });

        dialog.show();
    }
    private void updateUserData(String pass_salt,String salt,String ic) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(page == 7){
            register.setOtp(1);
        }else{
            register.setOtp(2);
        }
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("password", pass_salt);
        userUpdates.put("salt", salt);

        DocumentReference userRef = db.collection("Users").document(ic);

        userRef.update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    if(page == 7){
                        showDialog("Success", "Your password is change!");
                    }else {
                        showDialog("Success", "Your password is reset!");
                    }

                })
                .addOnFailureListener(e -> showDialog("Failed", "Please contact admin!"));

    }

}