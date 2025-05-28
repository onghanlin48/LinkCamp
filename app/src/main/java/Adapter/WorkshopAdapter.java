package Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.um.linkcamp.R;
import com.um.linkcamp.View_Workshop;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import data.DatabaseHelper;
import model.Workshop;

public class WorkshopAdapter extends RecyclerView.Adapter<WorkshopAdapter.ViewHolder> {
    public List<Workshop> mPost;
    public Context mContext;
    Dialog dialog;
    DatabaseHelper databaseHelper;
    public WorkshopAdapter(Context mContext, List<Workshop> mPost){
        this.mPost = mPost;
        this.mContext = mContext;
        this.databaseHelper = new DatabaseHelper(mContext);
        this.dialog = new Dialog(mContext);
    }


    @NonNull
    @Override
    public WorkshopAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.workshop_item, parent, false);
        return new WorkshopAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workshop workshop = mPost.get(position);
        try {
            byte[] imageBytes = Base64.decode(workshop.getCover(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.image_cover.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.title.setText(workshop.getTitle());

        String[] dates = workshop.getDate().split(" - ");
        String sdate;
        if (dates.length == 2) {
            sdate = dates[0].trim();
        } else {
            sdate = workshop.getDate();
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = inputFormat.parse(sdate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        SimpleDateFormat smonth = new SimpleDateFormat("MMM");
        SimpleDateFormat sday = new SimpleDateFormat("dd");
        String month = smonth.format(date);
        String day = sday.format(date);

        month = month.toUpperCase();
        holder.month.setText(month);
        holder.day.setText(day);

        holder.detail.setText(workshop.getStart()+" · "+workshop.getLocation());
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, View_Workshop.class);
            intent.putExtra("WorkshopID",workshop.getId());
            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_cover;
        public TextView month,day,title,detail;
        public Button btnDetail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_cover = itemView.findViewById(R.id.workshop_background);
            month = itemView.findViewById(R.id.workshop_month);
            day = itemView.findViewById(R.id.workshop_day);
            title = itemView.findViewById(R.id.workshop_title);
            detail = itemView.findViewById(R.id.workshop_detail);
            btnDetail = itemView.findViewById(R.id.btnView);
        }
    }
}
