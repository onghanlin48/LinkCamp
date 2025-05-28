package com.um.linkcamp;

import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import data.Constants;
import data.DatabaseHelper;

public class BaseActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    String ic;
    private DocumentReference documentReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(BaseActivity.this);
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection("Users")
                .document(ic);
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY,1);
    }
}
