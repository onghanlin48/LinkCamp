package com.um.linkcamp;

import static function.convert.encodeImageToBase64;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Objects;

import data.DatabaseHelper;
import data.Workshop;
import function.VerifyLogin;

public class Create_Workshop_Cover extends AppCompatActivity {
    TextView text1,text2;
    ImageView imageCover;
    String description = "",cover = "";
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_workshop_cover);

        dbHelper = new DatabaseHelper(Create_Workshop_Cover.this);
        VerifyLogin verifyLogin = new VerifyLogin(Create_Workshop_Cover.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    showDialog("Notice", "You have logged in on another device!");
                }else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    Intent intent = new Intent(Create_Workshop_Cover.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            Intent intent = new Intent(Create_Workshop_Cover.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        Workshop workshop = Workshop.getInstance();

        EditText edit_description = findViewById(R.id.description);

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        imageCover = findViewById(R.id.imageView);

        if(workshop.getDescription() != null){
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageCover.getLayoutParams();
            try {
                byte[] imageBytes = Base64.decode(workshop.getImageCover(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                layoutParams.height = convertDpToPx(200);;
                imageCover.setLayoutParams(layoutParams);
                imageCover.setImageBitmap(bitmap);
                cover = workshop.getImageCover();
            } catch (Exception e) {
                e.printStackTrace();
            }
            edit_description.setText(workshop.getDescription());
        }

        TextView btnP = findViewById(R.id.btnPrevious);
        btnP.setOnClickListener(v -> {
            description = (edit_description.getText()).toString();
            workshop.setDescription(description);
            workshop.setImageCover(cover);
            Intent intent = new Intent(Create_Workshop_Cover.this,Create_Workshop.class);
            startActivity(intent);
            finish();
        });
        TextView btnHome = findViewById(R.id.back);
        btnHome.setOnClickListener(v -> {
            workshop.clear();
            Intent intent = new Intent(Create_Workshop_Cover.this,Home.class);
            startActivity(intent);
            finish();
        });

        LinearLayout upload_cover = findViewById(R.id.upload_cover);
        upload_cover.setOnClickListener(v -> {
            ImagePicker.with(Create_Workshop_Cover.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        Button btnNext = findViewById(R.id.next);
        btnNext.setOnClickListener(v -> {
            description = (edit_description.getText()).toString();
            if(!check_empty()){
                workshop.setDescription(description);
                workshop.setImageCover(cover);
                Intent intent = new Intent(Create_Workshop_Cover.this,Create_WorkShop_tutor.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private Boolean check_empty(){
        Boolean check = false;

        TextView l_description = findViewById(R.id.de_title);

        if ((description.replaceAll("\\s","")).isEmpty()){
            check = true;
            l_description.setTextColor(ColorStateList.valueOf(Color.RED));
            l_description.setText("Description\nPlease fill in Description Workshop!");
        }else{
            l_description.setTextColor(ColorStateList.valueOf(Color.BLACK));
            l_description.setText("Description");
        }

        if(cover.isEmpty()){
            check = true;
            imageCover.setImageTintList(ColorStateList.valueOf(Color.RED));
            text1.setTextColor(ColorStateList.valueOf(Color.RED));
            text2.setTextColor(ColorStateList.valueOf(Color.RED));
        }else{
            imageCover.setImageTintList(null);
            text1.setTextColor(ColorStateList.valueOf(Color.BLACK));
            text2.setTextColor(ColorStateList.valueOf(Color.BLACK));
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
        text1.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageCover.getLayoutParams();
        try {
            cover = encodeImageToBase64(this,selectedImageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imageCover.setImageTintList(null);
        layoutParams.height = convertDpToPx(200);;
        imageCover.setLayoutParams(layoutParams);
        imageCover.setImageURI(selectedImageUri);

    }
    private int convertDpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density);
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Create_Workshop_Cover.this);
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
            Intent intent = new Intent(Create_Workshop_Cover.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}