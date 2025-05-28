package com.um.linkcamp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

import Adapter.WorkAdapter;
import Adapter.WorkshopAdapter;
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import model.Work;
import model.Workshop;

public class View_Applied extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    TextView txt_job,txt_workshop;
    RecyclerView work_view,workshop_view;
    ArrayList<Workshop> workshopList;
    ArrayList<Work> workList;
    WorkshopAdapter workshopAdapter;
    WorkAdapter workAdapter;
    String ic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_applied);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(View_Applied.this);
        VerifyLogin verifyLogin = new VerifyLogin(View_Applied.this);
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
        }

        txt_job = findViewById(R.id.job);
        txt_workshop = findViewById(R.id.workshop);

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(View_Applied.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);

        work_view = findViewById(R.id.work_view);
        workshop_view = findViewById(R.id.workshop_view);

        setupRecyclerView(work_view, new LinearLayoutManager(this), spacingInPixels);
        setupRecyclerView(workshop_view, new LinearLayoutManager(this), spacingInPixels);

        workshopList = new ArrayList<>();
        workshopAdapter = new WorkshopAdapter(View_Applied.this,workshopList);
        workshop_view.setAdapter(workshopAdapter);

        workList = new ArrayList<>();
        workAdapter = new WorkAdapter(View_Applied.this,workList);
        work_view.setAdapter(workAdapter);
        selected("Job",txt_job);
        showRecyclerView(work_view);
        UpdateWork();
        UpdateWorkshop();



        txt_workshop.setOnClickListener(v -> {
            selected("Workshop",txt_workshop);
            showRecyclerView(workshop_view);

        });

        txt_job.setOnClickListener(v -> {
            selected("Job",txt_job);
            showRecyclerView(work_view);
        });
        ImageView btnback =findViewById(R.id.back);

        btnback.setOnClickListener(v -> {
            finish();
        });

    }
    public void selected(String title,TextView textView){
        txt_job.setText("Job");
        txt_workshop.setText("Workshop");

        txt_job.setTextColor(getResources().getColor(R.color.black, null));
        txt_workshop.setTextColor(getResources().getColor(R.color.black, null));

        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(new UnderlineSpan(), 0, title.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(getResources().getColor(R.color.blue1, null));
    }
    private void setupRecyclerView(RecyclerView recyclerView, LinearLayoutManager layoutManager, int spacingInPixels) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        recyclerView.setVisibility(View.GONE);
    }
    private void showRecyclerView(RecyclerView recyclerView) {
        work_view.setVisibility(View.GONE);
        workshop_view.setVisibility(View.GONE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void UpdateWork(){
        workList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Candidates")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {

                            if (document.contains(ic)) {

                                String candidatesID = document.getId();
                                work(candidatesID);
                            }
                        }
                        RemoveWork();
                    }
                });

    }
    private void work(String id) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firebaseFirestore.collection("Work").document(id);

        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", Objects.requireNonNull(e.getMessage()));
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String title = documentSnapshot.getString("title");
                String location = documentSnapshot.getString("location");
                String minimum = documentSnapshot.getString("minimum");
                String maximum = documentSnapshot.getString("maximum");
                String pay = documentSnapshot.getString("pay");
                String job_title = documentSnapshot.getString("job_title");
                String publisher = documentSnapshot.getString("publisher");
                Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                // Construct salary string
                String salary;
                if (maximum == null || maximum.isEmpty()) {
                    salary = "RM " + minimum + " / " + pay;
                } else {
                    salary = "RM " + minimum + " - " + maximum + " / " + pay;
                }

                Work work = new Work(id, title, job_title, salary, publisher, location, timestamp);
                workList.add(work);
                workAdapter.notifyDataSetChanged();
                work_view.setVisibility(View.VISIBLE);
            } else {
                Log.d("Firestore", "Document does not exist!");
            }
        });
    }

    private void RemoveWork(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebaseFirestore.collection("Work");

        collectionRef.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", Objects.requireNonNull(e.getMessage()));
                return;
            }
            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.REMOVED) {
                        String id = change.getDocument().getString("id");
                        int index = -1;
                        System.out.println(id);
                        for (int i = 0; i < workList.size(); i++) {
                            if (workList.get(i).getId().equals(id)) {
                                index = i;
                                break;
                            }
                        }
                        System.out.println(index);
                        if (index != -1) {
                            workList.remove(index);
                            workAdapter.notifyItemRemoved(index);
                        }
                    }
                }

            }
        });
    }

    private void UpdateWorkshop(){
        workshopList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Workshop_Register")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {

                            if (document.contains(ic)) {

                                String candidatesID = document.getId();
                                workshop(candidatesID);
                            }
                        }
                        RemoveWorkshop();
                    }
                });

    }
    private void workshop(String id){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firebaseFirestore.collection("Workshop").document(id);

        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", Objects.requireNonNull(e.getMessage()));
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String cover = documentSnapshot.getString("Cover");
                String title = documentSnapshot.getString("Title");
                String date = documentSnapshot.getString("Date");
                String start = documentSnapshot.getString("Start");
                String location = documentSnapshot.getString("Location");
                String publisher = documentSnapshot.getString("publisher");
                Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                Workshop workshop = new Workshop(id,cover,title,date,start,location,publisher,timestamp);
                workshopList.add(workshop);
                int position = workshopList.size() - 1;
                workshopAdapter.notifyItemInserted(position);
            } else {
                Log.d("Firestore", "Document does not exist!");
            }
        });
    }
    private void RemoveWorkshop(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = firebaseFirestore.collection("WorkShop");

        collectionRef.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", Objects.requireNonNull(e.getMessage()));
                return;
            }
            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.REMOVED) {
                        String id = change.getDocument().getString("id");
                        int index = -1;
                        System.out.println(id);
                        for (int i = 0; i < workshopList.size(); i++) {
                            if (workshopList.get(i).getId().equals(id)) {
                                index = i;
                                break;
                            }
                        }
                        System.out.println(index);
                        if (index != -1) {
                            workshopList.remove(index);
                            workshopAdapter.notifyItemRemoved(index);
                        }
                    }
                }

            }
        });
    }

}