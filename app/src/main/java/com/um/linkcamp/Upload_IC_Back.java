package com.um.linkcamp;

import static function.convert.encodeImageToBase64;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
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

import java.io.IOException;

public class Upload_IC_Back extends AppCompatActivity {

    ImageView imageView;
    TextView txt1,txt2;
    String imageFront = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_ic_back);
        imageView = findViewById(R.id.imageView);
        txt1 = findViewById(R.id.text1);
        txt2 = findViewById(R.id.text2);

        data.register register = data.register.getInstance();

        TextView BtnAlready = findViewById(R.id.already);
        BtnAlready.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(Upload_IC_Back.this,login_page.class);
            startActivity(intent);
            finish();
        });

        LinearLayout BtnFront = findViewById(R.id.upload_ic);

        BtnFront.setOnClickListener(v -> {
            ImagePicker.with(Upload_IC_Back.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        Button btnNext = findViewById(R.id.next);
        TextView example = findViewById(R.id.example);

        btnNext.setOnClickListener(v -> {
            if(imageFront != null){
                register.setIc_back(imageFront);
                String role = register.getRole();
                if(role.equals("User")){
                    Intent intent = new Intent(Upload_IC_Back.this,Upload_profile.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(Upload_IC_Back.this,Upload_Certificates.class);
                    startActivity(intent);
                    finish();
                }

            }else{
                example.setTextColor(ColorStateList.valueOf(Color.RED));
                example.setText("Please Upload Back of IC!");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        Uri selectedImageUri = data.getData();
        if(selectedImageUri == null){
            return;
        }
        txt1.setVisibility(View.GONE);
        txt2.setVisibility(View.GONE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        try {
            imageFront = encodeImageToBase64(this,selectedImageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        layoutParams.height = convertDpToPx(200);;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageURI(selectedImageUri);

    }
    private int convertDpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density);
    }
}