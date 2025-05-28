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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.um.linkcamp.Profile_view;
import com.um.linkcamp.R;
import com.um.linkcamp.View_Work;

import java.util.List;

import model.Work;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.ViewHolder>{
    public List<Work> mWork;
    public Context mContext;
    public WorkAdapter(Context mContext, List<Work> mWork){
        this.mWork = mWork;
        this.mContext = mContext;

    }
    @NonNull
    @Override
    public WorkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.work_item, parent, false);
        return new WorkAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkAdapter.ViewHolder holder, int position) {
        Work work = mWork.get(position);
        publisherInfo(holder.profile,work.getPublisher());
        holder.title.setText(work.getTitle());
        holder.job.setText(work.getJob());
        holder.salary.setText(work.getSalary());
        holder.time.setText(work.getTimestamp());
        holder.location.setText(work.getLocation());
        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, View_Work.class);
            intent.putExtra("workID",work.getId());
            mContext.startActivity(intent);
        });
        holder.profile.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, Profile_view.class);
            intent.putExtra("UserID",work.getPublisher());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mWork.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView title,job,salary,location,time;
        Button view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile_post);
            title = itemView.findViewById(R.id.title);
            job = itemView.findViewById(R.id.job_title);
            salary = itemView.findViewById(R.id.salary);
            location = itemView.findViewById(R.id.location);
            time = itemView.findViewById(R.id.time);
            view = itemView.findViewById(R.id.btnView);

        }
    }
    private void publisherInfo(ImageView image_profile,String userId){
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
            }
        });
    }
}
