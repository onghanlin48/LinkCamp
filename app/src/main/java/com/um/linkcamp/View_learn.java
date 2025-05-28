package com.um.linkcamp;

import static android.graphics.Color.RED;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import data.DatabaseHelper;
import function.VerifyLogin;
import model.Learn;
import model.Tutor;

public class View_learn extends AppCompatActivity {
    DatabaseHelper dbHelper;
    PDFView pdfView;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_learn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(View_learn.this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        VerifyLogin verifyLogin = new VerifyLogin(View_learn.this);
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
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        id = intent.get().getStringExtra("LearnID");
        if(id.isEmpty()){
            finish();
        }else if (id == null){
            finish();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference loginRef = db.collection("Learning").document(id);
        loginRef.addSnapshotListener((documentSnapshot, e)  ->{
            if (e != null) {
                finish();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String material = documentSnapshot.getString("material");
                String title = documentSnapshot.getString("title");
                TextView txt_title = findViewById(R.id.title);
                txt_title.setText(title);

                displayPdf(material);
            } else {
                finish();
            }
        });

    }
    private void displayPdf(String fileUrl) {
        pdfView = findViewById(R.id.pdfView);
        new DownloadPdfTask().execute(fileUrl);
    }

    private class DownloadPdfTask extends AsyncTask<String, Void, File> {

        @Override
        protected File doInBackground(String... strings) {
            File file = null;
            try {

                URL url = new URL(strings[0].replace("http://", "https://"));

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(15000);
                urlConnection.setRequestMethod("GET");
                HttpURLConnection.setFollowRedirects(true);

                // Log response headers and debug response
                int responseCode = urlConnection.getResponseCode();
                Log.d("ResponseCode", "Response code: " + responseCode);
                Map<String, List<String>> headers = urlConnection.getHeaderFields();
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    Log.d("Header", header.getKey() + ": " + header.getValue());
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();

                    // Step 2: Save the file locally with a .pdf extension
                    file = new File(getExternalFilesDir(null), "downloadedFile.pdf");
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            // Step 3: Display the PDF
            if (file != null && file.exists()) {
                pdfView.fromFile(file).load();
            } else {
                Toast.makeText(View_learn.this, "Failed to download or open the file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}