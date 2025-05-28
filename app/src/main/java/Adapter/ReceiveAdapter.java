package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.um.linkcamp.R;
import com.um.linkcamp.databinding.ItemReceiveBinding;

import java.util.List;

import model.Receive;

public class ReceiveAdapter extends RecyclerView.Adapter<ReceiveAdapter.ReceiveViewHolder>{
    private final List<Receive> receiveList;

    public ReceiveAdapter(List<Receive> receiveList) {
        this.receiveList = receiveList;
    }

    @NonNull
    @Override
    public ReceiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReceiveAdapter.ReceiveViewHolder(
                ItemReceiveBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveViewHolder holder, int position) {
        holder.setData(receiveList.get(position));
    }

    @Override
    public int getItemCount() {
        return receiveList.size();
    }


    class ReceiveViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveBinding binding;
        public ReceiveViewHolder(ItemReceiveBinding itemReceiveBinding) {
            super(itemReceiveBinding.getRoot());
            binding = itemReceiveBinding;
        }
        void setData(Receive receive){
            binding.amount.setText("RM " + receive.amount);

            if("Withdraw".equals(receive.userID)){
                binding.title.setText("Withdraw");
                binding.status.setVisibility(View.VISIBLE);
                if(receive.status == 1){
                    binding.status.setImageResource(R.drawable.pending);
                }else if(receive.status == 2){
                    binding.status.setImageResource(R.drawable.acept);
                }else{
                    binding.status.setImageResource(R.drawable.block);
                }
            }else{
                binding.title.setText("Receive");
                binding.status.setVisibility(View.GONE);
            }
            binding.date.setText(receive.date);
        }
    }


}
