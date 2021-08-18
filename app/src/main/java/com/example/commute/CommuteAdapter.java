package com.example.commute;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.http.dto.ResponseCommuteListDto;

import java.util.List;

public class CommuteAdapter extends RecyclerView.Adapter<CommuteViewHolder> {

    private List<ResponseCommuteListDto> list;

    public CommuteAdapter(List<ResponseCommuteListDto> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public CommuteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.commute_list, parent, false);

        return new CommuteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommuteViewHolder holder, int position) {
        holder.getCommuteTime().setText(list.get(position).getStartTime() + "\n~\n" + list.get(position).getEndTime());
        holder.setId(list.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

