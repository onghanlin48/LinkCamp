package com.um.linkcamp;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

import static function.convert.encodeImageToBase64;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import data.DatabaseHelper;
import function.VerifyLogin;

public class Create_learning extends AppCompatActivity {
    String title = null, description = null, material = null, ic = null,image_pro = null;
    private DatabaseHelper dbHelper;
    Uri pdfUri;
    boolean open;
    CheckBox channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_learning);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(Create_learning.this);
        VerifyLogin verifyLogin = new VerifyLogin(Create_learning.this);
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

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        Button next = findViewById(R.id.next);

        TextInputEditText edit_title = findViewById(R.id.text_input_title);
        TextInputEditText edit_description = findViewById(R.id.text_input_description);
        LinearLayout upload_pdf = findViewById(R.id.upload_c);

        LinearLayout profile_view = findViewById(R.id.upload_profile);
        ImageView profile = findViewById(R.id.profile);

        profile.setOnClickListener(v -> {
            ImagePicker.with(Create_learning.this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        profile_view.setOnClickListener(v -> {
            ImagePicker.with(Create_learning.this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        channel = findViewById(R.id.checkBox);
        channel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                profile_view.setVisibility(View.VISIBLE);
            } else {
                profile_view.setVisibility(View.GONE);
            }
        });

        PDFView pdfView = findViewById(R.id.pdfView);
        pdfView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
        });

        upload_pdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
        });
        next.setOnClickListener(v -> {
            title = Objects.requireNonNull(edit_title.getText()).toString().trim();
            description = Objects.requireNonNull(edit_description.getText()).toString().trim();
            open = channel.isChecked();
            if (checkEmpty()){
                next.setEnabled(false);
                next.setBackgroundColor(GRAY);
                next.setText("Loading");
                back.setVisibility(View.GONE);
                next.setTextColor(WHITE);
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


        }else{
            assert data != null;
            Uri selectedImageUri = data.getData();

            try {
                image_pro = encodeImageToBase64(this,selectedImageUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ImageView imageView = findViewById(R.id.profile);
            imageView.setImageURI(selectedImageUri);
            imageView.setVisibility(View.VISIBLE);

            TextView textView1 = findViewById(R.id.text1_profile);
            TextView textView2 = findViewById(R.id.text2_profile);
            ImageView imageView1 = findViewById(R.id.imageView1);
            textView2.setVisibility(View.GONE);
            textView1.setVisibility(View.GONE);
            imageView1.setVisibility(View.GONE);
        }
    }

    private void upload(Uri pdfUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("Learning");
        String postId = postsRef.document().getId();

        // Use the MediaManager to upload the file
        MediaManager.get().upload(pdfUri)
                .option("resource_type", "raw")
                .option("public_id", postId)
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
                        material = resultData.get("url").toString();
                        if (checkEmpty()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("publisher", ic);
                            hashMap.put("title", title);
                            hashMap.put("description", description);
                            hashMap.put("channel", open);
                            hashMap.put("timestamp", FieldValue.serverTimestamp());
                            hashMap.put("material", material);

                            hashMap.put("id", postId);

                            postsRef.document(postId).set(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        if (open) {
                                            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                            Map<String, Object> data = new HashMap<>();
                                            data.put(ic, true);
                                            data.put("profile",image_pro);
                                            firebaseFirestore.collection("Channel")
                                                    .document(postId)
                                                    .set(data)
                                                    .addOnSuccessListener(documentReference -> {
                                                        showDialog("Success", "Successfully Upload a Learning Material!");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        showDialog("Failed", "Please contact admin!");
                                                    });
                                        } else {
                                            showDialog("Success", "Successfully Upload a Learning Material!");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        showDialog("Failed", "Please contact admin!");
                                    });
                        }
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

    public boolean checkEmpty() {
        Boolean check = true;
        TextInputLayout l_tile = findViewById(R.id.text_input_layout_title);
        TextView txt = findViewById(R.id.text1);
        if (title == null) {
            check = false;
            l_tile.setHelperTextColor(ColorStateList.valueOf(RED));
            l_tile.setHelperText("Please fill in Title / Subject");
        } else if (title.isEmpty()) {
            check = false;
            l_tile.setHelperTextColor(ColorStateList.valueOf(RED));
            l_tile.setHelperText("Please fill in Title / Subject");
        } else {
            l_tile.setHelperTextColor(ColorStateList.valueOf(BLACK));
            l_tile.setHelperText(null);
        }

        if (pdfUri == null) {
            check = false;
            txt.setVisibility(View.VISIBLE);
            txt.setText("Please Upload Learning Material!");
            txt.setTextColor(RED);
        } else {
            txt.setVisibility(View.GONE);
            txt.setTextColor(BLACK);
        }

        if(channel.isChecked()){
            TextView textView1 = findViewById(R.id.text1_profile);
            TextView textView2 = findViewById(R.id.text2_profile);
            if(image_pro == null){
                check = false;
                textView1.setTextColor(ColorStateList.valueOf(RED));
                textView2.setTextColor(ColorStateList.valueOf(RED));

            } else if (image_pro.isEmpty()) {
                check = false;
                textView1.setTextColor(ColorStateList.valueOf(RED));
                textView2.setTextColor(ColorStateList.valueOf(RED));
            }
        }

        return check;
    }

    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Create_learning.this);
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
            Intent intent = new Intent(Create_learning.this, Home.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void showWarning(String title, String message) {
        Dialog dialog = new Dialog(Create_learning.this);
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



}