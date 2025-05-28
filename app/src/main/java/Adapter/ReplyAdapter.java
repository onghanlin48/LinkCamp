package Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.databinding.ItemCommentBinding;
import com.um.linkcamp.databinding.ReplyItemBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import model.Reply;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>{
    private final List<Reply> replyList;
    private final String ic;
    private final Context context;

    public ReplyAdapter(List<Reply> replyList, String ic, Context context) {
        this.replyList = replyList;
        this.ic = ic;
        this.context = context;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReplyAdapter.ReplyViewHolder(
                ReplyItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        holder.setData(replyList.get(position),context,ic);
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder{
        ReplyItemBinding binding;
        String name;
        public ReplyViewHolder(ReplyItemBinding replyItemBinding) {
            super(replyItemBinding.getRoot());
            binding = replyItemBinding;
        }
        void setData(Reply reply, Context context,String ic){
            publisherInfo(binding.profile,binding.name,reply.getPublisher());

            String fullText = reply.getName() + " " + reply.getComment();
            String targetText = reply.getName();

            SpannableString spannableString = new SpannableString(fullText);

            int start = fullText.indexOf(targetText);
            int end = start + targetText.length();

            spannableString.setSpan(
                    new ForegroundColorSpan(Color.BLUE), // Color
                    start, // Start index
                    end,   // End index
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            binding.description.setText(spannableString);
            if(reply.getPublisher().equals(ic)){
                binding.delete.setVisibility(View.VISIBLE);
            }else{
                binding.delete.setVisibility(View.GONE);
            }
            binding.date.setText(reply.getTimestamp());

            binding.commentBar.setVisibility(View.GONE);

            binding.profile.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_view.class);
                intent.putExtra("UserID",reply.getPublisher());
                context.startActivity(intent);
            });

            binding.name.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_view.class);
                intent.putExtra("UserID",reply.getPublisher());
                context.startActivity(intent);
            });

            binding.btnReply.setOnClickListener(v -> {
                binding.commentBar.setVisibility(View.VISIBLE);
                binding.editReply.requestFocus();
            });

            binding.editReply.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus) {
                    binding.commentBar.setVisibility(View.GONE);
                }
            });
            binding.delete.setOnClickListener(v -> {
                System.out.println(reply.getReplyID());
                delete(reply.getReplyID());
            });
            binding.Reply.setOnClickListener(v -> {
                String comment = binding.editReply.getText().toString().trim();
                if(comment != null){
                    if (!comment.isEmpty()){
                        binding.commentBar.setVisibility(View.GONE);
                        upload(comment,ic,reply.getCommentID());
                    }
                }
            });

        }
        private void upload(String reply,String ic,String commentID){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference postsRef = db.collection("Reply");
            String replyID = postsRef.document().getId();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("publisher", ic);
            hashMap.put("Comment", reply);
            hashMap.put("ReplyID", replyID);
            hashMap.put("CommentID", commentID);
            hashMap.put("Name", name);
            hashMap.put("timestamp", new Date());

            postsRef.document(replyID).set(hashMap);
        }
        private void delete(String replyID){
            FirebaseFirestore.getInstance()
                    .collection("Reply")
                    .document(replyID)
                    .delete();
        }
        private void publisherInfo(ImageView image_profile, TextView username, String userId){
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
                    name = Snapshot.getString("name");
                    username.setText(Snapshot.getString("name"));
                }
            });
        }
    }
}
