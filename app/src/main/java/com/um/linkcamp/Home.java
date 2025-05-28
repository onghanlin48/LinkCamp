package com.um.linkcamp;


import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.Constants;
import data.DatabaseHelper;
import function.ChatNotificationSender;
import function.VerifyLogin;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FloatingActionButton fab;
    private DatabaseHelper dbHelper;
    BottomNavigationView bottomNavigationView;
    String role,name,profile = null,loginDetail,ic;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        dbHelper = new DatabaseHelper(Home.this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        VerifyLogin verifyLogin = new VerifyLogin(Home.this);
        data.register register = data.register.getInstance();
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    register.clear();
                    showDialog("Notice", "You have logged in on another device!");
                    return;
                }else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    register.clear();
                    Intent intent = new Intent(Home.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            });
        }else{
            register.clear();
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndex("role"));
            name = cursor.getString(cursor.getColumnIndex("name"));
            profile = cursor.getString(cursor.getColumnIndex("profile"));
            loginDetail = cursor.getString(cursor.getColumnIndex("login_detail"));
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }
        if(ic == null){
            finish();
            return;
        }

        ImageView btnSearch = findViewById(R.id.search_icon);
        btnSearch.setOnClickListener(v -> {
            EditText edit_search = findViewById(R.id.search_input);
            String search = edit_search.getText().toString().trim();

            if(search != null){
                if(!search.isEmpty()){
                    Intent intent = new Intent(Home.this,Search_.class);
                    intent.putExtra("Search",search);
                    startActivity(intent);
                }
            }
        });

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize NavigationView
        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);


        //call header
        View header = navigationView.getHeaderView(0);
        ImageView headerImage = header.findViewById(R.id.profile);
        TextView following = header.findViewById(R.id.following);
        TextView follower = header.findViewById(R.id.follower);


        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("Follow")
                .document(ic);
        CollectionReference followingRef = documentRef.collection("following");
        followingRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed", e);
                following.setText("Following\n0");
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                int followingCount = queryDocumentSnapshots.size();
                following.setText("Following\n" + followingCount);
            } else {
                following.setText("Following\n0");
            }
        });

        DocumentReference documentfollower = FirebaseFirestore.getInstance()
                .collection("Follow")
                .document(ic);
        CollectionReference followerRef = documentfollower.collection("follower");
        followerRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed", e);
                follower.setText("Follower\n0");
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                int followerCount = queryDocumentSnapshots.size();
                follower.setText("Follower\n" + followerCount);
            } else {
                follower.setText("Follower\n0");
            }
        });

        if (!"skip".equals(profile)) {
            try {
                byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                headerImage.setImageBitmap(bitmap);
                int iconSize = (int) (50 * getResources().getDisplayMetrics().density);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);

                Bitmap circularBitmap = getCircularBitmap(scaledBitmap);

                toolbar.setNavigationIcon(new BitmapDrawable(getResources(), circularBitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Drawable defaultDrawable = getResources().getDrawable(R.drawable.icon_person);
            int iconSize = (int) (50 * getResources().getDisplayMetrics().density);

            Bitmap bitmap = Bitmap.createBitmap(
                    iconSize, iconSize, Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            defaultDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            defaultDrawable.draw(canvas);

            toolbar.setNavigationIcon(new BitmapDrawable(getResources(), bitmap));
        }




        //change name
        TextView txt_name = header.findViewById(R.id.txt_name);
        txt_name.setText(name);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if("User".equals(role)){
                Intent intent = new Intent(Home.this, Posting.class);
                startActivity(intent);
            }else{
                dialog_post(role);
            }

        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.bottom_home){
                openFragment(new Fragment_Home());
                return true;
            }
            if(itemId == R.id.bottom_workshop){
                openFragment(new Fragment_Workshop());
                return true;
            }
            if (itemId == R.id.bottom_learn){
                openFragment(new Fragment_Learn());
                return true;
            }
            if(itemId == R.id.bottom_jod){
                openFragment(new Fragment_Work());
                return true;
            }
            return true;
        });

        fragmentManager = getSupportFragmentManager();
        openFragment(new Fragment_Home());

        ImageView send = findViewById(R.id.send);
        send.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this,Chat_Home.class);
            startActivity(intent);
        });
        getFCMToken();
        if(getIntent().getExtras()!=null){
            String userId = getIntent().getExtras().getString("userId");
            String channel = getIntent().getExtras().getString("channel");
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            assert userId != null;
            if("Channel".equals(channel)){
                DocumentReference Ref = firebaseFirestore.collection("Learning").document(userId);
                Ref.get().addOnSuccessListener(Snapshot -> {
                    if(Snapshot.exists()){
                        String name = Snapshot.getString("title");
                        Intent intent1 = new Intent(Home.this,Channel.class);
                        intent1.putExtra("channelName",name);
                        intent1.putExtra("channelID",userId);
                        startActivity(intent1);
                    }
                });
            }else{
                DocumentReference Ref = firebaseFirestore.collection("Users").document(userId);
                Ref.get().addOnSuccessListener(Snapshot -> {
                    if(Snapshot.exists()){
                        String name = Snapshot.getString("name");
                        Intent intent1 = new Intent(Home.this,Chat.class);
                        intent1.putExtra("name",name);
                        intent1.putExtra("userId",userId);
                        startActivity(intent1);
                    }
                });
            }

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_logout){
            Dialog_Logout("Logout","Do you want to logout?");
            return true;
        }
        if (itemId == R.id.nav_profile){
            Intent intent = new Intent(Home.this, Profile_view.class);
            intent.putExtra("UserID",ic);
            startActivity(intent);
            return true;
        }
        if(itemId == R.id.nav_apply_job){
            Intent intent = new Intent(Home.this, View_Applied.class);
            startActivity(intent);
            return true;
        }
        return true;

    }

    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Home.this);
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
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
    private void Dialog_Logout(String title, String message) {
        Dialog dialog = new Dialog(Home.this);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));


        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);

        t.setText(title);
        d.setText(message);
        btnC.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference = database.collection("Users")
                    .document(ic);
            documentReference.update("Token","new");
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            DocumentReference userRef = db.collection("Login").document(loginDetail);

            userRef.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                dbHelper.clearUserData();
                                userRef.update("status", "new")
                                        .addOnSuccessListener(aVoid -> {

                                            Intent intent = new Intent(Home.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("Firestore", "Error updating status", e);
                                            Intent intent = new Intent(Home.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        });
                            } else {

                                dbHelper.clearUserData();
                                Intent intent = new Intent(Home.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Log.w("Firestore", "Error getting document", task.getException());
                            dbHelper.clearUserData();
                            Intent intent = new Intent(Home.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
    private void openFragment(Fragment fragment){
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw a circle
        Path path = new Path();
        path.addCircle(
                width / 2.0f,
                width / 2.0f,
                width / 2.0f,
                Path.Direction.CCW
        );
        canvas.clipPath(path);

        // Draw the bitmap inside the circle
        canvas.drawBitmap(bitmap, (width - bitmap.getWidth()) / 2.0f, (width - bitmap.getHeight()) / 2.0f, paint);

        return output;
    }

    private void dialog_post(String role){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Home.this);
        View view = LayoutInflater.from(Home.this).inflate(R.layout.post_dialog,null);
        bottomSheetDialog.setContentView(view);

        TextView btnPost = view.findViewById(R.id.post);
        TextView btnMaterial = view.findViewById(R.id.material);
        TextView btnRecruitment = view.findViewById(R.id.recruitment);
        TextView btnWorkshop = view.findViewById(R.id.workshop);

        if("Company".equals(role)){
            btnRecruitment.setVisibility(View.VISIBLE);
        }else{
            btnMaterial.setVisibility(View.VISIBLE);
        }

        btnPost.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(Home.this, Posting.class);
            startActivity(intent);
        });
        btnWorkshop.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(Home.this, Create_Workshop.class);
            startActivity(intent);
        });
        btnRecruitment.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(Home.this, Create_work.class);
            startActivity(intent);
        });
        btnMaterial.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(Home.this, Create_learning.class);
            startActivity(intent);
        });

        Button btnCancel = view.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
    void getFCMToken(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult();
                    sendLaterMessage(token);
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = database.collection("Users")
                            .document(ic);
                    documentReference.update("Token",token);

                });
    }
    void sendLaterMessage(String token){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("later")
                .whereEqualTo("receiverId", ic)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            String name = document.getString("senderName");
                            String ic = document.getString("senderId");
                            String body = document.getString("message");
                            String channel = document.getString("channel");
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                ChatNotificationSender.sendNotification(Home.this,token,name,body,ic,channel);
                                FirebaseFirestore.getInstance()
                                        .collection("later")
                                        .document(documentId)
                                        .delete();
                            });

                        }
                    }
                });
    }
}
