package com.um.linkcamp;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import Adapter.ReceiveAdapter;
import Adapter.SendAdapter;
import data.Constants;
import data.DatabaseHelper;
import function.DecimalDigitsInputFilter;
import function.SpacingItemDecoration;
import function.VerifyLogin;
import model.Post;
import model.Receive;

public class View_Donation extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    FirebaseFirestore firestore;
    double total = 0;
    String ic;
    String account = null,bank_name = null,amount_w = null;
    ReceiveAdapter receiveAdapter;
    TextView txt_get,txt_send;
    List<Receive> receiveList;
    List<Receive> sendList;
    SendAdapter sendAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_donation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(View_Donation.this);
        VerifyLogin verifyLogin = new VerifyLogin(View_Donation.this);
        if (verifyLogin.isDatabaseExist()) {
            verifyLogin.verify(result -> {
                if ("other".equals(result)) {
                    dbHelper.clearUserData();
                    finish();
                } else if (!("login".equals(result))) {
                    dbHelper.clearUserData();
                    finish();
                }
            });
        } else {
            finish();
            return;
        }
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }
        ImageView btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView receiveView = findViewById(R.id.receive_view);
        RecyclerView sendView = findViewById(R.id.send_view);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        setupRecyclerView(receiveView, new LinearLayoutManager(this), spacingInPixels);
        setupRecyclerView(sendView, new LinearLayoutManager(this), spacingInPixels);

        receiveList = new ArrayList<>();
        receiveAdapter = new ReceiveAdapter(receiveList);
        receiveView.setAdapter(receiveAdapter);
        receiveView.setVisibility(View.VISIBLE);

        sendList = new ArrayList<>();
        sendAdapter = new SendAdapter(View_Donation.this,sendList);
        sendView.setAdapter(sendAdapter);

        firestore = FirebaseFirestore.getInstance();
        readReceive();
        sendReceive();

        Button btnW = findViewById(R.id.withdraw);
        btnW.setOnClickListener(v -> {
            showWithdraw();
        });
        txt_get = findViewById(R.id.get);
        txt_send = findViewById(R.id.give);

        selected("Receive / Withdraw",txt_get);

        txt_get.setOnClickListener(v -> {
            readReceive();
            selected("Receive / Withdraw",txt_get);
            receiveView.setVisibility(View.VISIBLE);
            sendView.setVisibility(View.GONE);
        });
        txt_send.setOnClickListener(v -> {
            selected("Send",txt_send);
            receiveView.setVisibility(View.GONE);
            sendView.setVisibility(View.VISIBLE);
        });
    }
    private void setupRecyclerView(RecyclerView recyclerView, LinearLayoutManager layoutManager, int spacingInPixels) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        recyclerView.setVisibility(View.GONE);
    }
    private void showWithdraw(){
        Dialog dialog = new Dialog(View_Donation.this);
        dialog.setContentView(R.layout.withdraw_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));

        TextInputEditText bankAccount = dialog.findViewById(R.id.bank_number);
        TextInputEditText bankName = dialog.findViewById(R.id.bank_name);
        TextInputEditText amount = dialog.findViewById(R.id.amount_value);

        amount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});

        Button btnC = dialog.findViewById(R.id.confirm);
        btnC.setOnClickListener(v -> {
            account = bankAccount.getText().toString().trim();
            bank_name = bankName.getText().toString().trim();
            amount_w = amount.getText().toString().trim();

            if(checkEmtpy(dialog)){
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference postsRef = db.collection("Donation");
                String donationID = postsRef.document().getId();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("Send", "Withdraw");
                hashMap.put("To", ic);
                hashMap.put("Amount", amount_w);
                hashMap.put("timestamp", new Date());
                hashMap.put("Account", account);
                hashMap.put("Bank_Name", bank_name);
                hashMap.put("status", 1);
                hashMap.put("id", donationID);

                postsRef.document(donationID).set(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            showDialog("Success", "Admin will approve in 1-3 day!");
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            showDialog("Failed", "Please contact admin!");
                            dialog.dismiss();
                        });
            }
        });

        Button cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
    public void selected(String title,TextView textView){
        txt_get.setText("Recieve / Withdraw");
        txt_send.setText("Send");

        txt_get.setTextColor(getResources().getColor(R.color.black, null));
        txt_send.setTextColor(getResources().getColor(R.color.black, null));

        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(new UnderlineSpan(), 0, title.length(), 0);
        textView.setText(spannableString);
        textView.setTextColor(getResources().getColor(R.color.blue1, null));
    }
    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(View_Donation.this);
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
    private Boolean checkEmtpy(Dialog dialog){
        boolean check = true;
        TextInputLayout l_account = dialog.findViewById(R.id.l_bank_number);
        TextInputLayout l_name = dialog.findViewById(R.id.l_bank_name);
        TextInputLayout l_amount = dialog.findViewById(R.id.amount);

        if(account == null){
            check = false;
            l_account.setHelperTextColor(ColorStateList.valueOf(RED));
            l_account.setHelperText("Please fill in Account Bank");
        } else if (account.isEmpty()) {
            check = false;
            l_account.setHelperTextColor(ColorStateList.valueOf(RED));
            l_account.setHelperText("Please fill in Account Bank");
        }else{
            l_account.setHelperTextColor(ColorStateList.valueOf(BLACK));
            l_account.setHelperText("");
        }

        if(bank_name == null){
            check = false;
            l_name.setHelperTextColor(ColorStateList.valueOf(RED));
            l_name.setHelperText("Please fill in Bank Name");
        } else if (bank_name.isEmpty()) {
            check = false;
            l_name.setHelperTextColor(ColorStateList.valueOf(RED));
            l_name.setHelperText("Please fill in Bank Name");
        }else{
            l_name.setHelperTextColor(ColorStateList.valueOf(BLACK));
            l_name.setHelperText("");
        }

        if(amount_w == null){
            check = false;
            l_amount.setHelperTextColor(ColorStateList.valueOf(RED));
            l_amount.setHelperText("Please fill in Amount (RM)");

        } else if (amount_w.isEmpty()) {
            check = false;
            l_amount.setHelperTextColor(ColorStateList.valueOf(RED));
            l_amount.setHelperText("Please fill in Amount (RM)");
        }else{
            Double amount = Double.valueOf(amount_w);
            if(amount > total){
                check = false;
                l_amount.setHelperTextColor(ColorStateList.valueOf(RED));
                l_amount.setHelperText("Total Amount is : RM "+ total +"\nCannot withdraw more than Total Amount");
            }else{
                l_amount.setHelperTextColor(ColorStateList.valueOf(BLACK));
                l_amount.setHelperText("");
            }
        }
        return check;
    }

    private void readReceive(){
        total = 0;
        receiveList.clear();
        firestore.collection("Donation")
                .whereEqualTo("To",ic)
                .addSnapshotListener(eventListener);

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Receive receive = new Receive();
                    receive.donationID = documentChange.getDocument().getString("id");
                    receive.userID = documentChange.getDocument().getString("Send");
                    if("Withdraw".equals(receive.userID)){
                        receive.status = documentChange.getDocument().getLong("status").intValue();

                        if(receive.status == 1 || receive.status == 2){
                            String amount = documentChange.getDocument().getString("Amount");

                            BigDecimal decimalAmount = new BigDecimal(amount);
                            receive.amount = "-" + decimalAmount.setScale(2).toString();
                            total = total + Double.parseDouble(receive.amount);
                        }else{
                            String amount = documentChange.getDocument().getString("Amount");

                            BigDecimal decimalAmount = new BigDecimal(amount);
                            receive.amount = "-" + decimalAmount.setScale(2).toString();
                        }
                    }else{
                        String amount = documentChange.getDocument().getString("Amount");

                        BigDecimal decimalAmount = new BigDecimal(amount);
                        receive.amount = decimalAmount.setScale(2).toString();

                        total = total + Double.parseDouble(receive.amount);
                    }
                    Date timestamp = documentChange.getDocument().getDate("timestamp");


                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    receive.date =  sdf.format(timestamp);

                    receiveList.add(receive);

                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < receiveList.size(); i++) {
                        String id = documentChange.getDocument().getString("id");
                        if (receiveList.get(i).donationID.equals(id)){
                            receiveList.get(i).status = documentChange.getDocument().getLong("status").intValue();
                            total = total - Double.parseDouble(receiveList.get(i).amount);
                            if(receiveList.get(i).status == 1 || receiveList.get(i).status == 2){
                                total = total + Double.parseDouble(receiveList.get(i).amount);
                            }
                            break;
                        }
                    }
                }
            }
            Collections.sort(receiveList,(obj1, obj2)-> obj2.date.compareTo(obj1.date));
            receiveAdapter.notifyDataSetChanged();
            TextView txt_total = findViewById(R.id.total);
            String s_total = String.valueOf(total);
            txt_total.setText("RM " + s_total);
        }
    };

    private void sendReceive(){
        sendList.clear();
        firestore.collection("Donation")
                .whereEqualTo("Send",ic)
                .addSnapshotListener(sendListener);

    }

    private final EventListener<QuerySnapshot> sendListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Receive receive = new Receive();
                    receive.donationID = documentChange.getDocument().getString("id");
                    receive.toID = documentChange.getDocument().getString("To");
                    receive.userID = documentChange.getDocument().getString("Send");
                    String amount = documentChange.getDocument().getString("Amount");

                    BigDecimal decimalAmount = new BigDecimal(amount);
                    receive.amount = decimalAmount.setScale(2).toString();
                    Date timestamp = documentChange.getDocument().getDate("timestamp");


                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    receive.date =  sdf.format(timestamp);

                    sendList.add(receive);

                }
            }
            Collections.sort(sendList,(obj1, obj2)-> obj2.date.compareTo(obj1.date));
            sendAdapter.notifyDataSetChanged();
        }
    };
}