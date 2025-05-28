package com.um.linkcamp;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;
import static android.graphics.Color.red;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import Adapter.TutorAdapter;
import data.DatabaseHelper;
import function.VerifyLogin;
import model.Tutor;


public class View_Workshop extends AppCompatActivity {
    private RecyclerView tutor_view;
    private TutorAdapter tutorAdapter;
    private List<Tutor> tutorList;
    DatabaseHelper dbHelper;
    String ic,workshopID;
    String today;
    String workshopTitle,location,description,publisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_workshop);

        dbHelper = new DatabaseHelper(View_Workshop.this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        VerifyLogin verifyLogin = new VerifyLogin(View_Workshop.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                   finish();
                }else if (!("login".equals(result))) {
                    finish();
                }
            });
        }else{
             finish();
            return;
        }
        Cursor cursor = dbHelper.getUserData();
        if(cursor.moveToFirst()){
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        workshopID = intent.get().getStringExtra("WorkshopID");
        if(workshopID.isEmpty()){
            finish();
        }else if (workshopID == null){
            finish();
        }

        TextView txt_title = findViewById(R.id.txt_title);
        TextView txt_location = findViewById(R.id.txt_location);
        TextView txt_date_and_time = findViewById(R.id.date_and_time);
        TextView txt_description = findViewById(R.id.description);
        TextView txt_name = findViewById(R.id.txt_name);
        TextView txt_role = findViewById(R.id.txt_role);
        TextView txt_open = findViewById(R.id.txt_open);

        ImageView profile = findViewById(R.id.profile);
        ImageView img_cover = findViewById(R.id.cover);
        ImageView btnFollow = findViewById(R.id.follow);


        tutor_view = findViewById(R.id.tutor);
        tutor_view.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(View_Workshop.this, LinearLayoutManager.HORIZONTAL, true);
        linearLayoutManager.setStackFromEnd(true);
        tutor_view.setLayoutManager(linearLayoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        tutor_view.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = spacingInPixels;
            }
        });
        tutorList = new ArrayList<>();
        tutorAdapter = new TutorAdapter(View_Workshop.this,tutorList);
        tutor_view.setAdapter(tutorAdapter);

        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnApplicant = findViewById(R.id.btnApplicant);

        btnApplicant.setOnClickListener(v -> {
            checkEmpty();
        });

        ImageView btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference loginRef = db.collection("Workshop").document(workshopID);
        loginRef.addSnapshotListener((documentSnapshot, e)  ->{
            if (e != null) {
                finish();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                workshopTitle = documentSnapshot.getString("Title");
                location = documentSnapshot.getString("Location");
                description = documentSnapshot.getString("Description");
                String date = documentSnapshot.getString("Date");
                String start = documentSnapshot.getString("Start");
                String end = documentSnapshot.getString("End");
                String cover = documentSnapshot.getString("Cover");
                publisher = documentSnapshot.getString("publisher");
                String close = documentSnapshot.getString("Close");

                txt_open.setText("Open Register Until :"+close);
                txt_title.setText(workshopTitle);
                txt_location.setText(location);
                txt_description.setText(description);
                try {
                    byte[] imageBytes = Base64.decode(cover, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    img_cover.setImageBitmap(bitmap);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if(publisher.equals(ic)){
                    btnRegister.setText("Delete");
                    btnRegister.setBackgroundColor(RED);
                    btnFollow.setVisibility(View.GONE);
                    btnApplicant.setVisibility(View.VISIBLE);
                }else{
                    btnApplicant.setVisibility(View.GONE);
                    isFollow(btnFollow);
                    today(close,btnRegister);
                }
                showProfile(profile,txt_name,txt_role);
                tutorList.clear();
                txt_date_and_time.setText(date_format(date,start,end));
                Long  tutor_ = documentSnapshot.getLong("Tutor");

                assert tutor_ != null;
                for (int i = tutor_.intValue(); i > 0 ; i--) {
                    String profile_ = documentSnapshot.getString("Profile"+i);
                    String name_ = documentSnapshot.getString("Name"+i);
                    String major_ = documentSnapshot.getString("Position"+i);
                    Tutor tutor = new Tutor(profile_,name_,major_);
                    tutorList.add(tutor);
                }

                tutorAdapter.notifyDataSetChanged();

            } else {
               finish();
            }
        });

        btnRegister.setOnClickListener(v -> {
            if(publisher.equals(ic)){
                showDialog("Detele","Do you want delete this workshop?",1);
            }else{
                if((btnRegister.getTag()).equals("UnRegister")){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(ic, true);

                    FirebaseFirestore.getInstance()
                            .collection("Workshop_Register")
                            .document(workshopID)
                            .set(hashMap, SetOptions.merge());


                    btnRegister.setText("Unregister");
                    btnRegister.setBackgroundColor(GRAY);
                    btnRegister.setTag("Register");
                }else{
                    FirebaseFirestore.getInstance()
                            .collection("Workshop_Register").document(workshopID)
                            .update(ic, FieldValue.delete());
                    btnRegister.setText("Register");
                    int blue1 = ContextCompat.getColor(View_Workshop.this,R.color.blue1);
                    btnRegister.setBackgroundColor(blue1);
                    btnRegister.setTag("UnRegister");
                }
            }
        });

        btnFollow.setOnClickListener(v -> {

            if((btnFollow.getTag()).equals("Follow")){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(publisher, true);

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(ic)
                        .collection("following")
                        .document(publisher)
                        .set(hashMap, SetOptions.merge());

                HashMap<String, Object> f_hashMap = new HashMap<>();
                f_hashMap.put(ic, true);

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(publisher)
                        .collection("follower")
                        .document(ic)
                        .set(f_hashMap, SetOptions.merge());
                btnFollow.setImageResource(R.drawable.icon_check);
                btnFollow.setColorFilter(ContextCompat.getColor(View_Workshop.this, R.color.grey));
                btnFollow.setTag("Following");
            }else{
                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(publisher)
                        .collection("follower")
                        .document(ic)
                        .delete();

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(ic)
                        .collection("following")
                        .document(publisher)
                        .delete();
                btnFollow.setImageResource(R.drawable.icon_add);
                btnFollow.setColorFilter(ContextCompat.getColor(View_Workshop.this, R.color.blue1));
                btnFollow.setTag("Follow");
            }
        });

        profile.setOnClickListener(v -> {
            intent.set(new Intent(View_Workshop.this, Profile_view.class));
            intent.get().putExtra("UserID",publisher);
           startActivity(intent.get());
        });
        txt_name.setOnClickListener(v -> {
            intent.set(new Intent(View_Workshop.this, Profile_view.class));
            intent.get().putExtra("UserID",publisher);
            startActivity(intent.get());
        });


    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(View_Workshop.this);
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
    private void showDialog(String title,String message,int status){// 1 = delete
        Dialog dialog = new Dialog(View_Workshop.this);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));

        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.cancel);
        Button btnCon = dialog.findViewById(R.id.confirm);

        t.setText(title);
        d.setText(message);
        if(status == 1){
            btnC.setText("Cancel");
            btnC.setBackgroundColor(ContextCompat.getColor(View_Workshop.this, R.color.blue1));
            btnCon.setText("Delete");
            btnCon.setBackgroundColor(ContextCompat.getColor(View_Workshop.this, R.color.red));
        }


        btnC.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnCon.setOnClickListener(v -> {
            if(status == 1){
                deleteWorkshop();
                finish();
            }
        });

        dialog.show();
    }
    private void isFollow(ImageView imageView){
        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("Follow")
                .document(ic);
        CollectionReference followingRef = documentRef.collection("following");
        followingRef.addSnapshotListener((queryDocumentSnapshots,e) -> {
            if(e != null){
                return;
            }

            if (!queryDocumentSnapshots.isEmpty()) {
                boolean isFollowing = queryDocumentSnapshots.getDocuments().stream()
                        .anyMatch(doc -> publisher.equals(doc.getId()));

                if (isFollowing) {
                    imageView.setImageResource(R.drawable.icon_check);
                    imageView.setColorFilter(ContextCompat.getColor(View_Workshop.this, R.color.grey));
                    imageView.setTag("Following");

                } else {
                    imageView.setImageResource(R.drawable.icon_add);
                    imageView.setColorFilter(ContextCompat.getColor(View_Workshop.this, R.color.blue1));
                    imageView.setTag("Follow");
                }
            }else {
                imageView.setImageResource(R.drawable.icon_add);
                imageView.setColorFilter(ContextCompat.getColor(View_Workshop.this, R.color.blue1));
                imageView.setTag("Follow");
            }

        });
    }
    private void showProfile(ImageView profile, TextView name, TextView role){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Users").document(publisher);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
                String profile_ = Snapshot.getString("profile");
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
                role.setText(Snapshot.getString("role"));
            }
        });
    }
    private void deleteWorkshop(){
        FirebaseFirestore.getInstance()
                .collection("Workshop")
                .document(workshopID)
                .delete();
    }

    private void isRegister(Button btn){
        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("Workshop_Register")
                .document(workshopID);

        documentRef.addSnapshotListener((queryDocumentSnapshots,e) -> {
            if(e != null){
                return;
            }

            if (queryDocumentSnapshots != null && queryDocumentSnapshots.exists()) {
                boolean isRegister = Boolean.TRUE.equals(queryDocumentSnapshots.getBoolean(ic));

                if (isRegister) {
                    btn.setText("Unregister");
                    btn.setBackgroundColor(GRAY);
                    btn.setTag("Register");

                } else {
                    btn.setText("Register");
                    int blue1 = ContextCompat.getColor(View_Workshop.this,R.color.blue1);
                    btn.setBackgroundColor(blue1);
                    btn.setTag("UnRegister");
                }
            }else {
                btn.setText("Register");
                int blue1 = ContextCompat.getColor(View_Workshop.this,R.color.blue1);
                btn.setBackgroundColor(blue1);
                btn.setTag("UnRegister");
            }

        });
    }

    private String date_format(String date, String start, String end) {
        String edate,sdate;
        String[] dates = date.split(" - ");
        if (dates.length == 2) {
            sdate = dates[0].trim();
            edate = dates[1].trim();
        } else {
            sdate = date;
            edate = date;
        }
        if(sdate.equals(edate)){
            return start + " - " + end + " " + sdate;
        }else{
            return sdate + " " + start + " - " + edate + " " + end;
        }
    }
    private void today (String date,Button btn){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("serverTime").setValue(ServerValue.TIMESTAMP);
        databaseReference.child("serverTime").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Long serverTimeMillis = task.getResult().getValue(Long.class);
                if (serverTimeMillis != null) {

                    Date serverDate = new Date(serverTimeMillis);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    today = sdf.format(serverDate);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date parsedDate1 = dateFormat.parse(today);
                        Date parsedDate2 = dateFormat.parse(date);
                        if (parsedDate1.compareTo(parsedDate2) > 0) {
                            btn.setText("Close");
                            btn.setBackgroundColor(GRAY);
                            btn.setEnabled(false);
                        } else if (parsedDate1.compareTo(parsedDate2) < 0) {
                            btn.setEnabled(true);
                            isRegister(btn);
                        } else {
                            btn.setEnabled(true);
                            isRegister(btn);

                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Failed to fetch server time.");
            }
        });
    }
    public void checkEmpty() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Workshop_Register")
                .document(workshopID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Check if the document has any data
                        if (task.getResult().exists() && !task.getResult().getData().isEmpty()) {
                            // Document exists and is not empty
                            Intent intent1 = new Intent(View_Workshop.this, Workshop_Applicant.class);
                            intent1.putExtra("WorkshopID",workshopID);
                            intent1.putExtra("WorkshopTitle",workshopTitle);
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
}