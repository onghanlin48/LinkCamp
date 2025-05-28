package Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.databinding.ItemReceiveBinding;
import com.um.linkcamp.databinding.ItemSendBinding;

import java.util.List;

import model.Receive;

public class SendAdapter extends RecyclerView.Adapter<SendAdapter.SendViewHolder>{
    private final Context context;
    private final List<Receive> sendList;

    public SendAdapter(Context context, List<Receive> sendList) {
        this.context = context;
        this.sendList = sendList;
    }

    @NonNull
    @Override
    public SendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SendAdapter.SendViewHolder(
                ItemSendBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SendViewHolder holder, int position) {
        holder.setData(sendList.get(position),context);
    }

    @Override
    public int getItemCount() {
        return sendList.size();
    }

    class SendViewHolder extends RecyclerView.ViewHolder{
        ItemSendBinding binding;
        public SendViewHolder(ItemSendBinding itemSendBinding) {
            super(itemSendBinding.getRoot());
            binding = itemSendBinding;
        }
        void setData(Receive receive, Context context){
            binding.amount.setText("RM " + receive.amount);
            binding.date.setText(receive.date);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference Ref = firebaseFirestore.collection("Users").document(receive.toID);
            Ref.get().addOnSuccessListener(Snapshot -> {
                if(Snapshot.exists()) {
                    String profile = Snapshot.getString("profile");
                    String name = Snapshot.getString("name");
                    binding.title.setText(name);
                    try {
                        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        binding.profilePost.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    binding.profilePost.setOnClickListener(v -> {
                        Intent intent = new Intent(context, Profile_view.class);
                        intent.putExtra("UserID",receive.toID);
                        context.startActivity(intent);
                    });
                    binding.title.setOnClickListener(v -> {
                        Intent intent = new Intent(context, Profile_view.class);
                        intent.putExtra("UserID",receive.toID);
                        context.startActivity(intent);
                    });
                }
            });
        }
    }
}
