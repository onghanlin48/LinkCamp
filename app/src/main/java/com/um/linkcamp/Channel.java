package com.um.linkcamp;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.um.linkcamp.databinding.ActivityChannelBinding;
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

public class Channel extends AppCompatActivity {
    private ActivityChannelBinding binding;
    private String s_name,ic,channelID,channelName,profile;
    DatabaseHelper dbHelper;
    private FirebaseFirestore database;
    private String conversionId = null;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChannelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.imageInfo.setImageDrawable(ContextCompat.getDrawable(Channel.this, R.drawable.icon_logout));
        dbHelper = new DatabaseHelper(Channel.this);
        VerifyLogin verifyLogin = new VerifyLogin(Channel.this);
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
            s_name = cursor.getString(cursor.getColumnIndex("name"));
        }
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        channelID = intent.get().getStringExtra("channelID");
        channelName = intent.get().getStringExtra("channelName");
        if(channelID.isEmpty()){
            finish();
        }else if (channelID == null){
            finish();
        }
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Channel")
                .whereEqualTo(ic, true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean check = false;
                        for (DocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            System.out.println(documentId);
                            System.out.println(channelID);
                            if (documentId.equals(channelID)) {
                                System.out.println(channelID);
                                check = true;
                                break;
                            }
                        }
                        if (!check) {
                            Dialog_Join();
                        }else{
                            loadReceiverDetails();
                            setListeners();
                            getInfo();
                            checkP();
                        }
                    } else {
                        Dialog_Join();
                    }
                });



    }
    private void checkP(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Learning")
                .document(channelID)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String p = task.getResult().getString("publisher");
                        if(ic.equals(p)){
                            binding.imageInfo.setVisibility(View.INVISIBLE);
                        }else{
                            binding.imageInfo.setOnClickListener(v -> {
                                Dialog_Unjoin();
                            });
                        }
                    }
                });
    }
    private void init(){
        database = FirebaseFirestore.getInstance();
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                profile,
                ic,
                chatMessages,
                1,
                Channel.this
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        listenMessage();
    }
    private void loadReceiverDetails(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("countMessage")
                .whereEqualTo("receiverId", ic)
                .whereEqualTo("senderId", channelID)
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
        binding.textName.setText(channelName);
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,ic);
        message.put(Constants.KEY_RECEIVER_ID,channelID);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,ic);
            conversion.put(Constants.KEY_SENDER_NAME,s_name);
            conversion.put(Constants.KEY_RECEIVER_ID,channelID);
            conversion.put(Constants.KEY_RECEIVER_NAME,channelName);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,profile);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        String body = binding.inputMessage.getText().toString();
        database.collection("Channel")
                .document(channelID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            for (String fieldName : document.getData().keySet()) {
                                Object value = document.get(fieldName);
                                if (value instanceof Boolean && (Boolean) value) {
                                    if(!ic.equals(fieldName)){
                                        count(fieldName);
                                        sendNotification(fieldName,body);
                                    }
                                }
                            }
                        }
                    }
                });


        binding.inputMessage.setText(null);
    }
    private void sendNotification(String userId,String body1){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Users").document(userId);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){

                String body = s_name+" : " + body1;
                System.out.println(body);
                String token = Snapshot.getString("Token");
                if ("new".equals(token)) {
                    HashMap<String,Object> laterSend = new HashMap<>();
                    laterSend.put("senderName",channelName);
                    laterSend.put("receiverId",userId);
                    laterSend.put("message", body);
                    laterSend.put("senderId",channelID);
                    laterSend.put("timestamp", FieldValue.serverTimestamp());
                    laterSend.put("channel","Channel");
                    database.collection("later").add(laterSend);
                }else{
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    String finalBody = body;
                    executor.execute(() -> {
                        ChatNotificationSender.sendNotification(Channel.this,token,channelName, finalBody,channelID,"Channel");
                    });
                }

            }
        });
    }
    private void count(String userId){
        database.collection("countMessage")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("senderId", channelID)
                .get()
                .addOnSuccessListener(snapshot -> {
                    HashMap<String, Object> count = new HashMap<>();
                    if (!snapshot.isEmpty()) {
                        // Assuming only one document matches the query
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);

                        Long countValue = doc.getLong("count"); // Safely get the count field
                        int count2 = (countValue != null) ? countValue.intValue() : 0;

                        count.put("receiverId", userId);
                        count.put("count", count2 + 1);
                        count.put("senderId", channelID);

                        // Update the existing document
                        database.collection("countMessage")
                                .document(doc.getId())
                                .set(count);
                    } else {
                        count.put("receiverId", userId);
                        count.put("count", 1);
                        count.put("senderId", channelID);

                        // Add a new document
                        database.collection("countMessage")
                                .add(count);
                    }
                });
    }
    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection("channelConversations").document(conversionId);
        documentReference.update(
                Constants.KEY_SENDER_ID,ic,
                Constants.KEY_SENDER_NAME,s_name,
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void addConversion(HashMap<String,Object> conversion){
        database.collection("channelConversations")
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    private void getInfo(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Channel").document(channelID);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
                profile = Snapshot.getString("profile");
                init();
            }
        });
    }

    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,channelID)
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
            Collections.sort(chatMessages,(obj1, obj2)->obj1.dateObject.compareTo(obj2.dateObject));
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
    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(channelID);
        }
    }
    private void checkForConversionRemotely(String receiverId){
        database.collection("channelConversations")
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
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void Dialog_Join() {
        Dialog dialog = new Dialog(Channel.this);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));


        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        btnC.setText("Join");

        t.setText("Join Channel");
        d.setText("Do you want join this Channel?");
        btnC.setOnClickListener(v -> {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    firebaseFirestore.collection("Channel").document(channelID);
            documentReference.update(ic,true);
            loadReceiverDetails();
            setListeners();
            getInfo();
            checkP();
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
    private void Dialog_Unjoin() {
        Dialog dialog = new Dialog(Channel.this);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));


        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        btnC.setText("Leave");

        t.setText("Leave Channel");
        d.setText("Do you want leave this channel?");
        btnC.setOnClickListener(v -> {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("Channel")
                    .document(channelID)
                    .update(ic, FieldValue.delete());
            dialog.dismiss();
            finish();
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}