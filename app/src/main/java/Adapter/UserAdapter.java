package Adapter;

import static android.graphics.Color.GRAY;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.databinding.ItemReceiveBinding;
import com.um.linkcamp.databinding.UserItemBinding;

import java.util.HashMap;
import java.util.List;

import model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private final List<User> userList;
    private final Context context;
    private final String ic;

    public UserAdapter(List<User> userList, Context context,String ic) {
        this.userList = userList;
        this.context = context;
        this.ic = ic;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserAdapter.UserViewHolder(
                UserItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setData(userList.get(position),context,ic);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        UserItemBinding binding;
        public UserViewHolder(UserItemBinding userItemBinding) {
            super(userItemBinding.getRoot());

            binding = userItemBinding;
        }
        void setData(User user,Context context,String ic){
            binding.name.setText(user.getUserName());
            binding.role.setText(user.getUserRole());

            if(user.getUserProfile().equals("skip")){
                binding.profile.setImageResource(R.drawable.icon_person);
            }else {
                try {
                    byte[] imageBytes = Base64.decode(user.getUserProfile(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    binding.profile.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String id = user.getUserId();

            if(id.equals(ic)){
                binding.following.setVisibility(View.GONE);
            }else{
                isFollow(id,ic,context,binding.following);
            }

            binding.following.setOnClickListener(v -> {

                if((binding.following.getTag()).equals("Follow")){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(id, true);

                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(ic)
                            .collection("following")
                            .document(id)
                            .set(hashMap, SetOptions.merge());

                    HashMap<String, Object> f_hashMap = new HashMap<>();
                    f_hashMap.put(ic, true);

                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(id)
                            .collection("follower")
                            .document(ic)
                            .set(f_hashMap, SetOptions.merge());
                    binding.following.setText("Following");
                    binding.following.setBackgroundColor(GRAY);
                    binding.following.setTag("Following");
                }else{
                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(id)
                            .collection("follower")
                            .document(ic)
                            .delete();

                    FirebaseFirestore.getInstance()
                            .collection("Follow")
                            .document(ic)
                            .collection("following")
                            .document(id)
                            .delete();
                    binding.following.setText("Follow");
                    binding.following.setBackgroundColor(ContextCompat.getColor(context, R.color.blue1));
                    binding.following.setTag("Follow");
                }
            });

            binding.profile.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_view.class);
                intent.putExtra("UserID",id);
                context.startActivity(intent);
            });

            binding.name.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_view.class);
                intent.putExtra("UserID",id);
                context.startActivity(intent);
            });

        }
        private void isFollow(String userId, String ic, Context context, Button button){

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
                        button.setText("Following");
                        button.setBackgroundColor(GRAY);
                        button.setTag("Following");

                    } else {
                        button.setText("Follow");
                        button.setBackgroundColor(ContextCompat.getColor(context, R.color.blue1));
                        button.setTag("Follow");
                    }
                }else {
                    button.setText("Follow");
                    button.setBackgroundColor(ContextCompat.getColor(context, R.color.blue1));
                    button.setTag("Follow");
                }

            });

        }
    }
}
