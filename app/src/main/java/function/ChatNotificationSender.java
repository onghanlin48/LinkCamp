package function;

import android.content.Context;

import com.google.auth.oauth2.GoogleCredentials;
import okhttp3.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class ChatNotificationSender {
    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/linkcamp-7fb6a/messages:send";

    public static void sendNotification(Context context, String token, String title, String body,String userId,String channel) {
        try {
            System.out.println(token);
            // Get access token

            InputStream serviceAccount = context.getAssets().open("serviceAccountKey.json");
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(serviceAccount)
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");
            googleCredentials.refreshIfExpired();
            String accessToken = googleCredentials.getAccessToken().getTokenValue();
            System.out.println(body);
            // Create JSON payload
            String payload = "{"
                    + "\"message\": {"
                    + "  \"token\": \"" + token + "\","
                    + "  \"notification\": {"
                    + "    \"title\": \"" + title + "\","
                    + "    \"body\": \"" + body + "\""
                    + "  },"
                    + "  \"data\": {"
                    + "    \"userId\": \""+userId+"\","
                    + "    \"channel\": \""+channel+"\""
                    + "  }"
                    + "}"
                    + "}";

            // Send HTTP POST request
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(payload, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(FCM_API_URL)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            System.out.println("Response: " + response.body().string());
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
