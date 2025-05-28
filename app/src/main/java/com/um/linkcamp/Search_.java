package com.um.linkcamp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import Adapter.LearnAdapter;
import Adapter.PostAdapter;
import Adapter.UserAdapter;
import Adapter.WorkAdapter;
import Adapter.WorkshopAdapter;
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import model.Learn;
import model.Post;
import model.User;
import model.Work;
import model.Workshop;

public class Search_ extends AppCompatActivity {
    String search;
    DatabaseHelper dbHelper;
    RecyclerView user_view,post_view,learn_view,work_view,workshop_view;

    TextView user,post,learn,work,workshop;
    ProgressBar progressBar;
    String txtUser = "User";
    String txtPost = "Post";
    String txtWork = "Work";
    String txtLearn = "Learn";
    String txtWorkshop = "Workshop";
    String ic;

    List<User> userList;
    List<Post> postList;
    List<Learn> learnList;
    List<Work> workList;
    List<Workshop> workshopList;

    PostAdapter postAdapter;
    WorkAdapter workAdapter;
    WorkshopAdapter workshopAdapter;
    LearnAdapter learnAdapter;
    UserAdapter userAdapter;
    FirebaseFirestore database;
    EditText edit_search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(Search_.this);
        VerifyLogin verifyLogin = new VerifyLogin(Search_.this);
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

        ImageView btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        ImageView btnSearch = findViewById(R.id.search_icon);
        btnSearch.setOnClickListener(v -> {
            String search = edit_search.getText().toString().trim();

            if(search != null){
                if(!search.isEmpty()){
                    Intent intent = new Intent(Search_.this,Search_.class);
                    intent.putExtra("Search",search);
                    startActivity(intent);
                    finish();
                }
            }
        });

        init();


        user.setOnClickListener(v -> {
            setGone();
            selected(txtUser,user);
            User();
        });
        post.setOnClickListener(v -> {
            setGone();
            selected(txtPost,post);
            Post();
        });
        learn.setOnClickListener(v -> {
            setGone();
            selected(txtLearn,learn);
            Learn();
        });
        work.setOnClickListener(v -> {
            setGone();
            selected(txtWork,work);
            Work();
        });

