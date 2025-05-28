package Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.um.linkcamp.Channel;
import com.um.linkcamp.Chat;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.View_learn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import model.Learn;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LearnAdapter extends RecyclerView.Adapter<LearnAdapter.ViewHolder>{
    public List<Learn> mLearn;
    public Context mContext;
    public Dialog dialog;
    public String ic;
    public LearnAdapter(Context mContext, List<Learn> mLearn,String ic){
        this.mLearn = mLearn;
        this.mContext = mContext;
        this.ic = ic;
        this.dialog = new Dialog(mContext);
    }
    @NonNull
    @Override
    public LearnAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.learn_item, parent, false);
        return new LearnAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LearnAdapter.ViewHolder holder, int position) {
        Learn learn = mLearn.get(position);
        publisherInfo(holder.profile,holder.name,learn.getPublisher());
        isFollow(learn.getPublisher(),ic,holder.follow);

        if(learn.getPublisher().equals(ic)){
            holder.follow.setVisibility(View.GONE);
            holder.delete.setVisibility(View.VISIBLE);

        }else{
            holder.delete.setVisibility(View.GONE);

        }

        holder.follow.setOnClickListener(v -> {
            if((holder.follow.getTag()).equals("Follow")){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(learn.getPublisher(), true);

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(ic)
                        .collection("following")
                        .document(learn.getPublisher())
                        .set(hashMap, SetOptions.merge());

                HashMap<String, Object> f_hashMap = new HashMap<>();
                f_hashMap.put(ic, true);

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(learn.getPublisher())
                        .collection("follower")
                        .document(ic)
                        .set(f_hashMap, SetOptions.merge());
                holder.follow.setImageResource(R.drawable.icon_check);
                holder.follow.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
                holder.follow.setTag("Following");
            }else{
                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(learn.getPublisher())
                        .collection("follower")
                        .document(ic)
                        .delete();

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(ic)
                        .collection("following")
                        .document(learn.getPublisher())
                        .delete();
                holder.follow.setImageResource(R.drawable.icon_add);
                holder.follow.setColorFilter(ContextCompat.getColor(mContext, R.color.blue1));
                holder.follow.setTag("Follow");
            }
        });
        holder.title.setText(learn.getTitle());
        holder.description.setText(learn.getDescription());
        holder.time.setText(learn.getTimestamp());

        if(learn.getChannel()){
            holder.channel.setVisibility(View.VISIBLE);
        }else{
            holder.channel.setVisibility(View.GONE);
        }
        holder.profile.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Profile_view.class);
            intent.putExtra("UserID",learn.getPublisher());
            mContext.startActivity(intent);
        });
        holder.name.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Profile_view.class);
            intent.putExtra("UserID",learn.getPublisher());
            mContext.startActivity(intent);
        });

        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, View_learn.class);
            intent.putExtra("LearnID",learn.getId());
            mContext.startActivity(intent);
        });
        holder.channel.setOnClickListener(v -> {
            Intent intent1 = new Intent(mContext, Channel.class);
            intent1.putExtra("channelName",learn.getTitle());
            intent1.putExtra("channelID",learn.getId());
            mContext.startActivity(intent1);
        });
        holder.delete.setOnClickListener(v -> {
            Dialog_Delete("Delete Material","Do you want delete your Material?", learn.getId(),position);
        });
    }

    @Override
    public int getItemCount() {
        return mLearn.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile,follow,delete;
        TextView name,title,description,time;
        Button view,channel;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile_post);
            follow = itemView.findViewById(R.id.follow_post);
            name = itemView.findViewById(R.id.username_post);
            description = itemView.findViewById(R.id.description_post);
            title =itemView.findViewById(R.id.title);
            view =itemView.findViewById(R.id.view);
            channel =itemView.findViewById(R.id.channel);
            time = itemView.findViewById(R.id.time);
            delete = itemView.findViewById(R.id.delete_post);
        }
    }
    private void isFollow(String userId,String ic,ImageView imageView){

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
                    imageView.setImageResource(R.drawable.icon_check);
                    imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
                    imageView.setTag("Following");

                } else {
                    imageView.setImageResource(R.drawable.icon_add);
                    imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.blue1));
                    imageView.setTag("Follow");
                }
            }else {
                imageView.setImageResource(R.drawable.icon_add);
                imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.blue1));
                imageView.setTag("Follow");
            }

        });

    }
    private void publisherInfo(ImageView image_profile,TextView username,String userId){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Users").document(userId);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
                String profile = Snapshot.getString("profile");
                if(profile.equals("skip")){
                    image_profile.setImageResource(R.drawable.icon_person);
                }else {
                    try {
                        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        image_profile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                username.setText(Snapshot.getString("name"));
            }
        });
    }
    private void Dialog_Delete(String title, String message,String postId,int position) {

        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(null);

        TextView t = dialog.findViewById(R.id.tittle);
        TextView d = dialog.findViewById(R.id.detail);
        Button btnC = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        btnCancel.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue1));
        btnC.setBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
        btnC.setText("Delete");
        t.setText(title);
        d.setText(message);
        btnC.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("Learning")
                    .document(postId)
                    .delete();
            FirebaseFirestore.getInstance()
                    .collection("Channel")
                    .document(postId)
                    .delete();
            String url = "https://nodemaillin.netlify.app/.netlify/functions/delete";

            // Create the JSON payload
            String jsonPayload = "{ \"resourceId\": \""+postId+"\" }";
            System.out.println(postId);

            // Create the request body
            RequestBody body = RequestBody.create(
                    jsonPayload,
                    MediaType.parse("application/json; charset=utf-8")
            );

            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle failure
                    System.err.println("Request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Handle success
                    if (response.isSuccessful()) {
                        System.out.println("Response: " + response.body().string());
                    } else {
                        System.err.println("Request failed with code: " + response.code());
                    }
                }
            });
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
