package com.um.linkcamp;

import static function.convert.encodeImageToBase64;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import data.DatabaseHelper;
import data.Workshop;
import function.VerifyLogin;

public class Create_WorkShop_tutor extends AppCompatActivity {
    String profile = null,name =null,position = null;
    int number = 0,index = 0;
    ImageView img_profile;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_work_shop_tutor);

        dbHelper = new DatabaseHelper(Create_WorkShop_tutor.this);
        VerifyLogin verifyLogin = new VerifyLogin(Create_WorkShop_tutor.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    showDialog_logout("Notice", "You have logged in on another device!");
                }else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    Intent intent = new Intent(Create_WorkShop_tutor.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            Intent intent = new Intent(Create_WorkShop_tutor.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        Workshop workshop = Workshop.getInstance();

       img_profile = findViewById(R.id.profile);
        TextInputEditText edit_name = findViewById(R.id.text_input_name);
        TextInputEditText edit_position = findViewById(R.id.text_input_position);
        TextView txt_title = findViewById(R.id.txt_title);

        number = workshop.getPage();
        index = workshop.getName().size();
        txt_title.setText("Profile Tutor "+(number + 1));
        if(index >= (number+1)){
            profile = workshop.getProfile().get(number);
            name = workshop.getName().get(number);
            position = workshop.getPosition().get(number);
            if(!"skip".equals(profile)){
                try {
                    byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    img_profile.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            edit_position.setText(position);
            edit_name.setText(name);
        }
        Button btnNext = findViewById(R.id.next);
        if((number+1) >= workshop.getTutor()){
            btnNext.setText("Submit");
        }else{
            btnNext.setText("Next");
        }


        TextView btnHome = findViewById(R.id.back);
        btnHome.setOnClickListener(v -> {
            workshop.clear();
            Intent intent = new Intent(Create_WorkShop_tutor.this,Home.class);
            startActivity(intent);
            finish();
        });
        TextView btnP = findViewById(R.id.btnPrevious);
        btnP.setOnClickListener(v -> {
            name = Objects.requireNonNull(edit_name.getText()).toString();
            position = Objects.requireNonNull(edit_position.getText()).toString();
            workshop.setProfile(profile,number);
            workshop.setName(name,number);
            workshop.setPosition(position,number);
            if(number == 0){
                Intent intent = new Intent(Create_WorkShop_tutor.this,Create_Workshop_Cover.class);
                startActivity(intent);
                finish();
            }else{
                number--;
                Workshop.setPage(number);
                Intent intent = new Intent(Create_WorkShop_tutor.this,Create_WorkShop_tutor.class);
                startActivity(intent);
                finish();
            }
        });

        CardView upload_profile = findViewById(R.id.btnCamera);
        upload_profile.setOnClickListener(v -> {
            ImagePicker.with(Create_WorkShop_tutor.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });
        CardView upload_profile1 = findViewById(R.id.btnImage);
        upload_profile1.setOnClickListener(v -> {
            ImagePicker.with(Create_WorkShop_tutor.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        btnNext.setOnClickListener(v -> {
            name = Objects.requireNonNull(edit_name.getText()).toString();
            position = Objects.requireNonNull(edit_position.getText()).toString();
            if(!check_empty()){
                if(profile == null){
                    profile = "skip";
                }
                workshop.setProfile(profile,number);
                workshop.setName(name,number);
                workshop.setPosition(position,number);
                number++;
                Workshop.setPage(number);
                if(workshop.getTutor() <= number){
                    String title = workshop.getTitle();
                    String location = workshop.getLocation();
                    String date = workshop.getDate();
                    String stime = workshop.getTime();
                    String etime = workshop.getEtime();
                    int tutor = workshop.getTutor();
                    String cover = workshop.getImageCover();
                    String description = workshop.getDescription();
                    ArrayList<String> name = workshop.getName();
                    ArrayList<String> position = workshop.getPosition();
                    ArrayList<String> profile = workshop.getProfile();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    for (int i = 0; i < tutor; i++) {
                        hashMap.put("Profile"+(i+1),profile.get(i));
                        hashMap.put("Name"+(i+1),name.get(i));
                        hashMap.put("Position"+(i+1),position.get(i));
                    }
                    hashMap.put("Title",title);
                    hashMap.put("Location",location);
                    hashMap.put("Date",date);
                    hashMap.put("Start",stime);
                    hashMap.put("End",etime);
                    hashMap.put("Cover",cover);
                    hashMap.put("Description",description);
                    hashMap.put("Tutor",tutor);
                    hashMap.put("timestamp", FieldValue.serverTimestamp());
                    hashMap.put("Close",workshop.getClose());

                    String ic = null;
                    DatabaseHelper databaseHelper = new DatabaseHelper(Create_WorkShop_tutor.this);
                    Cursor cursor = databaseHelper.getUserData();
                    if(cursor.moveToFirst()){
                        ic = cursor.getString(cursor.getColumnIndex("ic"));
                    }
                    hashMap.put("publisher",ic);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference postsRef = db.collection("Workshop");
                    String postId = postsRef.document().getId();

                    hashMap.put("id", postId);
                    postsRef.document(postId).set(hashMap)
                            .addOnSuccessListener(aVoid -> {
                                workshop.clear();
                                showDialog("Success","Successfully create a workshop!");
                            })
                            .addOnFailureListener(e -> {
                                workshop.clear();
                                showDialog("Failed","Please contact admin!");
                            });

                }else{
                    Intent intent = new Intent(Create_WorkShop_tutor.this,Create_WorkShop_tutor.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }
    private Boolean check_empty(){
        boolean check = false;
        TextInputLayout l_name = findViewById(R.id.text_input_layout_name);
        TextInputLayout l_position = findViewById(R.id.text_input_layout_position);
        if(name.isEmpty()){
            check = true;
            l_name.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_name.setHelperText("Please fill Tutor Name!");
        }else{
            l_name.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_name.setHelperText(null);
        }
        if(position.isEmpty()){
            check = true;
            l_position.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            l_position.setHelperText("Please fill Tutor's Major!");
        }else{
            l_position.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
            l_position.setHelperText(null);
        }
        return check;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        Uri selectedImageUri = data.getData();
        if(selectedImageUri == null){
            return;
        }

        try {
            profile = encodeImageToBase64(this,selectedImageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        img_profile.setImageTintList(null);
        img_profile.setImageURI(selectedImageUri);

    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Create_WorkShop_tutor.this);
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
            Intent intent = new Intent(Create_WorkShop_tutor.this, Home.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
    private void showDialog_logout(String title, String message) {
        Dialog dialog = new Dialog(Create_WorkShop_tutor.this);
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
            Intent intent = new Intent(Create_WorkShop_tutor.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}