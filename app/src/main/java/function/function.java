package function;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import java.io.File;
import java.time.LocalDate;
import java.util.Base64;


import java.io.IOException;


import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.um.linkcamp.Workshop_Applicant;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.*;;
public class function {


    public static boolean verify_email(String email){
        email = email.toLowerCase();
        String p = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$";
         return email.matches(p);
    }

    public static void sendmail(String email, String title, String content)  throws IOException {
        String json = "{\"recipient\": \""+email+"\",\"subject\": \""+title+"\",\"message\": \""+content+"\"}";

        // OkHttp client
        OkHttpClient client = new OkHttpClient();

        // Request body with JSON data
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        // Build request
        Request request = new Request.Builder()
                .url("https://nodemaillin.netlify.app/.netlify/functions/sendEmail")
                .post(body)
                .build();

        // Make the asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Email sent successfully!");

                } else {
                    System.out.println("Failed to send email. Response code: " + response.code());
                }


            }
        });

    }
    public static final String TAG = "EmailSender";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendEmail(String email, String title, String content, Context context) {
        String json;
        try {
            json = new JSONObject()
                    .put("recipient", email)
                    .put("subject", title)
                    .put("message", content)
                    .toString();
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON payload", e);
            Toast.makeText(context, "Failed to send email: Invalid data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create OkHttpClient with proper timeout configurations
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Create the request body
        RequestBody body = RequestBody.create(json, JSON);

        // Build the request
        Request request = new Request.Builder()
                .url("https://nodemaillin.netlify.app/.netlify/functions/sendEmail")
                .post(body)
                .build();

        // Make the asynchronous call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to send email", e);
                // Show a toast on the UI thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Failed to send email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle success
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Email sent successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Handle failure
                    String responseBody = response.body() != null ? response.body().string() : "null";
                    Log.e(TAG, "Failed to send email. Code: " + response.code() + ", Body: " + responseBody);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Failed to send email: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    public static int random(){
        Random random = new Random();
        return  1000 + random.nextInt(9000);
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes()); // Add salt
        byte[] hashedPassword = md.digest(password.getBytes()); // Hash password with salt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(hashedPassword);
        }else{
            return null;
        }
    }

    public static boolean isValidPassword(String password,TextInputLayout textInputLayout)  {
        String helpText = "At least one uppercase letter (e.g., A, B, C)\n" +
                "At least one lowercase letter (e.g., a, b, c)\n" +
                "At least one digit (e.g., 0, 1, 2)\n" +
                "At least one special character (e.g., !, @, #, $)\n" +
                "Minimum length of 8 characters";

        SpannableString spannableString = new SpannableString(helpText);

        boolean isValid = true;

        // Check for minimum length
        if (password.length() < 8) {
            isValid = false;
            int startIndex = helpText.indexOf("Minimum length of 8 characters");
            int endIndex = startIndex + "Minimum length of 8 characters".length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            isValid = false;
            int startIndex = helpText.indexOf("At least one uppercase letter (e.g., A, B, C)");
            int endIndex = startIndex + "At least one uppercase letter (e.g., A, B, C)".length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            isValid = false;
            int startIndex = helpText.indexOf("At least one lowercase letter (e.g., a, b, c)");
            int endIndex = startIndex + "At least one lowercase letter (e.g., a, b, c)".length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            isValid = false;
            int startIndex = helpText.indexOf("At least one digit (e.g., 0, 1, 2)");
            int endIndex = startIndex + "At least one digit (e.g., 0, 1, 2)".length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            isValid = false;
            int startIndex = helpText.indexOf("At least one special character (e.g., !, @, #, $)");
            int endIndex = startIndex + "At least one special character (e.g., !, @, #, $)".length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        textInputLayout.setHelperText(spannableString);

        return isValid;
    }

    public static String random_password(){
        Random r = new Random();
        int rStringLength = r.nextInt(26) + 5;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < rStringLength; i++) {
            char chr = (char) (r.nextInt(95) + 33);
            str.append(chr);
        }
        return str.toString();
    }

    public static void addTextWatcher(EditText currentField, EditText nextField) {
        currentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) { // Move to next field when one character is entered
                    nextField.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static Boolean check_ic_format(String ic) {
        if (ic.length() == 12) {
            int inputMonth = Integer.parseInt(ic.substring(2, 4)); // Example: February (1 = January, 12 = December)

            // Days in each month (index 0 = January, index 11 = December)
            int[] daysInMonths = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

            if (inputMonth >= 1 && inputMonth <= 12) {
                int days = daysInMonths[inputMonth - 1]; // Access the corresponding month
                if (days >= Integer.parseInt(ic.substring(4, 6))) {
                    return true;
                }
            }
        }
        return false;

    }

    public static Boolean check_ssm_format(String ic) {
        if(ic.length() != 12){
            return false;
        }
        LocalDate localDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
        }
        int thisYear = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            thisYear = localDate.getYear();
        }
        if(Integer.parseInt(ic.substring(0,4)) <= thisYear ){
            if(Integer.parseInt(ic.substring(4,6)) >0 && Integer.parseInt(ic.substring(4,6))<=6 ){
                return true;
            }else{
                return false;
            }
        }
        return false;

    }

    public static String remove(String c){
        return c.replace("/", "");
    }
}
