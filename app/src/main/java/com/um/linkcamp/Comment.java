package com.um.linkcamp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import Adapter.CommentApdater;
import data.Constants;
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import model.ChatMessage;
import model.Comment_save;

public class Comment extends AppCompatActivity {
    FirebaseFirestore database;
    DatabaseHelper dbHelper;
    String postId,ic;
    EditText edit_comment;
    ImageView btnSend;
    RecyclerView comment_view;
    List<Comment_save> commentSaveList;
    CommentApdater commentApdater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(Comment.this);
        VerifyLogin verifyLogin = new VerifyLogin(Comment.this);
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
        btnBack.setOnClickListener(v -> finish());

        init();

        btnSend.setOnClickListener(v -> {
            String comment = edit_comment.getText().toString().trim();
            if (comment != null){
                if(!comment.isEmpty()){
                    edit_comment.setText("");
                    upload(comment);
                }
            }
        });

        readComment();
    }
    private void init(){
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        postId = intent.get().getStringExtra("PostId");

        if(postId == null){
            finish();
        } else if (postId.isEmpty()) {
            finish();
        }

        database = FirebaseFirestore.getInstance();

        edit_comment = findViewById(R.id.Comment);
        btnSend = findViewById(R.id.send);

        comment_view = findViewById(R.id.comment_view);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        setupRecyclerView(comment_view,new LinearLayoutManager(this),spacingInPixels);

        commentSaveList = new ArrayList<>();
        commentApdater = new CommentApdater(Comment.this,commentSaveList,ic);

        comment_view.setAdapter(commentApdater);
    }
    private void setupRecyclerView(RecyclerView recyclerView, LinearLayoutManager layoutManager, int spacingInPixels) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
    }
    private void upload(String comment){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("Comment");
        String commentID = postsRef.document().getId();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("publisher", ic);
        hashMap.put("Comment", comment);
        hashMap.put("CommentID", commentID);
        hashMap.put("PostID", postId);
        hashMap.put("timestamp", new Date());

        postsRef.document(commentID).set(hashMap);
    }

    private void readComment(){
        commentSaveList.clear();
        database.collection("Comment")
                .whereEqualTo("PostID",postId)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String commentID = documentChange.getDocument().getString("CommentID");
                    String comment_context = documentChange.getDocument().getString("Comment");
                    String PostID = documentChange.getDocument().getString("PostID");
                    String publisher = documentChange.getDocument().getString("publisher");
                    Date timestamp = documentChange.getDocument().getDate("timestamp");

                    Comment_save commentSave= new Comment_save(commentID,comment_context,PostID,publisher,timestamp);

                   commentSaveList.add(commentSave);
                } else if (documentChange.getType() == DocumentChange.Type.REMOVED){
                    for (int i = 0; i < commentSaveList.size(); i++) {
                        String commentID = documentChange.getDocument().getString("CommentID");
                        if(commentSaveList.get(i).getCommentID().equals(commentID)){
                            commentSaveList.remove(i);
                        }
                    }
                }
            }
            Collections.sort(commentSaveList,(obj1,obj2)-> obj2.getTimeStamp().compareTo(obj1.getTimeStamp()));
            commentApdater.notifyDataSetChanged();
        }
    };
}