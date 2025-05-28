package com.um.linkcamp;

import static function.function.addTextWatcher;
import static function.function.hashPassword;
import static function.function.remove;
import static function.function.sendmail;
import android.provider.Settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;

import android.os.Bundle;
import android.os.CountDownTimer;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import data.DatabaseHelper;


public class OTPv extends AppCompatActivity {
    private String otp;
    private DatabaseHelper dbHelper;
    int page;
    data.register register = data.register.getInstance();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_otpv);

        String email = register.getEmail();
        page = register.getPage();

        TextView title = findViewById(R.id.create_text);
        TextView detail = findViewById(R.id.create_note_text);

        if(page == 2){
            title.setText("Reset Password");
            detail.setText("Forgot your password? \nDon't worry, reset your password here");
        } else if (page == 3) {
            title.setText("Login Verify");
            detail.setText("Welcome back you've\nbeen missed!");
        }else if(page == 5){
            title.setText("Verify");
            detail.setText("Verify Account");
        } else if (page == 6) {
            title.setText("Verify");
            detail.setText("Verify New Email");  
        }else if(page == 7){
            title.setText("Verify");
            detail.setText("Verify Account");
        }


        TextView email_ = findViewById(R.id.text_email);
        email_.setText(email);

        EditText otp1_ = findViewById(R.id.otp1);
        EditText otp2_ = findViewById(R.id.otp2);
        EditText otp3_ = findViewById(R.id.otp3);
        EditText otp4_ = findViewById(R.id.otp4);


        addTextWatcher(otp1_, otp2_);
        addTextWatcher(otp2_, otp3_);
        addTextWatcher(otp3_, otp4_);



        otp2_.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL && otp2_.getText().length() == 0) {
                otp1_.requestFocus();
            }
            return false;
        });

        otp3_.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL && otp3_.getText().length() == 0) {
                otp2_.requestFocus();
            }
            return false;
        });

        otp4_.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL && otp4_.getText().length() == 0) {
                otp3_.requestFocus();
            }
            return false;
        });

        TextView otp_msg = findViewById(R.id.otp_msg);
        TextView already = findViewById(R.id.already);
        TextView resend = findViewById(R.id.resend);

        Button BtnVerify = findViewById(R.id.verify);

        long timerDuration = TimeUnit.MINUTES.toMillis(1);
        long tickInterval = 10;

        if(page == 1){
            already.setOnClickListener(v -> {
                register.clear();
                Intent intent = new Intent(OTPv.this,Create_password.class);
                startActivity(intent);
                finish();
            });
        } else if (page == 5 || page == 6 || page == 7) {
            already.setText("Back");
            already.setOnClickListener(v -> {
                register.clear();
                finish();
            });
        } else{
            already.setVisibility(View.GONE);
        }


        resend.setOnClickListener(v -> {
            register.setOtp(3);
            String number = String.valueOf(function.function.random());
            String number1 = String.valueOf(function.function.random());
            try {
                String password = function.function.hashPassword(number, number1);
                if(page == 3){
                    register.setHashOTp(password);
                }else{
                    register.setPassword(password);
                }

                System.out.println(password);
                register.setSalt(number1);

                data.gui gui = new data.gui();
                if(page == 1){
                    sendmail(email,gui.otp_t_r, gui.otp_content(number));
                }else if (page == 2){
                    sendmail(email,gui.otp_t_r, gui.otp_content_reset(number));
                } else if (page == 3) {
                    sendmail(email,gui.otp_t_r, gui.otp_content_login(number));
                } else if (page == 5) {
                    sendmail(email,gui.otp_t_r, gui.otp_v(number));
                } else if (page == 6) {
                    sendmail(email,gui.otp_t_r, gui.otp_new(number));
                } else if (page == 7) {
                    sendmail(email,gui.otp_t_r, gui.otp_v(number));
                }


                new CountDownTimer(timerDuration, tickInterval) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        String timeText = String.format(Locale.getDefault(), "%02d",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                        );
                        resend.setText(timeText);
                        resend.setEnabled(false);
                    }

                    @Override
                    public void onFinish() {

                        resend.setText("Resend OTP");
                        resend.setEnabled(true);
                    }
                }.start();

            } catch (NoSuchAlgorithmException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        BtnVerify.setOnClickListener(v -> {
            int o = register.getOtp();

            if(o<= 0){
                otp_msg.setText("Please resend you OTP code!");
                return;
            }

            String otp1 = otp1_.getText().toString();
            String otp2 = otp2_.getText().toString();
            String otp3 = otp3_.getText().toString();
            String otp4 = otp4_.getText().toString();

            if(empty_check(otp1,otp2,otp3,otp4)){
                otp_msg.setText("The OTP code must be 4 digit number!");
                System.out.println(otp1);
                return;
            }
            try {
                if(!check_otp()){
                    o--;
                    register.setOtp(o);
                    otp_msg.setText("The OTP code is wrong! Left attempt :" + register.getOtp());
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            if(page == 1 || page == 2){
                Intent intent = new Intent(OTPv.this,Create_password.class);
                startActivity(intent);
                finish();
            } else if (page == 5) {
                Intent intent = new Intent(OTPv.this,Change_email.class);
                startActivity(intent);
                finish();
            } else if (page == 6) {
                String ic = register.getIc();
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                DocumentReference Ref = firebaseFirestore.collection("Users").document(ic);

                Map<String, Object> updates = new HashMap<>();
                updates.put("email", email);

                Ref.update(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        register.setOtp(3);
                        DatabaseHelper databaseHelper = new DatabaseHelper(OTPv.this);
                        databaseHelper.clearUserData();
                        Intent intent = new Intent(OTPv.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

            } else if (page == 7) {
                Intent intent = new Intent(OTPv.this,Create_password.class);
                startActivity(intent);
                finish();
            } else if (page == 3) {
                String profile = register.getProfile();
                String name = register.getName();
                String password = register.getPassword();
                String role = register.getRole();
                String ic = register.getIc();
                String front = null;
                String back = null;
                String certificates = null;
                if(role.equals("Company")){
                    certificates = register.getCertificates();
                }else{
                    front = register.getIc_front();
                    back = register.getIc_back();
                    if(role.equals("User")){
                        certificates = register.getCertificates();
                    }
                }
                String login_detail;
                try {
                    login_detail = hashPassword(email,password);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                login_detail = remove(login_detail);
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String login_status = null;
                try {
                    login_status = hashPassword(email,deviceId);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }


                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String finalLogin_status = login_status;
                String finallogin_detail = login_detail;
                String finalFront = front;
                String finalBack = back;
                String finalCertificates = certificates;

                DocumentReference loginRef = db.collection("Login").document(login_detail);

                loginRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            // If the document exists, check the status field
                            String status = snapshot.getString("status");
                            if ("new".equals(status)) {
                                // Handle "new" status
                                dbHelper = new DatabaseHelper(OTPv.this);
                                dbHelper.insertUser(profile, name, password, role, ic, email, finallogin_detail, finalLogin_status, finalFront, finalBack, finalCertificates);

                                // Update the status field in Firestore
                                loginRef.update("status", finalLogin_status);

                                // Navigate to the Home activity
                                Intent intent = new Intent(OTPv.this, Home.class);
                                startActivity(intent);
                                finish();


                            } else {
                                // Handle existing login
                                loginRef.update("status", finalLogin_status);

                                dbHelper = new DatabaseHelper(OTPv.this);
                                dbHelper.insertUser(profile, name, password, role, ic, email, finallogin_detail, finalLogin_status, finalFront, finalBack, finalCertificates);

                                // Show notice about other device
                                show("Notice", "Already Logout other device!");
                            }
                        } else {
                            // If the document does not exist, create it with default data
                            Map<String, Object> defaultData = new HashMap<>();
                            defaultData.put("ic", register.getIc());
                            defaultData.put("status", finalLogin_status);

                            loginRef.set(defaultData).addOnCompleteListener(createTask -> {
                                if (createTask.isSuccessful()) {
                                    dbHelper = new DatabaseHelper(OTPv.this);
                                    dbHelper.insertUser(profile, name, password, role, ic, email, finallogin_detail, finalLogin_status, finalFront, finalBack, finalCertificates);

                                    // Navigate to the Home activity
                                    Intent intent = new Intent(OTPv.this, Home.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Show error dialog
                                    showDialog("Failed", "Please contact admin!");
                                }
                            });
                        }
                    } else {
                        // Handle Firestore errors
                        Log.w("Firestore", "Error getting document", task.getException());
                    }
                });

            }
        });

    }


    public boolean empty_check(String otp1,String otp2,String otp3,String otp4){
        if(otp1.isEmpty() || otp2.isEmpty() || otp3.isEmpty() || otp4.isEmpty()){
            return true;
        }
        otp = otp1+otp2+otp3+otp4;
        return false;
    }

    public boolean check_otp() throws NoSuchAlgorithmException {
        String pas = hashPassword(otp,register.getSalt());


        assert pas != null;
        if(register.getPage() == 3){
            return pas.equals(register.getHashOTp());
        }else{
            return pas.equals(register.getPassword());
        }
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(OTPv.this);
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
            Intent intent = new Intent(OTPv.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
    private void show(String title,String message){
        Dialog dialog = new Dialog(OTPv.this);
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
            Intent intent = new Intent(OTPv.this, Home.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}