        workshop.setOnClickListener(v -> {
            setGone();
            selected(txtWorkshop,workshop);
            Workshop();
        });
    }
    private void init(){
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        search = intent.get().getStringExtra("Search");

        if(search == null){
            finish();
        }else if (search.isEmpty()){
            finish();
        }
        edit_search = findViewById(R.id.search_input);
        edit_search.setText(search);

        search = search.toLowerCase();

        user = findViewById(R.id.user);
        post = findViewById(R.id.post);
        work =findViewById(R.id.work);
        learn = findViewById(R.id.learn);
        workshop = findViewById(R.id.workshop);

        progressBar = findViewById(R.id.progressBar);

        user_view = findViewById(R.id.user_view);
        post_view = findViewById(R.id.post_view);
        learn_view = findViewById(R.id.learn_view);
        work_view = findViewById(R.id.work_view);
        workshop_view = findViewById(R.id.workshop_view);

        userList = new ArrayList<>();
        postList = new ArrayList<>();
        learnList = new ArrayList<>();
        workList = new ArrayList<>();
        workshopList = new ArrayList<>();

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        setupRecyclerView(user_view,new LinearLayoutManager(this),spacingInPixels);
        setupRecyclerView(post_view,new LinearLayoutManager(this),spacingInPixels);
        setupRecyclerView(learn_view,new LinearLayoutManager(this),spacingInPixels);
        setupRecyclerView(work_view,new LinearLayoutManager(this),spacingInPixels);
        setupRecyclerView(workshop_view,new LinearLayoutManager(this),spacingInPixels);

        userAdapter = new UserAdapter(userList, Search_.this,ic);
        postAdapter = new PostAdapter(Search_.this,postList);
        learnAdapter = new LearnAdapter(Search_.this,learnList,ic);
        workAdapter = new WorkAdapter(Search_.this,workList);
        workshopAdapter = new WorkshopAdapter(Search_.this,workshopList);

        user_view.setAdapter(userAdapter);
        post_view.setAdapter(postAdapter);
        learn_view.setAdapter(learnAdapter);
        work_view.setAdapter(workAdapter);
        workshop_view.setAdapter(workshopAdapter);

        database = FirebaseFirestore.getInstance();

        setGone();
        User();
    }
    public void selected(String title,TextView textView){
        user.setText(txtUser);
        post.setText(txtPost);
        learn.setText(txtLearn);
        work.setText(txtWork);
        workshop.setText(txtWorkshop);

        user.setTextColor(getResources().getColor(R.color.black, null));
        post.setTextColor(getResources().getColor(R.color.black, null));
        learn.setTextColor(getResources().getColor(R.color.black, null));
        work.setTextColor(getResources().getColor(R.color.black, null));
        workshop.setTextColor(getResources().getColor(R.color.black, null));

        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(new UnderlineSpan(), 0, title.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(getResources().getColor(R.color.blue1, null));
    }
    private void setGone(){
        user_view.setVisibility(View.GONE);
        post_view.setVisibility(View.GONE);
        learn_view.setVisibility(View.GONE);
        work_view.setVisibility(View.GONE);
        workshop_view.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView(RecyclerView recyclerView, LinearLayoutManager layoutManager, int spacingInPixels) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        recyclerView.setVisibility(View.GONE);
    }

    private void User(){
        userList.clear();
        database.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String d_name = name.toLowerCase();
                        if (d_name.contains(search)) {
                            String id = document.getString("ic");
                            String role = document.getString("role");
                            String profile = document.getString("profile");

                            User user = new User(id,name,role,profile);
                            userList.add(user);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    user_view.setVisibility(View.VISIBLE);
                });
    }
    private void Post(){
        postList.clear();
        database.collection("Posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String description = document.getString("description");
                        String lowerCase = description.toLowerCase();
                        if (lowerCase.contains(search)) {
                            Post post = document.toObject(Post.class);
                            postList.add(post);
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    post_view.setVisibility(View.VISIBLE);
                });
    }
    private void Learn(){
        learnList.clear();
        database.collection("Learning")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String l_title = title.toLowerCase();
                        String l_description = description.toLowerCase();
                        if (search != null) {
                            if(l_title.contains(search) || l_description.contains(search)){
                                String id = document.getString("id");
                                Boolean channel = document.getBoolean("channel");
                                Timestamp timestamp = document.getTimestamp("timestamp");
                                String publisher = document.getString("publisher");

                                Learn learn = new Learn(id,title,description,publisher,channel,timestamp);

                                learnList.add(learn);
                            }
                        }
                    }
                    learnAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    learn_view.setVisibility(View.VISIBLE);
                });
    }
    private void Work(){
        workList.clear();
        database.collection("Work")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("job_title");
                        String description = document.getString("description");
                        String requirement = document.getString("requirement");
                        String l_title = title.toLowerCase();
                        description = description.toLowerCase();
                        requirement = requirement.toLowerCase();
                        if (search != null) {
                            if(l_title.contains(search) || description.contains(search) || requirement.contains(search)){
                                String id = document.getString("id");
                                String location = document.getString("location");
                                String minimum = document.getString("minimum");
                                String maximum = document.getString("maximum");
                                String pay = document.getString("pay");
                                String job_title = document.getString("job_title");
                                String publisher = document.getString("publisher");
                                Timestamp timestamp = document.getTimestamp("timestamp");
                                String salary;
                                if(maximum == null){
                                    salary = "RM "+minimum + " / " + pay;
                                }else if(maximum.isEmpty()){
                                    salary = "RM "+minimum + " / " + pay;
                                }else{
                                    salary ="RM "+ minimum + " - " + maximum + " / " + pay;

                                }

                                Work work = new Work(id,title,job_title,salary,publisher,location,timestamp);
                                workList.add(work);
                            }
                        }
                    }
                    workAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    work_view.setVisibility(View.VISIBLE);
                });
    }
    private void Workshop(){
        workshopList.clear();
        database.collection("Workshop")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("Title");
                        String description = document.getString("Description");
                        String l_title = title.toLowerCase();
                        String l_description = description.toLowerCase();
                        if (search != null) {
                            if(l_title.contains(search) || l_description.contains(search)){
                                String id = document.getString("id");
                                String cover = document.getString("Cover");
                                String date = document.getString("Date");
                                String start = document.getString("Start");
                                String location = document.getString("Location");
                                String publisher = document.getString("publisher");

                                Workshop workshop = new Workshop(id,cover,title,date,start,location,publisher);

                                workshopList.add(workshop);
                            }
                        }
                    }
                    workshopAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    workshop_view.setVisibility(View.VISIBLE);
                });
    }
}