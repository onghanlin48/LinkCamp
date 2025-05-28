package function;

import android.content.Context;
import android.database.Cursor;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

import data.DatabaseHelper;

public class VerifyLogin {
    private DatabaseHelper dbHelper;
    private Context context;

    public VerifyLogin(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public interface VerifyCallback {
        void onResult(String result);
    }

    public boolean isDatabaseExist() {
        File dbFile = context.getDatabasePath("UserData.db");
        return dbFile.exists();
    }

    public void verify(VerifyCallback callback) {
        Cursor cursor = dbHelper.getUserData();
        if (cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndex("email"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            String loginStatus = cursor.getString(cursor.getColumnIndex("login_status"));
            String loginDetail = cursor.getString(cursor.getColumnIndex("login_detail"));
            String ic = cursor.getString(cursor.getColumnIndex("ic"));
            String Certificates = cursor.getString(cursor.getColumnIndex("certificates"));
            String Front = cursor.getString(cursor.getColumnIndex("front"));
            String Back = cursor.getString(cursor.getColumnIndex("back"));

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference loginRef = db.collection("Login").document(loginDetail);

            // Attach a real-time listener to loginRef
            loginRef.addSnapshotListener((loginSnapshot, e) -> {
                if (e != null) {
                    // Handle error
                    callback.onResult(null);
                    return;
                }

                if (loginSnapshot != null && loginSnapshot.exists()) {
                    String status = loginSnapshot.getString("status");
                    if (status != null && status.equals(loginStatus)) {
                        DocumentReference userRef = db.collection("Users").document(ic);

                        // Attach a real-time listener to the user document
                        userRef.addSnapshotListener((userSnapshot, userError) -> {
                            if (userError != null) {
                                // Handle error
                                callback.onResult(null);
                                return;
                            }

                            if (userSnapshot != null && userSnapshot.exists()) {
                                String dEmail = userSnapshot.getString("email");
                                String dPassword = userSnapshot.getString("password");
                                String dic = userSnapshot.getString("ic");
                                int status_check = userSnapshot.getLong("status").intValue();
                                if(status_check == 2){
                                    if (email.equals(dEmail) && password.equals(dPassword) && ic.equals(dic)) {
                                        String profile = userSnapshot.getString("profile");
                                        String name = userSnapshot.getString("name");
                                        String role = userSnapshot.getString("role");

                                        // Insert user data into local DB
                                        dbHelper.insertUser(
                                                profile,
                                                name,
                                                dPassword,
                                                role,
                                                ic,
                                                dEmail,
                                                loginDetail,
                                                loginStatus,
                                                Front,
                                                Back,
                                                Certificates
                                        );

                                        callback.onResult("login");
                                    } else {
                                        callback.onResult(null);
                                    }
                                }else {
                                    callback.onResult(null);
                                }

                            } else {
                                callback.onResult(null);
                            }
                        });
                    } else {
                        callback.onResult("other");
                    }
                } else {
                    callback.onResult(null);
                }
            });


        } else {
            callback.onResult(null);
        }
        if (cursor != null) cursor.close();
    }

}
