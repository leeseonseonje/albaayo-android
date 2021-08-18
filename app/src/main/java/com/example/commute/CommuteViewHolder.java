package com.example.commute;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommuteViewHolder extends RecyclerView.ViewHolder {

    private Long id;
    private TextView commuteTime;

    public CommuteViewHolder(@NonNull View itemView) {
        super(itemView);

        commuteTime = itemView.findViewById(R.id.commute_time);
    }
}
