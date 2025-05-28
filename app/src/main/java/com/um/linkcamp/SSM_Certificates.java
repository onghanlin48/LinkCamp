package com.um.linkcamp;

import static function.convert.encodeImageToBase64;

import android.app.Dialog;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.github.barteksc.pdfviewer.PDFView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SSM_Certificates extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    Dialog dialog;
    String pdf,image,c = null;
    TextView txt1,txt2;
    PDFView pdfV;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ssm_certificates);
        TextView BtnAlready = findViewById(R.id.already);
        data.register register = data.register.getInstance();

        pdfV = findViewById(R.id.pdfView);
        txt1 = findViewById(R.id.text1);
        txt2 = findViewById(R.id.text2);

        dialog = new Dialog(SSM_Certificates.this);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));

        BtnAlready.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(SSM_Certificates.this,login_page.class);
            startActivity(intent);
            finish();
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        DocumentReference docRef = db.collection("Example").document("SSM");

        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                pdf = documentSnapshot.getString("pdf");
                image = documentSnapshot.getString("image");

            } else {
                Log.d("Firestore", "No such document");
            }
        });

        TextView single = findViewById(R.id.image);
        single.setOnClickListener(v -> {
            dialog.setContentView(R.layout.image_dialog);
            ImageView imageView = dialog.findViewById(R.id.imageView);
            try {
                byte[] imageBytes = Base64.decode(image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.show();
        });

        TextView mutli = findViewById(R.id.pdf);
        mutli.setOnClickListener(v -> {
            dialog.setContentView(R.layout.pdf_dialog);
            PDFView pdfView = dialog.findViewById(R.id.pdfView);
            try {
                byte[] pdfBytes = Base64.decode(pdf, Base64.DEFAULT);
                InputStream pdfStream = new ByteArrayInputStream(pdfBytes);
                pdfView.fromStream(pdfStream).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.show();
        });

        LinearLayout BtnC = findViewById(R.id.upload_c);
        BtnC.setOnClickListener(v -> openFilePicker());

        Button btnNext = findViewById(R.id.next);

        btnNext.setOnClickListener(v -> {
            if(c != null){
                register.setCertificates(c);
                Intent intent = new Intent(SSM_Certificates.this, Upload_profile.class);
                startActivity(intent);
                finish();

            }else{
                Toast.makeText(this, "Please select JPEG, PNG, or PDF.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        String[] mimeTypes = {"image/jpeg", "image/png", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String mimeType = getContentResolver().getType(fileUri);
                ImageView imageView = findViewById(R.id.imageView);
                txt1.setVisibility(View.GONE);
                txt2.setVisibility(View.GONE);
                if (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/png"))) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                    layoutParams.height = convertDpToPx(200);;
                    imageView.setLayoutParams(layoutParams);
                    imageView.setVisibility(View.VISIBLE);
                    pdfV.setVisibility(View.GONE);
                    imageView.setImageURI(fileUri);
                    try {
                        c = encodeImageToBase64(this,fileUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else if (mimeType != null && (mimeType.equals("application/pdf"))) {
                    imageView.setVisibility(View.GONE);
                    pdfV.setVisibility(View.VISIBLE);
                    pdfV.fromUri(fileUri).load();
                    try {
                        c = encodeImageToBase64(this,fileUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                } else {
                    Toast.makeText(this, "Invalid file type. Please select JPEG, PNG, or PDF.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private int convertDpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density);
    }
}