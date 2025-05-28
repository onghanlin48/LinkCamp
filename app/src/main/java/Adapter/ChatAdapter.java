package Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.databinding.ItemContainerReceivedMessageBinding;
import com.um.linkcamp.databinding.ItemContainerSentMessageBinding;

import java.util.List;

import model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final String receiverProfileImage;
    private final String senderId;
    private final List<ChatMessage> chatMessages;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    private final int page;
    private final Context context;

    public ChatAdapter(String receiverProfileImage, String senderId, List<ChatMessage> chatMessages, int page, Context context) {
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.chatMessages = chatMessages;
        this.page = page;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else{
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else{
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage,page,context);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }
        void setData(ChatMessage chatMessage,String receiverProfileImage,int page,Context context){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

            if(page == 0){
                if(receiverProfileImage.equals("skip")){
                    binding.imageProfile.setImageResource(R.drawable.icon_person);
                }else {
                    try {
                        byte[] imageBytes = Base64.decode(receiverProfileImage, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        binding.imageProfile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                binding.imageProfile.setOnClickListener(v -> {
                    Intent intent = new Intent(context, Profile_view.class);
                    intent.putExtra("UserID",chatMessage.senderId);
                    context.startActivity(intent);
                });
            }else {
                String userId = chatMessage.senderId;
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                DocumentReference Ref = firebaseFirestore.collection("Users").document(userId);
                Ref.get().addOnSuccessListener(Snapshot -> {
                    if(Snapshot.exists()) {
                        String profile = Snapshot.getString("profile");
                        String name = Snapshot.getString("name");
                        binding.textName.setVisibility(View.VISIBLE);
                        binding.textName.setText(name);
                        try {
                            byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            binding.imageProfile.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        binding.imageProfile.setOnClickListener(v -> {
                            Intent intent = new Intent(context, Profile_view.class);
                            intent.putExtra("UserID",userId);
                            context.startActivity(intent);
                        });
                        binding.textName.setOnClickListener(v -> {
                            Intent intent = new Intent(context, Profile_view.class);
                            intent.putExtra("UserID",userId);
                            context.startActivity(intent);
                        });
                    }
                });
            }


        }
    }
}
