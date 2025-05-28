package com.um.linkcamp;

import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapter.RecentConversationsAdapter;
import data.Constants;
import data.DatabaseHelper;
import function.VerifyLogin;
import model.ChatMessage;

public class Chat_Home extends AppCompatActivity {
    DatabaseHelper dbHelper;
    TextView chat,channel;
    String ic,name;
    RecyclerView chatRecycler,channelRecycle;
    ProgressBar progressBar;

    private List<ChatMessage> conversations,channelList;
    private RecentConversationsAdapter conversationsAdapter,channelAdapter;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(Chat_Home.this);
        VerifyLogin verifyLogin = new VerifyLogin(Chat_Home.this);
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
            name = cursor.getString(cursor.getColumnIndex("name"));
        }
        init();

        ImageView btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        TextView txtName = findViewById(R.id.title);
        txtName.setText(name);

        chat = findViewById(R.id.chat);
        channel = findViewById(R.id.channel);

        selected("Chat",chat);

        chat.setOnClickListener(v -> {
            selected("Chat",chat);
            chatRecycler.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            channelRecycle.setVisibility(View.GONE);
            listenConversations();
        });

        channel.setOnClickListener(v -> {
            selected("Channel",channel);
            chatRecycler.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            channelRecycle.setVisibility(View.GONE);
            progressBar = findViewById(R.id.progressBar);
            listenChannelConversations();
        });
        listenConversations();
    }
    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(Chat_Home.this,conversations,ic,0);
        chatRecycler = findViewById(R.id.conversationRecyclerView);
        chatRecycler.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();

        channelList = new ArrayList<>();
        channelAdapter = new RecentConversationsAdapter(Chat_Home.this,channelList,ic,1);
        channelRecycle = findViewById(R.id.channelRecyclerView);
        channelRecycle.setAdapter(channelAdapter);

        progressBar = findViewById(R.id.progressBar);
    }
    private void listenConversations(){
        conversations.clear();
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,ic)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,ic)
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (ic.equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2)-> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            chatRecycler.smoothScrollToPosition(0);
            chatRecycler.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.GONE);
        }
    };

    private void listenChannelConversations(){
        channelList.clear();
        database.collection("Channel")
                        .whereEqualTo(ic,true).addSnapshotListener(((value, error) -> {
                    if (error != null){
                       
                        return;
                    }
                    if (value != null){
                        boolean fieldExists = false;
                        for (DocumentChange documentChange : value.getDocumentChanges()){
                            if(documentChange.getType() == DocumentChange.Type.ADDED){
                                fieldExists = true;
                                String channelID = documentChange.getDocument().getId();
                                database.collection("channelConversations")
                                        .whereEqualTo(Constants.KEY_RECEIVER_ID,channelID)
                                        .addSnapshotListener(eventChannelListener);
                            } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                                fieldExists = true;
                                String channelID = documentChange.getDocument().getId();
                                for (int i = 0; i < channelList.size(); i++) {
                                    if(channelList.get(i).receiverId.equals(channelID)){
                                        channelList.remove(i);
                                        channelAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                            }
                        }
                        if (!fieldExists) {
                            // No documents with the field `ic` set to `true`
                            progressBar.setVisibility(View.GONE);
                            // Optionally inform the user
                        }
                    }
                }));
    }
    private final EventListener<QuerySnapshot> eventChannelListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){

                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                    chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                    chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    if (ic.equals(senderId)){
                        chatMessage.message = "You :" + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    }else{
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME)+" :"
                                + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    }
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    channelList.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < channelList.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        System.out.println(receiverId);
                        if (channelList.get(i).receiverId.equals(receiverId)){
                            if(senderId.equals(ic)){
                                channelList.get(i).message = "You :" + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            }else{
                                channelList.get(i).message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME)+" :"
                                        + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            }
                            channelList.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(channelList,(obj1,obj2)-> obj2.dateObject.compareTo(obj1.dateObject));
            channelAdapter.notifyDataSetChanged();
            channelRecycle.smoothScrollToPosition(0);
            channelRecycle.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.GONE);
        }
    };

    public void selected(String title,TextView textView){
        chat.setText("Chat");
        channel.setText("Channel");

        chat.setTextColor(getResources().getColor(R.color.black, null));
        channel.setTextColor(getResources().getColor(R.color.black, null));

        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(new UnderlineSpan(), 0, title.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(getResources().getColor(R.color.blue1, null));
    }
}