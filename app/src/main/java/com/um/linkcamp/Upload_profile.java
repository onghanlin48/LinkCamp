package com.um.linkcamp;

import static function.convert.encodeImageToBase64;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.IOException;

public class Upload_profile extends AppCompatActivity {
    ImageView profile;
    String image_pro = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_profile);

        profile = findViewById(R.id.profile);
        data.register register = data.register.getInstance();

        TextView BtnAlready = findViewById(R.id.already);
        BtnAlready.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(Upload_profile.this,login_page.class);
            startActivity(intent);
            finish();
        });

        TextView btnLater = findViewById(R.id.btnLater);
        btnLater.setOnClickListener(v -> {
            register.setProfile("skip");
            Intent intent = new Intent(Upload_profile.this,term.class);
            startActivity(intent);
            finish();
        });

        TextView exmaple = findViewById(R.id.example);
        Button btnNext = findViewById(R.id.next);
        btnNext.setOnClickListener(v -> {
            if(image_pro != null){
                register.setProfile(image_pro);
                Intent intent = new Intent(Upload_profile.this,term.class);
                startActivity(intent);
                finish();
            }else{
                exmaple.setTextColor(ColorStateList.valueOf(Color.RED));
            }
        });

        CardView card1 = findViewById(R.id.btnImage);
        CardView card2 = findViewById(R.id.btnCamera);
        card2.setOnClickListener(v -> {
            imagePicker();
        });
        card1.setOnClickListener(v -> {
            imagePicker();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            Uri selectedImageUri = data.getData();
            if(selectedImageUri != null){
                try {
                    image_pro = encodeImageToBase64(this,selectedImageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                profile.setImageURI(selectedImageUri);
            }
        }

    }

    public void imagePicker(){
        ImagePicker.with(Upload_profile.this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }
}