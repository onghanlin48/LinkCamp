package com.um.linkcamp;

import static android.graphics.Color.RED;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import Adapter.ApplicantAdapter;
import Adapter.ApplyAdapter;
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import function.function;

public class View_Apply extends AppCompatActivity {
    String title,id,job,name;
    ArrayList<String> apply;
    RecyclerView recyclerView;
    ApplyAdapter applyAdapter;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_apply);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(View_Apply.this);
        VerifyLogin verifyLogin = new VerifyLogin(View_Apply.this);
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
            name = cursor.getString(cursor.getColumnIndex("name"));
        }

        ImageView btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        id = intent.get().getStringExtra("workID");
        title = intent.get().getStringExtra("Title");
        job = intent.get().getStringExtra("Job_title");
        if(title.isEmpty() || id.isEmpty()){
            finish();
        }else if (title == null || id == null){
            finish();
        }

        TextView txt_title = findViewById(R.id.work_name);
        TextView txt_job = findViewById(R.id.job_title);

        txt_title.setText(title);
        txt_job.setText(job);

        apply = new ArrayList<>();
        recyclerView = findViewById(R.id.Applicant);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(View_Apply.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        applyAdapter = new ApplyAdapter(View_Apply.this,apply,title,name,id);
        recyclerView.setAdapter(applyAdapter);
        readApp();
        Button btnAll = findViewById(R.id.send_email_all);
        btnAll.setOnClickListener(v -> {
            sendmail();
        });
    }
    private void readApp() {
        apply.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Candidates")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        for (String key : Objects.requireNonNull(document.getData()).keySet()) {
                            apply.add(key);
                        }
                        applyAdapter.notifyDataSetChanged();
                    }
                });

    }
    private void sendmail(){
        Dialog dialog = new Dialog(View_Apply.this);
        dialog.setContentView(R.layout.dialog_sendmail);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
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
                for (String userID : apply){
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference Ref = db.collection("Users").document(userID);
                    String finalContent = content;
                    Ref.get().addOnSuccessListener(Snapshot -> {
                        if(Snapshot.exists()){
                            data.gui gui = new data.gui();
                            String user_name = Snapshot.getString("name");
                            String email = Snapshot.getString("email");
                            function.sendEmail(email,gui.work_title(title,name),gui.workshop(finalContent,name,user_name),View_Apply.this);
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}