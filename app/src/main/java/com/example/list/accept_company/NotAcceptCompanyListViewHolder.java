package com.example.list.accept_company;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class  NotAcceptCompanyListViewHolder extends RecyclerView.ViewHolder {

    private Long id;
    private TextView name;
    private TextView location;
    private Button acceptButton;
    private Button refusalButton;

    public NotAcceptCompanyListViewHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.not_accept_company_name);
        location = itemView.findViewById(R.id.not_accept_company_location);
        acceptButton = itemView.findViewById(R.id.accept_button);
        refusalButton = itemView.findViewById(R.id.refusal_button);


    }
}
