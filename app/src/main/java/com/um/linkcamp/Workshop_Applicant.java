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
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import function.function;

public class Workshop_Applicant extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    ArrayList<String> applicant;
    RecyclerView recyclerView;
    ApplicantAdapter applicantAdapter;
    String ic = null,name =null,workshopTitle,workshopID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workshop_applicant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(Workshop_Applicant.this);
        VerifyLogin verifyLogin = new VerifyLogin(Workshop_Applicant.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    showDialog("Notice", "You have logged in on another device!");
                }else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    Intent intent = new Intent(Workshop_Applicant.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            Intent intent = new Intent(Workshop_Applicant.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
            name = cursor.getString(cursor.getColumnIndex("name"));
        }
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        workshopTitle = intent.get().getStringExtra("WorkshopTitle");
        workshopID = intent.get().getStringExtra("WorkshopID");
        if(workshopTitle.isEmpty() || workshopID.isEmpty()){
            finish();
        }else if (workshopTitle == null || workshopTitle == null){
            finish();
        }
        applicant = new ArrayList<>();

        recyclerView = findViewById(R.id.Applicant);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(Workshop_Applicant.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        applicantAdapter = new ApplicantAdapter(Workshop_Applicant.this,applicant,workshopTitle,name);
        recyclerView.setAdapter(applicantAdapter);
        readApp();

        TextView txt_title = findViewById(R.id.workshop_name);

        ImageView back = findViewById(R.id.back);

        Button btnAll = findViewById(R.id.send_email_all);

        txt_title.setText(workshopTitle);

        back.setOnClickListener(v -> {
            finish();
        });

        btnAll.setOnClickListener(v -> {
            sendmail();
        });
    }

    private void readApp() {
        applicant.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Workshop_Register")
                .document(workshopID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        for (String key : Objects.requireNonNull(document.getData()).keySet()) {
                            Object value = document.getData().get(key);
                            if (value instanceof Boolean) {

                                if ((Boolean) value) {
                                    applicant.add(key);
                                }
                            }
                        }
                        applicantAdapter.notifyDataSetChanged();
                    }
                });

    }

    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Workshop_Applicant.this);
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
            Intent intent = new Intent(Workshop_Applicant.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
    private void sendmail(){
        Dialog dialog = new Dialog(Workshop_Applicant.this);
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
                for (String userID : applicant){
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference Ref = db.collection("Users").document(userID);
                    String finalContent = content;
                    Ref.get().addOnSuccessListener(Snapshot -> {
                        if(Snapshot.exists()){
                            data.gui gui = new data.gui();
                            String user_name = Snapshot.getString("name");
                            String email = Snapshot.getString("email");
                            function.sendEmail(email,gui.workshop_title(workshopTitle,name),gui.workshop(finalContent,name,user_name),Workshop_Applicant.this);
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