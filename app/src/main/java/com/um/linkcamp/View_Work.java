package com.um.linkcamp;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import data.DatabaseHelper;
import function.VerifyLogin;
import model.Tutor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class View_Work extends AppCompatActivity {
    DatabaseHelper dbHelper;
    String ic,id,publisher,profile_,title,job,role;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_work);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(View_Work.this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        VerifyLogin verifyLogin = new VerifyLogin(View_Work.this);
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
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
            role = cursor.getString(cursor.getColumnIndex("role"));
        }

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        id = intent.get().getStringExtra("workID");
        if(id.isEmpty()){
            finish();
        }else if (id == null){
            finish();
        }

        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnApply = findViewById(R.id.btnApply);

        TextView txt_title = findViewById(R.id.title);
        TextView txt_username = findViewById(R.id.username);
        TextView txt_job = findViewById(R.id.job);
        TextView txt_job_title = findViewById(R.id.job_title);
        TextView txt_time = findViewById(R.id.time);
        TextView txt_location = findViewById(R.id.location);
        TextView txt_description = findViewById(R.id.description);
        TextView txt_key = findViewById(R.id.key);
        TextView txt_requirement = findViewById(R.id.requirement);
        TextView txt_type = findViewById(R.id.type);
        TextView txt_salary = findViewById(R.id.salary);

        ImageView profile = findViewById(R.id.profile_post);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference loginRef = db.collection("Work").document(id);
        loginRef.addSnapshotListener((documentSnapshot, e)  ->{
            if (e != null) {
                finish();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                publisher = documentSnapshot.getString("publisher");
                title = documentSnapshot.getString("title");
                job = documentSnapshot.getString("job_title");
                String location = documentSnapshot.getString("location");
                String key = documentSnapshot.getString("key");
                String requirement = documentSnapshot.getString("requirement");
                Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");
                String description = documentSnapshot.getString("description");
                String maximum = documentSnapshot.getString("maximum");
                String minimum = documentSnapshot.getString("minimum");
                String pay = documentSnapshot.getString("pay");
                String type = documentSnapshot.getString("type");

                String salary;
                if(maximum == null){
                    salary = "RM "+minimum + " / " + pay;
                }else if(maximum.isEmpty()){
                    salary = "RM "+minimum + " / " + pay;
                }else{
                    salary ="RM "+ minimum + " - " + maximum + " / " + pay;

                }
                Date date = timestamp.toDate();

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

                txt_job.setText(title);
                txt_title.setText(title);
                txt_job_title.setText(job);
                txt_location.setText(location);
                txt_key.setText(key);
                txt_requirement.setText(requirement);
                txt_time.setText(sdf.format(date));
                txt_description.setText(description);
                txt_type.setText(type);
                txt_salary.setText(salary);

                showProfile(profile,txt_username);
                if(publisher.equals(ic)){
                    btnDelete.setVisibility(View.VISIBLE);
                    btnApply.setText("Candidates");
                }else{
                    if ("Company".equals(role)){
                        btnApply.setVisibility(View.GONE);
                    }
                    btnDelete.setVisibility(View.GONE);
                    checkUserExists(btnApply);
                }

            } else {
                finish();
            }
        });

        btnDelete.setOnClickListener(v -> {
            Dialog_Delete("Delete Recruitment","Do you want delete this recruitment!");
        });
        btnApply.setOnClickListener(v -> {
            if(publisher.equals(ic)){
                checkEmpty();
            }else{
                Intent intent1 = new Intent(View_Work.this, Apply_Work.class);
                intent1.putExtra("workID",id);
                intent1.putExtra("Title",title);
                intent1.putExtra("Job_title",job);
                intent1.putExtra("Profile",profile_);
                startActivity(intent1);
            }

        });

        profile.setOnClickListener(v -> {
            Intent intent1 = new Intent(View_Work.this, Profile_view.class);
            intent1.putExtra("UserID",publisher);
            startActivity(intent1);
        });

    }

    private void Delete() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Candidates")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Check if the document has any data
                        if (task.getResult().exists() && !task.getResult().getData().isEmpty()) {
                            DocumentSnapshot document = task.getResult();
                            String url = "https://nodemaillin.netlify.app/.netlify/functions/delete";
                            for (String key : Objects.requireNonNull(document.getData()).keySet()) {

                                String jsonPayload = "{ \"resourceId\": \""+key+id+"\" }";
                                RequestBody body = RequestBody.create(
                                        jsonPayload,
                                        MediaType.parse("application/json; charset=utf-8")
                                );

                                // Build the request
                                Request request = new Request.Builder()
                                        .url(url)
                                        .post(body)
                                        .build();
                                OkHttpClient client = new OkHttpClient();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        // Handle failure
                                        System.err.println("Request failed: " + e.getMessage());
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        // Handle success
                                        if (response.isSuccessful()) {
                                            System.out.println("Response: " + response.body().string());
                                        } else {
                                            System.err.println("Request failed with code: " + response.code());
                                        }
                                    }
                                });
                            }
                            FirebaseFirestore.getInstance()
                                    .collection("Work")
                                    .document(id)
                                    .delete();
                            FirebaseFirestore.getInstance()
                                    .collection("Candidates")
                                    .document(id)
                                    .delete();
                            finish();
                        } else {
                            FirebaseFirestore.getInstance()
                                    .collection("Work")
                                    .document(id)
                                    .delete();
                            FirebaseFirestore.getInstance()
                                    .collection("Candidates")
                                    .document(id)
                                    .delete();
                            finish();
                        }
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection("Work")
                                .document(id)
                                .delete();
                        FirebaseFirestore.getInstance()
                                .collection("Candidates")
                                .document(id)
                                .delete();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    FirebaseFirestore.getInstance()
                            .collection("Work")
                            .document(id)
                            .delete();
                    FirebaseFirestore.getInstance()
                            .collection("Candidates")
                            .document(id)
                            .delete();
                    finish();
                });


    }

    private void showProfile(ImageView profile, TextView name){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Users").document(publisher);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
              profile_ = Snapshot.getString("profile");
                if(profile_.equals("skip")){
                    profile.setImageResource(R.drawable.icon_person);
                }else {
                    try {
                        byte[] imageBytes = Base64.decode(profile_, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                name.setText(Snapshot.getString("name"));
            }
        });
    }
    private void Dialog_Delete(String title, String message) {
        Dialog dialog = new Dialog(View_Work.this);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(null);

        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        btnCancel.setBackgroundColor(ContextCompat.getColor(View_Work.this, R.color.blue1));
        btnC.setBackgroundColor(ContextCompat.getColor(View_Work.this, R.color.red));
        btnC.setText("Delete");
        t.setText(title);
        d.setText(message);
        btnC.setOnClickListener(v -> {
            Delete();
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
    public void checkUserExists(Button btn) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the specific Candidates document
        DocumentReference docRef = db.collection("Candidates").document(id);

        // Fetch the document
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Check if the specific userKey exists
                        if (document.contains(ic)) {
                            btn.setEnabled(false);
                            btn.setBackgroundColor(GRAY);
                            btn.setText("Already Apply");
                        }
                    } else {
                        System.out.println("Document does not exist.");
                    }
                } else {
                    System.out.println("Error getting document: " + task.getException());
                }
            }
        });
    }

    public void checkEmpty() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Candidates")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Check if the document has any data
                        if (task.getResult().exists() && !task.getResult().getData().isEmpty()) {
                            // Document exists and is not empty
                            Intent intent1 = new Intent(View_Work.this, View_Apply.class);
                            intent1.putExtra("workID", id);
                            intent1.putExtra("Title", title);
                            intent1.putExtra("Job_title", job);
                            startActivity(intent1);
                        } else {
                            // Document is empty or does not exist
                            showDialog("Warning", "No Applicants!");
                        }
                    } else {
                        // Task was unsuccessful or result is null
                        showDialog("Warning", "No Applicants!");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore retrieval failure
                    showDialog("Warning", "No Applicants!");
                });
    }

    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(View_Work.this);
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
}