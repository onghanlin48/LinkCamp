package com.um.linkcamp;

import static function.convert.encodeImageToBase64;
import static function.function.sendmail;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.base.FinalizablePhantomReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import data.DatabaseHelper;
import data.gui;
import data.register;
import function.VerifyLogin;

public class Edit_profile extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    String ic,name,profile,description,email,password;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(Edit_profile.this);
        VerifyLogin verifyLogin = new VerifyLogin(Edit_profile.this);
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
            name = cursor.getString(cursor.getColumnIndex("name"));
            email = cursor.getString(cursor.getColumnIndex("email"));
            password = cursor.getString(cursor.getColumnIndex("password"));
        }

        ImageView back = findViewById(R.id.back);

        back.setOnClickListener(v -> {
            Intent intent1 = new Intent(Edit_profile.this,Profile_view.class);
            intent1.putExtra("UserID",ic);
            startActivity(intent1);
            finish();
        });

        TextView txtName = findViewById(R.id.username);
        txtName.setText(name);

        ImageView imgProfile = findViewById(R.id.profile);

        TextInputEditText txt_description = findViewById(R.id.text_input_description);

        firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Users").document(ic);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
                profile = Snapshot.getString("profile");
                description = Snapshot.getString("description");
                if(profile.equals("skip")){
                    imgProfile.setImageResource(R.drawable.icon_person);
                }else {
                    try {
                        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        imgProfile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                txt_description.setText(description);
            }
        });

        Button btnSave = findViewById(R.id.next);
        btnSave.setOnClickListener(v -> {

            description = Objects.requireNonNull(txt_description.getText()).toString().trim();
            Map<String, Object> updates = new HashMap<>();
            updates.put("profile", profile);
            updates.put("description", description);

            Ref.update(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showDialog("Success","Successfully updated!");
                } else {
                    System.out.println(task.getException());
                    showDialog("Failed", "Please Contact admin!");
                }
            });
        });

        imgProfile.setOnClickListener(v -> {
            ImagePicker.with(Edit_profile.this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        ImageView upload_profile =findViewById(R.id.upload);
        upload_profile.setOnClickListener(v -> {
            ImagePicker.with(Edit_profile.this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        TextView txt_pass = findViewById(R.id.Change_pass);
        TextView txt_email = findViewById(R.id.Change_email);

        txt_email.setOnClickListener(v -> {
            gui gui = new gui();
            String number = String.valueOf(function.function.random());
            String number2 = String.valueOf(function.function.random());
            try {
                sendmail(email,gui.otp_t, gui.otp_v(number));
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
            r.setPage(5);
            r.setProfile(password);
            r.setIc(ic);
            r.setSalt(number2);
            Intent intent = new Intent(Edit_profile.this,OTPv.class);
            startActivity(intent);
        });

        txt_pass.setOnClickListener(v -> {
            gui gui = new gui();
            String number = String.valueOf(function.function.random());
            String number2 = String.valueOf(function.function.random());
            try {
                sendmail(email,gui.otp_t, gui.otp_v(number));
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
            r.setPage(7);
            r.setProfile(password);
            r.setIc(ic);
            r.setSalt(number2);
            Intent intent = new Intent(Edit_profile.this,OTPv.class);
            startActivity(intent);
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            ImageView imageProfile = findViewById(R.id.profile);
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {

                profile = encodeImageToBase64(selectedImageUri);

                imageProfile.setImageURI(selectedImageUri);
            }

        }

    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Edit_profile.this);
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
        });

        dialog.show();
    }

    private String encodeImageToBase64(Uri imageUri) {
        String base64String = null;
        try {
            // Open the InputStream
            InputStream inputStream = Edit_profile.this.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                // Decode the image to Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    // Compress and convert Bitmap to byte array
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                } else {
                    System.out.println("Failed to decode image.");
                }
            } else {
                System.out.println("Failed to open InputStream.");
            }
        } catch (IOException e) {
            System.out.println("Error encoding image to Base64.  " + e);
        }
        return base64String;
    }

}