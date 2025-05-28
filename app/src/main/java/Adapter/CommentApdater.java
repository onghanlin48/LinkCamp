package Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.databinding.ItemCommentBinding;
import com.um.linkcamp.databinding.UserItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import function.SpacingItemDecoration;
import model.Comment_save;
import model.Reply;

public class CommentApdater extends RecyclerView.Adapter<CommentApdater.CommentViewHolder>{

    private final Context context;
    private final List<Comment_save> commentSaveList;
    private final String ic;

    public CommentApdater(Context context, List<Comment_save> commentSaveList, String ic) {
        this.context = context;
        this.commentSaveList = commentSaveList;
        this.ic = ic;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentApdater.CommentViewHolder(
                ItemCommentBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.setData(commentSaveList.get(position),context,ic);
    }

    @Override
    public int getItemCount() {
        return commentSaveList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;
        List<Reply> replyList;
        ReplyAdapter replyAdapter;

        String name;
        public CommentViewHolder(ItemCommentBinding itemCommentBinding) {
            super(itemCommentBinding.getRoot());
            binding = itemCommentBinding;
        }

        void setData(Comment_save comment, Context context,String ic){

            publisherInfo(binding.profile,binding.name,comment.getPublisher());
            binding.description.setText(comment.getComment());

            if(comment.getPublisher().equals(ic)){
                binding.delete.setVisibility(View.VISIBLE);
            }else{
                binding.delete.setVisibility(View.GONE);
            }
            binding.date.setText(comment.getTimestamp());

            binding.commentBar.setVisibility(View.GONE);
            binding.replyView.setVisibility(View.GONE);

            binding.profile.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_view.class);
                intent.putExtra("UserID",comment.getPublisher());
                context.startActivity(intent);
            });

            binding.name.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_view.class);
                intent.putExtra("UserID",comment.getPublisher());
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
                delete(comment.getCommentID());
            });
            binding.Reply.setOnClickListener(v -> {
                String reply = binding.editReply.getText().toString().trim();
                if(reply != null){
                    if (!reply.isEmpty()){
                        binding.commentBar.setVisibility(View.GONE);
                        upload(reply,ic,comment.getCommentID());
                    }
                }
            });

            int spacingInPixels = context.getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
            setupRecyclerView(binding.replyView,new LinearLayoutManager(context),spacingInPixels);

            replyList = new ArrayList<>();
            replyAdapter = new ReplyAdapter(replyList,ic,context);

            binding.replyView.setAdapter(replyAdapter);
            readComment(comment.getCommentID());
        }
        private void setupRecyclerView(RecyclerView recyclerView, LinearLayoutManager layoutManager, int spacingInPixels) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
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
        private void delete(String commentID){
            FirebaseFirestore.getInstance()
                    .collection("Comment")
                    .document(commentID)
                    .delete();
            FirebaseFirestore.getInstance()
                    .collection("Reply")
                    .whereEqualTo("CommentID", commentID)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            document.getReference().delete();
                        }
                    });
        }

        private void readComment(String commentID){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            replyList.clear();
            database.collection("Reply")
                    .whereEqualTo("CommentID",commentID)
                    .addSnapshotListener(eventListener);
        }

        private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                Boolean check = false;
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if(documentChange.getType() == DocumentChange.Type.ADDED){
                        check = true;
                        String replyID = documentChange.getDocument().getString("ReplyID");
                        String commentID = documentChange.getDocument().getString("CommentID");
                        String comment_context = documentChange.getDocument().getString("Comment");
                        String name = documentChange.getDocument().getString("Name");
                        String publisher = documentChange.getDocument().getString("publisher");
                        Date timestamp = documentChange.getDocument().getDate("timestamp");

                        Reply reply = new Reply(replyID,commentID,comment_context,publisher,name,timestamp);

                        replyList.add(reply);
                    } else if (documentChange.getType() == DocumentChange.Type.REMOVED){
                        for (int i = 0; i < replyList.size(); i++) {
                            String replyID = documentChange.getDocument().getString("ReplyID");
                            if(replyList.get(i).getReplyID().equals(replyID)){
                                replyList.remove(i);
                            }
                        }
                    }
                }
                Collections.sort(replyList,(obj1, obj2)-> obj1.getTimeStamp().compareTo(obj2.getTimeStamp()));
                replyAdapter.notifyDataSetChanged();
                if(check){
                    binding.replyView.setVisibility(View.VISIBLE);
                }
            }
        };
    }
}
