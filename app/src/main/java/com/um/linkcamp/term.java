package com.um.linkcamp;

import static function.convert.handleBase64Content;

import android.app.Dialog;
import android.content.Intent;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class term extends AppCompatActivity {
    PDFView pdfView;
    private CheckBox checkTerm;
    private FirebaseFirestore db;
    Dialog dialog ,dialog_pdf;
    TextView tittle,detail;
    String termValue;
    private int totalPages = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_term);
        dialog = new Dialog(term.this);
        dialog.setContentView(R.layout.create_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
        dialog.setCancelable(false);

        data.register register = data.register.getInstance();
        String role = register.getRole();
        pdfView = findViewById(R.id.pdfView);
        checkTerm = findViewById(R.id.check_term);
        checkTerm.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("Term").document(role);

        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                termValue = documentSnapshot.getString("term");
                if (termValue != null) {
                    if (handleBase64Content(termValue).equals("PDF")) {
                        try {
                            byte[] pdfBytes = Base64.decode(termValue, Base64.DEFAULT);
                            InputStream pdfStream = new ByteArrayInputStream(pdfBytes);
                            pdfView.fromStream(pdfStream)
                                    .onLoad(new OnLoadCompleteListener() {
                                        @Override
                                        public void loadComplete(int numberOfPages) {
                                            totalPages = numberOfPages;
                                        }
                                    })
                                    .onPageChange(new OnPageChangeListener() {
                                        @Override
                                        public void onPageChanged(int page, int pageCount) {
                                            if (page == pageCount - 1) {
                                                checkTerm.setEnabled(true);
                                            }
                                        }
                                    })
                                    .load();
                        } catch (Exception ev) {
                            Log.e("Error", ev.toString());
                        }
                    }
                }
            } else {
                Log.d("Firestore", "No such document");
            }
        });

        pdfView.setOnClickListener(v -> {
            dialog_pdf = new Dialog(term.this);
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog_pdf.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog));
            dialog_pdf.setContentView(R.layout.pdf_dialog);
            dialog_pdf.setCancelable(true);
            PDFView pdf = dialog_pdf.findViewById(R.id.pdfView);
            TextView txt = dialog_pdf.findViewById(R.id.tittle);
            txt.setText("Terms & Conditions");
            byte[] pdfBytes = Base64.decode(termValue, Base64.DEFAULT);
            InputStream pdfStream = new ByteArrayInputStream(pdfBytes);
            pdf.fromStream(pdfStream)
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int numberOfPages) {
                            totalPages = numberOfPages;
                        }
                    })
                    .onPageChange(new OnPageChangeListener() {
                        @Override
                        public void onPageChanged(int page, int pageCount) {
                            if (page == pageCount - 1) {
                                checkTerm.setEnabled(true);
                            }
                        }
                    })
                    .load();
            dialog_pdf.show();
        });
        TextView BtnAlready = findViewById(R.id.already);
        BtnAlready.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(term.this,login_page.class);
            startActivity(intent);
            finish();
        });

        Button btnNext = findViewById(R.id.next);
        btnNext.setOnClickListener(v -> {
            if (checkTerm.isChecked()){
                FirebaseFirestore database = FirebaseFirestore.getInstance();

                Map<String, Object> userUpdates = new HashMap<>();
                String ic = register.getIc();
                do{
                    if(role.equals("User")){
                        String name = register.getName();
                        String email = register.getEmail();
                        String pass = register.getPassword();
                        String salt = register.getSalt();
                        String front = register.getIc_front();
                        String back = register.getIc_back();
                        String profile = register.getProfile();


                        userUpdates.put("role", role);
                        userUpdates.put("ic", ic);
                        userUpdates.put("name", name);
                        userUpdates.put("email", email);
                        userUpdates.put("password", pass);
                        userUpdates.put("salt", salt);
                        userUpdates.put("front", front);
                        userUpdates.put("back", back);
                        userUpdates.put("profile",profile);
                        userUpdates.put("status",1);

                        break;
                    }
                    if (role.equals("Lecturer & Teacher")){
                        String name = register.getName();
                        String email = register.getEmail();
                        String pass = register.getPassword();
                        String salt = register.getSalt();
                        String front = register.getIc_front();
                        String back = register.getIc_back();
                        String profile = register.getProfile();
                        String certificates = register.getCertificates();

                        userUpdates.put("role", role);
                        userUpdates.put("ic", ic);
                        userUpdates.put("name", name);
                        userUpdates.put("email", email);
                        userUpdates.put("password", pass);
                        userUpdates.put("salt", salt);
                        userUpdates.put("front", front);
                        userUpdates.put("back", back);
                        userUpdates.put("certificates",certificates);
                        userUpdates.put("profile",profile);
                        userUpdates.put("status",1);

                        break;
                    }
                    if(role.equals("Company")){
                        String name = register.getName();
                        String email = register.getEmail();
                        String pass = register.getPassword();
                        String salt = register.getSalt();
                        String profile = register.getProfile();
                        String certificates = register.getCertificates();

                        userUpdates.put("role", role);
                        userUpdates.put("ic", ic);
                        userUpdates.put("name", name);
                        userUpdates.put("email", email);
                        userUpdates.put("password", pass);
                        userUpdates.put("salt", salt);
                        userUpdates.put("certificates",certificates);
                        userUpdates.put("profile",profile);
                        userUpdates.put("status",1);
                        break;
                    }
                }while (true);
                tittle = dialog.findViewById(R.id.tittle);
                detail = dialog.findViewById(R.id.detail);
                userUpdates.put("description",null);
                userUpdates.put("Token","new");
                userUpdates.put("availability",0);
                database.collection("Users").document(ic)
                        .set(userUpdates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                tittle.setText("Success");
                                detail.setText("Please wait for admin approval!");
                                dialog.show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                tittle.setText("Failed");
                                detail.setText("Please contact admin!");
                                dialog.show();
                            }
                        });



            }else{
                checkTerm.setTextColor(ColorStateList.valueOf(Color.RED));
            }
        });

        Button btnC = dialog.findViewById(R.id.confirm);
        btnC.setOnClickListener(v -> {
            register.clear();
            Intent intent = new Intent(term.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}