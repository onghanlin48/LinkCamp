package com.um.linkcamp;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import data.DatabaseHelper;
import function.VerifyLogin;

public class Apply_Work extends AppCompatActivity {
    String id,ic;
    Uri pdfUri;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apply_work);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(Apply_Work.this);
        VerifyLogin verifyLogin = new VerifyLogin(Apply_Work.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    finish();
                } else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    finish();
                }
            });
        } else {
            finish();
            return;
        }
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        id = intent.get().getStringExtra("workID");
        String profile = intent.get().getStringExtra("Profile");
        String title = intent.get().getStringExtra("Title");
        String job_title = intent.get().getStringExtra("Job_title");
        if(id.isEmpty()){
            finish();
        }else if (id == null){
            finish();
        }

        TextView txt_title = findViewById(R.id.title);
        TextView txt_job = findViewById(R.id.job_title);

        ImageView img_profile = findViewById(R.id.profile_post);

        txt_title.setText(title);
        txt_job.setText(job_title);

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        if(profile.equals("skip")){
            img_profile.setImageResource(R.drawable.icon_person);
        }else {
            try {
                byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                img_profile.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LinearLayout upload_pdf = findViewById(R.id.upload_c);
        PDFView pdfView = findViewById(R.id.pdfView);
        pdfView.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
            intent1.setType("application/pdf");
            intent1.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent1, "Select PDF"), 1);
        });

        upload_pdf.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
            intent1.setType("application/pdf");
            intent1.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent1, "Select PDF"), 1);
        });

        Button next = findViewById(R.id.next);
        next.setOnClickListener(v -> {
            TextView txt = findViewById(R.id.text1);
            if (pdfUri == null) {
                txt.setVisibility(View.VISIBLE);
                txt.setText("Please Upload Resume!");
                txt.setTextColor(RED);
            } else {
                next.setEnabled(false);
                next.setBackgroundColor(GRAY);
                next.setText("Loading");
                back.setVisibility(View.GONE);
                next.setTextColor(WHITE);
                txt.setVisibility(View.GONE);
                txt.setTextColor(BLACK);
                upload(pdfUri);
            }
        });
        initConfig();
    }
    private void initConfig(){
        Map config = new HashMap();
        config.put("cloud_name", "dakrinvpf");
        config.put("api_key","184762337876237");
        config.put("api_secret","SM6tkXj_NUCpZF5qCHL7dV-N8LI");
        config.put("secure", true);
        try {
            MediaManager.init(this,config);
        } catch (IllegalStateException e) {
            // Handle the case when MediaManager is already initialized
            Log.w("MediaManager", "MediaManager is already initialized.");
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            pdfUri = data.getData();
            if (isFileSizeLessThan10MB(pdfUri)) {
                PDFView pdfView = findViewById(R.id.pdfView);
                pdfView.setVisibility(View.VISIBLE);

                pdfView.fromUri(pdfUri).load();

                ImageView imageView = findViewById(R.id.imageView);
                TextView textView = findViewById(R.id.text1);
                TextView textView1 = findViewById(R.id.text2);

                imageView.setVisibility(View.GONE);
                textView1.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
            } else {
                showWarning("Warning", "PDF cannot more than 10MB!");
            }


        }
    }
    private void showWarning(String title, String message) {
        Dialog dialog = new Dialog(Apply_Work.this);
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
    public boolean isFileSizeLessThan10MB(Uri pdfUri) {
        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(pdfUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            long fileSize = cursor.getLong(sizeIndex);
            cursor.close();

            return fileSize <= 10 * 1024 * 1024;
        }
        return false;
    }
    private void upload(Uri pdfUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("Candidates");
        String filename = ic+id;
        // Use the MediaManager to upload the file
        MediaManager.get().upload(pdfUri)
                .option("resource_type", "raw")
                .option("public_id", filename)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Optional: Show a loading spinner
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Get the URL of the uploaded file
                        String material = resultData.get("url").toString();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(ic,material);
                        postsRef.document(id).set(hashMap)
                                .addOnSuccessListener(aVoid -> {

                                    showDialog("Success", "Successfully Apply a Job!");

                                })
                                .addOnFailureListener(e -> {
                                    showDialog("Failed", "Please contact admin!");
                                });

                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        showWarning("Error", "Upload failed: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Handle retry logic if needed
                    }
                })
                .dispatch();
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Apply_Work.this);
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
            Intent intent = new Intent(Apply_Work.this, Home.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}