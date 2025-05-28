package Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.um.linkcamp.R;

import java.util.List;

import model.Tutor;


public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.ViewHolder> {

    public List<Tutor> mTutor;
    public Context mContext;

    public TutorAdapter(Context mContext, List<Tutor> mTutor){
        this.mTutor = mTutor;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public TutorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.tutor_display, parent, false);
        return new TutorAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorAdapter.ViewHolder holder, int position) {
        Tutor tutor = mTutor.get(position);
        if("skip".equals(tutor.getProfile())){
            holder.profile.setImageResource(R.drawable.icon_person);
        }else{
            try {
                byte[] imageBytes = Base64.decode(tutor.getProfile(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.profile.setImageBitmap(bitmap);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        holder.name.setText(tutor.getName());
        holder.major.setText(tutor.getMajor());

    }

    @Override
    public int getItemCount() {
        return mTutor.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView name,major;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile);
            name = itemView.findViewById(R.id.name);
            major = itemView.findViewById(R.id.major);
        }
    }
}
