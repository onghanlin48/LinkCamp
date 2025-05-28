package Adapter;

import static androidx.core.content.ContextCompat.registerReceiver;
import static androidx.core.content.ContextCompat.startActivity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.SetOptions;

import com.um.linkcamp.Comment;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.View_Workshop;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import data.DatabaseHelper;
import model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public List<Post> mPost;
    public Context mContext;
    Dialog dialog;
    DatabaseHelper databaseHelper;

    public PostAdapter(Context mContext,List<Post> mPost){
        this.mPost = mPost;
        this.mContext = mContext;
        this.databaseHelper = new DatabaseHelper(mContext);
        this.dialog = new Dialog(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPost.get(position);
        String ic;
        Cursor cursor = databaseHelper.getUserData();
        if(cursor.moveToFirst()){
            ic = cursor.getString(cursor.getColumnIndex("ic"));
            if(ic.equals(post.getPublisher())){
                holder.report.setVisibility(View.INVISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
                holder.follow.setVisibility(View.GONE);
            }else{
                holder.report.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.GONE);
                holder.follow.setVisibility(View.VISIBLE);
            }
        } else {
            ic = "";
        }
        if((post.getPostImage()) == null){
            holder.post_image.setVisibility(View.GONE);
        }else {
            holder.post_image.setVisibility(View.VISIBLE);
            try {
                byte[] imageBytes = Base64.decode(post.getPostImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.post_image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if((post.getDescription()).equals("")){
            holder.description.setVisibility(View.GONE);
        }else{
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }
        holder.timestamp.setText(post.getTimestamp());

        publisherInfo(holder.image_profile,holder.username,holder.role,post.getPublisher());
        isLikes(post.getPostId(),ic,holder.like );
        nrLikes(holder.likes,post.getPostId());
        isFollow(post.getPublisher(),ic,holder.follow);

        holder.follow.setOnClickListener(v -> {
            data.register register = data.register.getInstance();
            register.setIc(post.getPublisher());

            if((holder.follow.getTag()).equals("Follow")){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(post.getPublisher(), true);

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(ic)
                        .collection("following")
                        .document(post.getPublisher())
                        .set(hashMap, SetOptions.merge());

                HashMap<String, Object> f_hashMap = new HashMap<>();
                f_hashMap.put(ic, true);

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(post.getPublisher())
                        .collection("follower")
                        .document(ic)
                        .set(f_hashMap, SetOptions.merge());
                holder.follow.setImageResource(R.drawable.icon_check);
                holder.follow.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
                holder.follow.setTag("Following");
            }else{
                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(post.getPublisher())
                        .collection("follower")
                        .document(ic)
                        .delete();

                FirebaseFirestore.getInstance()
                        .collection("Follow")
                        .document(ic)
                        .collection("following")
                        .document(post.getPublisher())
                        .delete();
                holder.follow.setImageResource(R.drawable.icon_add);
                holder.follow.setColorFilter(ContextCompat.getColor(mContext, R.color.blue1));
                holder.follow.setTag("Follow");
            }
        });

        holder.like.setOnClickListener(v -> {
            if ((holder.like.getTag()).equals("like")) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(ic, true);
                FirebaseFirestore.getInstance()
                        .collection("Likes")
                        .document(post.getPostId())
                        .set(hashMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            holder.like.setImageResource(R.drawable.icon_liked);
                            holder.like.setTag("liked");
                        });

            }else{
                FirebaseFirestore.getInstance()
                        .collection("Likes")
                        .document(post.getPostId())
                        .update(ic, FieldValue.delete())
                        .addOnSuccessListener(aVoid -> {
                            holder.like.setImageResource(R.drawable.icon_like);
                            holder.like.setTag("like");
                        });

            }

            nrLikes(holder.likes,post.getPostId());
        });

        holder.image_profile.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Profile_view.class);
            intent.putExtra("UserID",post.getPublisher());
            mContext.startActivity(intent);
        });
        holder.username.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Profile_view.class);
            intent.putExtra("UserID",post.getPublisher());
            mContext.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> {
            Dialog_Delete("Delete Post","Do you want delete your post?", post.getPostId());
        });

        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Comment.class);
            intent.putExtra("PostId",post.getPostId());
            mContext.startActivity(intent);
        });
        holder.report.setOnClickListener(v -> {
            Dialog_Report(ic,post.getPostId());
        });
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_profile, post_image, like, comment, delete, report,follow;
        public TextView username, likes, description,role,timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.profile_post);
            post_image = itemView.findViewById(R.id.image_post);
            like = itemView.findViewById(R.id.like_post);
            comment = itemView.findViewById(R.id.comment_post);
            delete = itemView.findViewById(R.id.delete_post);
            report = itemView.findViewById(R.id.report_post);
            username = itemView.findViewById(R.id.username_post);
            likes = itemView.findViewById(R.id.likes_post);
            description = itemView.findViewById(R.id.description_post);
            role = itemView.findViewById(R.id.role_post);
            timestamp = itemView.findViewById(R.id.time_post);
            follow = itemView.findViewById(R.id.follow_post);
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
    private void isLikes(String postId,String ic, ImageView imageView){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Likes").document(postId);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if (Snapshot.exists()){
                Boolean hasLiked = Snapshot.getBoolean(ic);
                if (hasLiked != null && hasLiked) {
                    imageView.setImageResource(R.drawable.icon_liked);
                    imageView.setTag("liked");
                } else {

                    imageView.setImageResource(R.drawable.icon_like);
                    imageView.setTag("like");
                }
            }else{
                imageView.setImageResource(R.drawable.icon_like);
                imageView.setTag("like");
            }
        });

    }
    private void nrLikes(TextView likes,String postId){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference Ref = firebaseFirestore.collection("Likes").document(postId);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if (Snapshot.exists()) {

                int likeCount = Snapshot.getData() != null ? Snapshot.getData().size() : 0;
                likes.setText(likeCount + " Likes");
            } else {

                likes.setText("0 Likes");
            }
        });
    }
    private void publisherInfo(ImageView image_profile,TextView username,TextView role,String userId){
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
                role.setText(Snapshot.getString("role"));
            }
        });
    }
    private void Dialog_Delete(String title, String message,String postId) {

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
                    .collection("Posts")
                    .document(postId)
                    .delete();
            FirebaseFirestore.getInstance()
                    .collection("Comments")
                    .document(postId)
                    .delete();
            FirebaseFirestore.getInstance()
                    .collection("Likes")
                    .document(postId)
                    .delete();
            FirebaseFirestore.getInstance()
                    .collection("Reports")
                    .document(postId)
                    .delete();
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
    private void Dialog_Report(String ic,String postId) {

        dialog.setContentView(R.layout.report_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(null);

        TextInputEditText txt_reason = dialog.findViewById(R.id.content);
        TextInputLayout l_reason = dialog.findViewById(R.id.l_report);

        Button btnreport = dialog.findViewById(R.id.confirm);
        btnreport.setOnClickListener(v -> {
            String reason = txt_reason.getText().toString().trim();
            if(reason == null){
                l_reason.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                l_reason.setHelperText("Please fill in the reason!");
            } else if (reason.isEmpty()) {
                l_reason.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                l_reason.setHelperText("Please fill in the reason!");
            }else{
                l_reason.setHelperTextColor(ColorStateList.valueOf(Color.BLACK));
                l_reason.setHelperText(" ");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference postsRef = db.collection("Report");
                String reportId = postsRef.document().getId();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("publisher", ic);
                hashMap.put("reason", reason);
                hashMap.put("PostID", postId);
                hashMap.put("ReportId", reportId);
                hashMap.put("timestamp", new Date());

                postsRef.document(reportId).set(hashMap);
                dialog.dismiss();
            }
        });

        Button cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
