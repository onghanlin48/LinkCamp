package com.um.linkcamp;

import static android.graphics.Color.RED;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import data.DatabaseHelper;
import function.VerifyLogin;
import function.function;

public class View_Resume extends AppCompatActivity {
    DatabaseHelper dbHelper;
    String username,name,title,resume,email;
    PDFView pdfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_resume);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(View_Resume.this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        VerifyLogin verifyLogin = new VerifyLogin(View_Resume.this);
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
        username = intent.get().getStringExtra("Username");
        title = intent.get().getStringExtra("Title");
        name = intent.get().getStringExtra("Name");
        resume = intent.get().getStringExtra("Resume");
        email = intent.get().getStringExtra("Email");
        if(username.isEmpty()){
            finish();
        }else if (username == null){
            finish();
        }

        displayPdf(resume);

        TextView name = findViewById(R.id.name);
        name.setText(username);

        Button send = findViewById(R.id.send_email);
        send.setOnClickListener(v -> {
            sendmail(email,username);
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
                Toast.makeText(View_Resume.this, "Failed to download or open the file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendmail(String email,String user_name){
        Dialog dialog = new Dialog(View_Resume.this);
        dialog.setContentView(R.layout.dialog_sendmail);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(View_Resume.this.getDrawable(R.drawable.dialog));
        dialog.setCancelable(false);

        Button btnc = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        TextInputEditText message = dialog.findViewById(R.id.content);
        TextView detail = dialog.findViewById(R.id.detail);
        btnc.setOnClickListener(v -> {
            String content = Objects.requireNonNull(message.getText()).toString().trim();
            if(content.isEmpty() || content == null){
                detail.setTextColor(RED);
                detail.setText("The content format can be HTML !\nPlease fill in the message!");
            }else{
                content = content.replace("\n", "<br>");
                data.gui gui = new data.gui();
                function.sendEmail(email,gui.work_title(title,name),gui.workshop(content,name,user_name),View_Resume.this);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}