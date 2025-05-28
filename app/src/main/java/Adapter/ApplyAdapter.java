package Adapter;

import static android.graphics.Color.RED;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.View_Resume;

import java.util.List;
import java.util.Objects;

import function.function;

public class ApplyAdapter extends RecyclerView.Adapter<ApplyAdapter.ViewHolder> {
    public List<String> mApp;
    public Context mContext;
    public String title ,name,resume,id;
    public ApplyAdapter(Context mContext,List<String> mApp,String title,String name,String id){
        this.mApp = mApp;
        this.mContext = mContext;
        this.title = title;
        this.name = name;
        this.id = id;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.apply_item, parent, false);
        return new ApplyAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userID = mApp.get(position);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference Ref = db.collection("Users").document(userID);
        Ref.get().addOnSuccessListener(Snapshot -> {
            if(Snapshot.exists()){
                String profile = Snapshot.getString("profile");
                if(profile.equals("skip")){
                    holder.profile.setImageResource(R.drawable.icon_person);
                }else {
                    try {
                        byte[] imageBytes = Base64.decode(profile, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        holder.profile.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                holder.name.setText(Snapshot.getString("name"));
                holder.email.setText(Snapshot.getString("email"));
                holder.name.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, Profile_view.class);
                    intent.putExtra("UserID",userID);
                    mContext.startActivity(intent);
                });
                holder.profile.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, Profile_view.class);
                    intent.putExtra("UserID",userID);
                    mContext.startActivity(intent);
                });
                holder.send.setOnClickListener(v -> {
                    sendmail(Snapshot.getString("email"),Snapshot.getString("name"));
                });
                holder.view.setOnClickListener(v -> {
                    resume(userID,Snapshot.getString("name"),Snapshot.getString("email"));

                });
            }
        });
    }
    public void resume(String userID,String username,String email){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Candidates")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        for (String key : Objects.requireNonNull(document.getData()).keySet()) {
                            if(key.equals(userID)){
                                resume = (String) document.getData().get(key);
                                Intent intent = new Intent(mContext, View_Resume.class);
                                intent.putExtra("Username",username);
                                intent.putExtra("Title",title);
                                intent.putExtra("Name",name);
                                intent.putExtra("Resume",resume);
                                intent.putExtra("Email",email);
                                mContext.startActivity(intent);
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mApp.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView email,name;
        Button send,view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile);
            email = itemView.findViewById(R.id.email);
            name = itemView.findViewById(R.id.name);
            send = itemView.findViewById(R.id.send_email);
            view = itemView.findViewById(R.id.view);
        }
    }
    private void sendmail(String email,String user_name){
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_sendmail);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(mContext.getDrawable(R.drawable.dialog));
        dialog.setCancelable(false);

        Button btnc = dialog.findViewById(R.id.confirm);
        Button btnCancel = dialog.findViewById(R.id.cancel);
        TextInputEditText message = dialog.findViewById(R.id.content);
        TextView detail = dialog.findViewById(R.id.detail);
        btnc.setOnClickListener(v -> {
            String content = Objects.requireNonNull(message.getText()).toString().trim();
            if(content.isEmpty() || content == null){
                detail.setTextColor(RED);
                detail.setText("The content format can be HTML !\nPlease fill in the message!");
            }else{
                content = content.replace("\n", "<br>");
                data.gui gui = new data.gui();
                function.sendEmail(email,gui.work_title(title,name),gui.workshop(content,name,user_name),mContext);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
