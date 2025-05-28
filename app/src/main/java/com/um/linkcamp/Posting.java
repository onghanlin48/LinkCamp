package com.um.linkcamp;

import static function.convert.encodeImageToBase64;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.github.dhaval2404.imagepicker.ImagePicker;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import data.DatabaseHelper;
import function.VerifyLogin;

public class Posting extends AppCompatActivity {
    ImageView result,close,delete;
    String p_image = null;
    Button post;
    EditText des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_posting);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        VerifyLogin verifyLogin = new VerifyLogin(Posting.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    databaseHelper.clearUserData();
                    finish();
                }else if (!("login".equals(result))) {
                    databaseHelper.clearUserData();
                    finish();
                }
            });
        }

        result = findViewById(R.id.imageView);
        post = findViewById(R.id.post);
        des = findViewById(R.id.description);
        close = findViewById(R.id.close);
        delete = findViewById(R.id.delete);

        des.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String check = s.toString();
                if(check.isEmpty()){
                    if(p_image.isEmpty()){
                        post.setEnabled(false);
                    }else {
                        post.setEnabled(true);
                    }
                }else{
                    post.setEnabled(true);
                }
            }
        });

        delete.setOnClickListener(v -> {
            result.setImageURI(null);
            p_image = null;
            delete.setVisibility(View.GONE);
            String check = (des.getText()).toString();
            if(check.isEmpty()){
                post.setEnabled(false);
            }else{
                post.setEnabled(true);
            }
        });

        ImageView photo = findViewById(R.id.photo);
        photo.setOnClickListener(v -> {
            imagePicker();
        });

        close.setOnClickListener(v -> {
            finish();
        });

        post.setOnClickListener(v -> {

            Cursor cursor = databaseHelper.getUserData();
            String ic = null;
            if(cursor.moveToFirst()){
                ic = cursor.getString(cursor.getColumnIndex("ic"));
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference postsRef = db.collection("Posts");
            String postId = postsRef.document().getId();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("postId",postId);
            hashMap.put("postImage",p_image);
            hashMap.put("description",des.getText().toString());
            hashMap.put("publisher",ic);
            hashMap.put("timestamp", FieldValue.serverTimestamp());

            postsRef.document(postId).set(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.e("FirestoreError", "Error adding post", e);
                    });
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        if(data.getData() != null){
            Uri selectedImageUri = data.getData();

            try {
                p_image = encodeImageToBase64(this,selectedImageUri);
                post.setEnabled(true);
                delete.setVisibility(View.VISIBLE);
                result.setImageURI(selectedImageUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public void imagePicker(){
        ImagePicker.with(Posting.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

}