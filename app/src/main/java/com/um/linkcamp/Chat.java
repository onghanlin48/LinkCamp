package com.um.linkcamp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.databinding.ActivityChatBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import Adapter.ChatAdapter;
import data.Constants;
import data.DatabaseHelper;
import function.ChatNotificationSender;
import function.VerifyLogin;
import model.ChatMessage;

public class Chat extends BaseActivity {
    private ActivityChatBinding binding;
    String userID, name,profile,ic,s_name,s_profile,token;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    DatabaseHelper dbHelper;
    private String conversionId = null;
    private Boolean isReceiverAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(Chat.this);
        VerifyLogin verifyLogin = new VerifyLogin(Chat.this);
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
        binding.imageInfo.setVisibility(View.INVISIBLE);
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
            s_name = cursor.getString(cursor.getColumnIndex("name"));
            s_profile = cursor.getString(cursor.getColumnIndex("profile"));
        }

        loadReceiverDetails();

        setListeners();
        getInfo();
    }
    private void loadReceiverDetails(){
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        userID = intent.get().getStringExtra("userId");
        name = intent.get().getStringExtra("name");
        if(userID.isEmpty()){
            finish();
        }else if (userID == null){
            finish();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("countMessage")
                .whereEqualTo("receiverId", ic)
                .whereEqualTo("senderId", userID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            String documentId = documentSnapshot.getId();
                            db.collection("countMessage")
                                    .document(documentId)
                                    .delete();
                        }
                    }
                });
        binding.textName.setText(name);

    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
    private void init(){
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                profile,
                ic,
                chatMessages,
                0,
                Chat.this
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,ic);
        message.put(Constants.KEY_RECEIVER_ID,userID);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,ic);
            conversion.put(Constants.KEY_SENDER_NAME,s_name);
            conversion.put(Constants.KEY_SENDER_IMAGE,s_profile);
            conversion.put(Constants.KEY_RECEIVER_ID,userID);
            conversion.put(Constants.KEY_RECEIVER_NAME,name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,profile);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if(!isReceiverAvailable) {
            database.collection("countMessage")
                    .whereEqualTo("receiverId", userID)
                    .whereEqualTo("senderId", ic)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        HashMap<String, Object> count = new HashMap<>();
                        if (!snapshot.isEmpty()) {
                            // Assuming only one document matches the query
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);

                            Long countValue = doc.getLong("count"); // Safely get the count field
                            int count2 = (countValue != null) ? countValue.intValue() : 0;

                            count.put("receiverId", userID);
                            count.put("count", count2 + 1);
                            count.put("senderId", ic);

                            // Update the existing document
                            database.collection("countMessage")
                                    .document(doc.getId())
                                    .set(count);
                        } else {
                            count.put("receiverId", userID);
                            count.put("count", 1);
                            count.put("senderId", ic);

                            // Add a new document
                            database.collection("countMessage")
                                    .add(count);
                        }
                    });
            String body = binding.inputMessage.getText().toString();
            if ("new".equals(token)) {
                HashMap<String,Object> laterSend = new HashMap<>();
                laterSend.put("senderName",s_name);
                laterSend.put("receiverId",userID);
                laterSend.put("message",body);
                laterSend.put("senderId",ic);
                laterSend.put("timestamp", FieldValue.serverTimestamp());
                laterSend.put("channel","Channel");
                database.collection("later").add(laterSend);
            }else{
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ChatNotificationSender.sendNotification(Chat.this,token,s_name,body,ic,"Chat");
                });
            }
        }
        binding.inputMessage.setText(null);
    }
    private void listenAvailabilityOfReceiver(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("Users").document(userID)
                .addSnapshotListener(Chat.this,((value, error) -> {
                    if(error != null){
                        return;
                    }
                    if (value != null){
                        if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                            int available = Objects.requireNonNull(
                                    value.getLong(Constants.KEY_AVAILABILITY)
                            ).intValue();
                            isReceiverAvailable = available == 1;
                        }
                    }
                    if (isReceiverAvailable){
                        binding.textAvailability.setVisibility(View.VISIBLE);
                    }else{
                        binding.textAvailability.setVisibility(View.GONE);
                    }

                }));
    }
    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,ic)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,userID)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,userID)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,ic)
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            System.err.println("Error listening for changes: " + error.getMessage());
            return;
        }
        if(value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2)->obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeChanged(chatMessages.size(),chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    };
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void getInfo(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Users").document(userID);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
                profile = Snapshot.getString("profile");
                token = Snapshot.getString("Token");
                init();
                listenMessage();
            }
        });
    }
    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    userID,
                    ic
            );
            checkForConversionRemotely(
                    ic,
                    userID
            );
        }
    }
    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}