package Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.Channel;
import com.um.linkcamp.Chat;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.databinding.ItemContainerRecentConversionBinding;

import java.util.HashMap;
import java.util.List;

import model.ChatMessage;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final Context context;
    private final String ic;;
    private final int page;

    public RecentConversationsAdapter(Context context, List<ChatMessage> chatMessages, String ic, int page) {
        this.chatMessages = chatMessages;
        this.context = context;
        this.ic = ic;
        this.page = page;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        String documentId;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding  = itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            if(chatMessage.conversionImage.equals("skip")){
                binding.imageProfile.setImageResource(R.drawable.icon_person);
            }else {
                try {
                    byte[] imageBytes = Base64.decode(chatMessage.conversionImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    binding.imageProfile.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                if(page == 0){
                    Intent intent1 = new Intent(context, Chat.class);
                    intent1.putExtra("name",chatMessage.conversionName);
                    intent1.putExtra("userId",chatMessage.conversionId);
                    intent1.putExtra("countId",documentId);
                    context.startActivity(intent1);
                }else{
                    Intent intent1 = new Intent(context, Channel.class);
                    intent1.putExtra("channelName",chatMessage.conversionName);
                    intent1.putExtra("channelID",chatMessage.conversionId);
                    context.startActivity(intent1);
                }
            });

            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection("countMessage")
                    .whereEqualTo("receiverId", ic)
                    .whereEqualTo("senderId", chatMessage.conversionId)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            // Handle error
                            return;
                        }
                        if (querySnapshot != null) {
                            for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                                documentId = change.getDocument().getId();
                                if (change.getType() == DocumentChange.Type.REMOVED) {
                                    binding.count.setVisibility(View.GONE);
                                    binding.count.setText(null);
                                    binding.textRecentMessage.setTypeface(null, Typeface.NORMAL);
                                    binding.textName.setTypeface(null,Typeface.NORMAL);
                                } else if (change.getType() == DocumentChange.Type.ADDED ||
                                        change.getType() == DocumentChange.Type.MODIFIED) {
                                    Long countValue = change.getDocument().getLong("count");
                                    int count = (countValue != null) ? countValue.intValue() : 0;
                                    if (count != 0) {
                                        if (count >= 100) {
                                            binding.count.setVisibility(View.VISIBLE);
                                            binding.count.setText("99+");
                                            binding.textRecentMessage.setTypeface(null, Typeface.BOLD);
                                            binding.textName.setTypeface(null,Typeface.BOLD);
                                        } else {
                                            binding.count.setVisibility(View.VISIBLE);
                                            binding.count.setText(String.valueOf(count));
                                            binding.textRecentMessage.setTypeface(null, Typeface.BOLD);
                                            binding.textName.setTypeface(null,Typeface.BOLD);
                                        }
                                    }else{
                                        binding.count.setVisibility(View.GONE);
                                        binding.count.setText(null);
                                        binding.textRecentMessage.setTypeface(null, Typeface.NORMAL);
                                        binding.textName.setTypeface(null,Typeface.NORMAL);
                                    }
                                }
                            }
                        }
                    });
        }

    }
}
