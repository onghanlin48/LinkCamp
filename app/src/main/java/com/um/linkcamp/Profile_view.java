package com.um.linkcamp;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;

import static function.function.sendmail;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;


import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import Adapter.LearnAdapter;
import Adapter.PostAdapter;
import Adapter.WorkAdapter;
import Adapter.WorkshopAdapter;
import data.DatabaseHelper;
import function.DecimalDigitsInputFilter;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import model.Learn;
import model.Post;
import model.Work;
import model.Workshop;

public class Profile_view extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    String pay;
    String ic = null,userID = null,name=null,email_user = null,email_user_ = null;
    RecyclerView item_view,learn_view,work_view,workshop_view;
    ArrayList<Post> postList;
    ArrayList<Workshop> workshopList;
    ArrayList<Learn> learnList;
    ArrayList<Work> workList;
    PostAdapter postAdapter;
    WorkshopAdapter workshopAdapter;
    LearnAdapter learnAdapter;
    WorkAdapter workAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(Profile_view.this);
        VerifyLogin verifyLogin = new VerifyLogin(Profile_view.this);
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
            email_user = cursor.getString(cursor.getColumnIndex("email"));
        }
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        userID = intent.get().getStringExtra("UserID");
        if(userID.isEmpty()){
            finish();
        }else if (userID == null){
            finish();
        }


        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);


        TextView txt_username = findViewById(R.id.username);
        TextView txt_name = findViewById(R.id.txt_name);
        TextView txt_follower = findViewById(R.id.follower);
        TextView txt_following = findViewById(R.id.following);
        TextView txt_role = findViewById(R.id.txt_role);
        TextView txt_description = findViewById(R.id.description);

        ImageView post_item = findViewById(R.id.post_item);
        ImageView learn_item = findViewById(R.id.learn_item);
        ImageView work_item = findViewById(R.id.work_item);
        ImageView workshop_item = findViewById(R.id.workshop_item);
        ImageView imgProfile = findViewById(R.id.profile_image);
        ImageView btnBack = findViewById(R.id.back);

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(Profile_view.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);

        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(Profile_view.this);
        itemLayoutManager.setReverseLayout(true);
        itemLayoutManager.setStackFromEnd(true);


        item_view = findViewById(R.id.item_view);
        learn_view = findViewById(R.id.learn_view);
        work_view = findViewById(R.id.work_view);
        workshop_view = findViewById(R.id.workshop_view);

        setupRecyclerView(item_view, new LinearLayoutManager(this), spacingInPixels);
        setupRecyclerView(learn_view, new LinearLayoutManager(this), spacingInPixels);
        setupRecyclerView(work_view, new LinearLayoutManager(this), spacingInPixels);
        setupRecyclerView(workshop_view, new LinearLayoutManager(this), spacingInPixels);

        item_view.setVisibility(View.VISIBLE);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(Profile_view.this,postList);

        item_view.setAdapter(postAdapter);

        workshopList = new ArrayList<>();
        workshopAdapter = new WorkshopAdapter(Profile_view.this,workshopList);

        workshop_view.setAdapter(workshopAdapter);

        learnList = new ArrayList<>();
        learnAdapter = new LearnAdapter(Profile_view.this,learnList,ic);

        learn_view.setAdapter(learnAdapter);

        workList = new ArrayList<>();
        workAdapter = new WorkAdapter(Profile_view.this,workList);

        work_view.setAdapter(workAdapter);

        UpdatePost();
        CardView cardView =findViewById(R.id.send_message);

        post_item.setOnClickListener(v -> {
            showRecyclerView(item_view,post_item);

            UpdatePost();
        });
        workshop_item.setOnClickListener(v -> {
            showRecyclerView(workshop_view,workshop_item);

            UpdateWorkshop();
        });
        learn_item.setOnClickListener(v -> {
            showRecyclerView(learn_view,learn_item);
            UpdateLearn();
        });
        work_item.setOnClickListener(v -> {
            showRecyclerView(work_view,work_item);
            UpdateWork();
        });


        if(check(userID,ic)){
            btn1.setText("Edit Profile");
            btn1.setBackgroundColor(GRAY);
            btn1.setOnClickListener(v -> {
                Intent intent1 = new Intent(Profile_view.this,Edit_profile.class);
                startActivity(intent1);
                finish();
            });
            btn2.setOnClickListener(v -> {
                Intent intent1 = new Intent(Profile_view.this,View_Donation.class);
                startActivity(intent1);
            });
            cardView.setVisibility(View.GONE);
        }else{
            isFollow(userID,ic,btn1);
            btn1.setOnClickListener(v -> {
                if((btn1.getTag()).equals("Follow")){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(userID, true);

                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(ic)
                            .collection("following")
                            .document(userID)
                            .set(hashMap, SetOptions.merge());

                    HashMap<String, Object> f_hashMap = new HashMap<>();
                    f_hashMap.put(ic, true);

                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(userID)
                            .collection("follower")
                            .document(ic)
                            .set(f_hashMap, SetOptions.merge());
                    btn1.setText("Following");
                    btn1.setBackgroundColor(ContextCompat.getColor(Profile_view.this, R.color.grey));
                    btn1.setTag("Following");
                }else{
                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(userID)
                            .collection("follower")
                            .document(ic)
                            .delete();

                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(ic)
                            .collection("following")
                            .document(userID)
                            .delete();
                    btn1.setText("Follow");
                    btn1.setBackgroundColor(ContextCompat.getColor(Profile_view.this, R.color.blue1));
                    btn1.setTag("Follow");
                }
            });
            btn2.setOnClickListener(v -> {
                dialog_donation();
            });
        }
        cardView.setOnClickListener(v -> {
            Intent intent1 = new Intent(Profile_view.this,Chat.class);
            intent1.putExtra("userId",userID);
            intent1.putExtra("name",name);
            startActivity(intent1);
        });
        ImageView send = findViewById(R.id.send);
        send.setOnClickListener(v -> {
            Intent intent1 = new Intent(Profile_view.this,Chat.class);
            intent1.putExtra("name",name);
            intent1.putExtra("userId",userID);
            startActivity(intent1);
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").whereEqualTo("ic",userID).get()
                .addOnSuccessListener(querySnapshot ->{
                    if(querySnapshot.isEmpty()){
                        finish();
                    }else{
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            name = document.getString("name");
                            email_user_ = document.getString("email");
                            String role = document.getString("role");
                            String description = document.getString("description");
                            String profile = document.getString("profile");
                            int status = Objects.requireNonNull(document.getLong("status")).intValue();
                            ImageView img_status = findViewById(R.id.status);
                            if(status == 1){
                                img_status.setImageResource(R.drawable.pending);
                            } else if (status == 2) {
                                img_status.setImageResource(R.drawable.acept);
                            }else{
                                img_status.setImageResource(R.drawable.block);
                            }
                            followN(txt_following,txt_follower);
                            txt_username.setText(name);
                            txt_name.setText(name);
                            txt_role.setText(role);

                            if(description == null){
                                txt_description.setText(null);
                            }else if(description.equals("null")){
                                txt_description.setText(null);
                            } else{
                                txt_description.setText(description);
                            }
                            if(!"skip".equals(profile)){
                                try {
                                    byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    imgProfile.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if("Company".equals(role)){
                                workshop_item.setVisibility(View.VISIBLE);
                                work_item.setVisibility(View.VISIBLE);
                            } else if ("Lecturer & Teacher".equals(role)) {
                                workshop_item.setVisibility(View.VISIBLE);
                                learn_item.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(Profile_view.this);
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
    private void setupRecyclerView(RecyclerView recyclerView, LinearLayoutManager layoutManager, int spacingInPixels) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        recyclerView.setVisibility(View.GONE);
    }
    private void showRecyclerView(RecyclerView recyclerView,ImageView imageView) {
        item_view.setVisibility(View.GONE);
        learn_view.setVisibility(View.GONE);
        work_view.setVisibility(View.GONE);
        workshop_view.setVisibility(View.GONE);

        ImageView post_item = findViewById(R.id.post_item);
        ImageView learn_item = findViewById(R.id.learn_item);
        ImageView work_item = findViewById(R.id.work_item);
        ImageView workshop_item = findViewById(R.id.workshop_item);

        post_item.setColorFilter(ContextCompat.getColor(Profile_view.this, R.color.black));
        learn_item.setColorFilter(ContextCompat.getColor(Profile_view.this, R.color.black));
        work_item.setColorFilter(ContextCompat.getColor(Profile_view.this, R.color.black));
        workshop_item.setColorFilter(ContextCompat.getColor(Profile_view.this, R.color.black));


        imageView.setColorFilter(ContextCompat.getColor(Profile_view.this, R.color.blue1));

        recyclerView.setVisibility(View.VISIBLE);
    }
    private void dialog_donation(){
        Dialog dialog = new Dialog(Profile_view.this);
        dialog.setContentView(R.layout.donation_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));

        dialog.setCancelable(false);

        Button btnC = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        TextInputEditText amount = dialog.findViewById(R.id.content);
        amount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        btnC.setOnClickListener(v -> {
            String amount_ = Objects.requireNonNull(amount.getText()).toString().trim();
            TextInputLayout l_amount = dialog.findViewById(R.id.amount);
            if (amount_ == null){
                l_amount.setHelperTextColor(ColorStateList.valueOf(RED));
                l_amount.setHelperText("Please fill in Amount (RM)");
            } else if (amount_.isEmpty()) {
                l_amount.setHelperTextColor(ColorStateList.valueOf(RED));
                l_amount.setHelperText("Please fill in Amount (RM)");
            }else{
                l_amount.setHelperTextColor(ColorStateList.valueOf(BLACK));
                l_amount.setHelperText("");
                dialog.dismiss();
                payment(amount_);

            }
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
    private void payment(String amount_){
        Dialog dialog = new Dialog(Profile_view.this);
        dialog.setContentView(R.layout.dialog_payment);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
        dialog.setCancelable(false);

        Button confirm = dialog.findViewById(R.id.confirm);

        TextInputEditText txt_card = dialog.findViewById(R.id.card_number);
        TextInputEditText txt_date = dialog.findViewById(R.id.date);
        TextInputEditText txt_cvv = dialog.findViewById(R.id.cvv);
        TextInputEditText txt_name = dialog.findViewById(R.id.name);


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AutoCompleteTextView autoCompleteTextView = dialog.findViewById(R.id.auto_complete_pay_cycle);
        List<String> Pay = new ArrayList<>();

        db.collection("Payment")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Pay.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String type = document.getString("Payment");
                        if (type != null) {
                            Pay.add(type);
                        }
                    }
                });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Pay);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) ->{
            LinearLayout card = dialog.findViewById(R.id.card);
            pay = adapter.getItem(position);
            if("Online".equals(pay)){
                card.setVisibility(View.GONE);
            }else{
                card.setVisibility(View.VISIBLE);
            }
        });

        confirm.setOnClickListener(v -> {

            if("Online".equals(pay)){
                submit(amount_);
                dialog.dismiss();
            }else {
                String number = Objects.requireNonNull(txt_card.getText()).toString().trim();
                String date = Objects.requireNonNull(txt_date.getText()).toString().trim();
                String cvv = Objects.requireNonNull(txt_cvv.getText()).toString().trim();
                String name = Objects.requireNonNull(txt_name.getText()).toString().trim();

                TextInputLayout l_number = dialog.findViewById(R.id.l_card_number);
                TextInputLayout l_date = dialog.findViewById(R.id.l_date);
                TextInputLayout l_cvv = dialog.findViewById(R.id.l_cvv);
                TextInputLayout l_name = dialog.findViewById(R.id.l_name);


                boolean check = true;

                if(number == null){
                    check = false;
                    l_number.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_number.setHelperText("Please fill in Card number!");
                } else if (number.isEmpty()) {
                    check = false;
                    l_number.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_number.setHelperText("Please fill in Card number!");
                }else{
                    if(number.length() >= 13 && number.length() <= 19){
                        l_number.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                        l_number.setHelperText(" ");
                    }else{
                        check = false;
                        l_number.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        l_number.setHelperText("Card Number between 13 digit and 19 digit!");
                    }
                }

                if(date == null){
                    check = false;
                    l_date.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_date.setHelperText("Please fill in Expiry Date (MMYY)!");
                } else if (date.isEmpty()) {
                    check = false;
                    l_date.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_date.setHelperText("Please fill in Expiry Date (MMYY)!");
                }else{
                    if(date.length() != 4){
                        check = false;
                        l_date.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        l_date.setHelperText("Expiry Date format error!");
                    }else{
                        l_date.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                        l_date.setHelperText(" ");
                    }
                }

                if(cvv == null){
                    check = false;
                    l_cvv.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_cvv.setHelperText("Please fill in CVV!");
                } else if (cvv.isEmpty()) {
                    check = false;
                    l_cvv.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_cvv.setHelperText("Please fill in CVV!");
                }else{
                    if(cvv.length() != 3){
                        check = false;
                        l_cvv.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                        l_cvv.setHelperText("CVV have 3 digit number!");
                    }else{
                        l_cvv.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                        l_cvv.setHelperText(" ");
                    }
                }

                if(name == null){
                    check = false;
                    l_name.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_name.setHelperText("Please fill Name on Card");
                } else if (name.isEmpty()) {
                    check = false;
                    l_name.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    l_name.setHelperText("Please fill Name on Card");
                }else{
                    l_name.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                    l_name.setHelperText(" ");
                }

                if(check){
                    submit(amount_);
                }

            }
        });

        Button cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });


        dialog.show();
    }
    private void submit(String amount_){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("Donation");
        String donationID = postsRef.document().getId();

        try {
            data.gui gui = new data.gui();
            sendmail(email_user,gui.d_t_r, gui.donation_content_receive(amount_,name,donationID));
            sendmail(email_user,gui.d_t, gui.donation_content(amount_,name,donationID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("Send", ic);
        hashMap.put("To", userID);
        hashMap.put("Amount", amount_);
        hashMap.put("timestamp", new Date());
        hashMap.put("id", donationID);

        postsRef.document(donationID).set(hashMap)
                .addOnSuccessListener(aVoid -> {
                    showDialog("Success", "Successfully Donation!");
                })
                .addOnFailureListener(e -> {
                    showDialog("Failed", "Please contact admin!");
                });
    }
    private void followN(TextView txt_following,TextView txt_follower){
        DocumentReference documentRef = FirebaseFirestore.getInstance()
                .collection("Follow")
                .document(userID);
        CollectionReference followingRef = documentRef.collection("following");
        followingRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed", e);
                txt_following.setText("0");
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                int followingCount = queryDocumentSnapshots.size();
                txt_following.setText(""+followingCount);
            } else {
                txt_following.setText("0");
            }
        });

        DocumentReference documentfollower = FirebaseFirestore.getInstance()
                .collection("Follow")
                .document(userID);
        CollectionReference followerRef = documentfollower.collection("follower");
        followerRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed", e);
                txt_follower.setText("0");
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                int followerCount = queryDocumentSnapshots.size();
                txt_follower.setText(""+followerCount);
            } else {
                txt_follower.setText("0");
            }
        });
    }
    private Boolean check(String x,String y){
        return x.equals(y);
    }
    private void isFollow(String userId,String ic,Button button){

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
                        .anyMatch(doc -> userId.equals(doc.getId()));

                if (isFollowing) {
                    button.setText("Following");
                    button.setBackgroundColor(ContextCompat.getColor(Profile_view.this, R.color.grey));
                    button.setTag("Following");

                } else {
                    button.setText("Follow");
                    button.setBackgroundColor(ContextCompat.getColor(Profile_view.this, R.color.blue1));
                    button.setTag("Follow");
                }
            }else {
                button.setText("Follow");
                button.setBackgroundColor(ContextCompat.getColor(Profile_view.this, R.color.blue1));
                button.setTag("Follow");
            }

        });

    }

    private void UpdatePost(){
        RemovePost();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firebaseFirestore.collection("Posts");
        ref.whereEqualTo("publisher", userID).get().addOnCompleteListener(task -> {
            postList.clear();

            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Post post = document.toObject(Post.class);
                    postList.add(post);
                }

                postAdapter.notifyDataSetChanged();
            }
        });
    }
    private void RemovePost(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Posts");
        Ref.whereEqualTo("publisher", userID).addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", Objects.requireNonNull(e.getMessage()));
                return;
            }
            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.REMOVED) {

                        Post postToRemove = change.getDocument().toObject(Post.class);
                        int index = -1;
                        for (int i = 0; i < postList.size(); i++) {
                            if (postList.get(i).getPostId().equals(postToRemove.getPostId())) {
                                index = i;
                                break;
                            }
                        }

                        if (index != -1) {
                            postList.remove(index);
                            postAdapter.notifyItemRemoved(index);
                        }
                    }else if (change.getType() == DocumentChange.Type.ADDED) {
                        Post post = change.getDocument().toObject(Post.class);
                        postList.add(post);
                        int position = postList.size() - 1;
                        postAdapter.notifyItemInserted(position);
                    }
                }
            }
        });
    }
    private void UpdateWorkshop(){
        RemoveWorkshop();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firebaseFirestore.collection("Workshop");
        ref.whereEqualTo("publisher",userID).get().addOnCompleteListener(task -> {
            workshopList.clear();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getString("id");
                    String cover = document.getString("Cover");
                    String title = document.getString("Title");
                    String date = document.getString("Date");
                    String start = document.getString("Start");
                    String location = document.getString("Location");
                    String publisher = document.getString("publisher");

                    Workshop workshop = new Workshop(id,cover,title,date,start,location,publisher);

                    workshopList.add(workshop);

                }
                workshopAdapter.notifyDataSetChanged();
            }
        });
    }
    private void RemoveWorkshop(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Workshop");
        Ref.whereEqualTo("publisher", userID).addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", Objects.requireNonNull(e.getMessage()));
                return;
            }
            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.REMOVED) {
                        String id = change.getDocument().getString("id");
                        int index = -1;
                        for (int i = 0; i < workshopList.size(); i++) {
                            if (workshopList.get(i).getId().equals(id)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            workshopList.remove(index);
                            workshopAdapter.notifyItemRemoved(index);
                        }
                    }else if (change.getType() == DocumentChange.Type.ADDED) {
                        String id = change.getDocument().getString("id");
                        String cover = change.getDocument().getString("Cover");
                        String title = change.getDocument().getString("Title");
                        String date = change.getDocument().getString("Date");
                        String start = change.getDocument().getString("Start");
                        String location = change.getDocument().getString("Location");
                        String publisher = change.getDocument().getString("publisher");
                        Timestamp timestamp = change.getDocument().getTimestamp("timestamp");

                        Workshop workshop = new Workshop(id,cover,title,date,start,location,publisher,timestamp);
                        workshopList.add(workshop);
                        workshopAdapter.notifyDataSetChanged();
                    }
                }

            }
        });
    }
    private void UpdateLearn(){
        RemoveLearn();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firebaseFirestore.collection("Learning");
        ref.whereEqualTo("publisher",userID).get().addOnCompleteListener(task -> {
            learnList.clear();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getString("id");
                    String title = document.getString("title");
                    String description = document.getString("description");
                    Boolean channel = document.getBoolean("channel");
                    Timestamp timestamp = document.getTimestamp("timestamp");
                    String publisher = document.getString("publisher");

                    Learn learn = new Learn(id,title,description,publisher,channel,timestamp);

                    learnList.add(learn);
                }

                learnAdapter.notifyDataSetChanged();
            }
        });
    }
    private void RemoveLearn(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Learning");
        Ref.whereEqualTo("publisher", userID).addSnapshotListener((querySnapshot, e) -> {
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
                        for (int i = 0; i < learnList.size(); i++) {
                            if (learnList.get(i).getId().equals(id)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            learnList.remove(index);
                            learnAdapter.notifyItemRemoved(index);
                        }
                    }else if (change.getType() == DocumentChange.Type.ADDED) {
                        String id = change.getDocument().getString("id");
                        String title = change.getDocument().getString("title");
                        String description = change.getDocument().getString("description");
                        Boolean channel = change.getDocument().getBoolean("channel");
                        Timestamp timestamp = change.getDocument().getTimestamp("timestamp");
                        String publisher = change.getDocument().getString("publisher");
                        Learn learn = new Learn(id,title,description,publisher,channel,timestamp);
                        learnList.add(learn);
                        int position = learnList.size() - 1;
                        learnAdapter.notifyItemInserted(position);
                    }
                }

            }
        });
    }
    private void UpdateWork(){
        RemoveWork();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firebaseFirestore.collection("Work");
        ref.whereEqualTo("publisher",userID).get().addOnCompleteListener(task -> {
            workList.clear();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getString("id");
                    String title = document.getString("title");
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

                workAdapter.notifyDataSetChanged();
            }
        });
    }
    private void RemoveWork(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Work");
        Ref.whereEqualTo("publisher", userID).addSnapshotListener((querySnapshot, e) -> {
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
                    }else if (change.getType() == DocumentChange.Type.ADDED) {
                        String id = change.getDocument().getString("id");
                        String title = change.getDocument().getString("title");
                        String location = change.getDocument().getString("location");
                        String minimum = change.getDocument().getString("minimum");
                        String maximum = change.getDocument().getString("maximum");
                        String pay = change.getDocument().getString("pay");
                        String job_title = change.getDocument().getString("job_title");
                        String publisher = change.getDocument().getString("publisher");
                        Timestamp timestamp = change.getDocument().getTimestamp("timestamp");
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
                        int position = workList.size() - 1;
                        workAdapter.notifyItemInserted(position);
                    }
                }

            }
        });
    }
}