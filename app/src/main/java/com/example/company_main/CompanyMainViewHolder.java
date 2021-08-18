package com.example.company_main;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.albaayo.location.EmployerLocationShare;
import com.example.albaayo.personalchat.PersonalChat;
import com.example.http.dto.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyMainViewHolder extends RecyclerView.ViewHolder {

    private Long workerId;
    private TextView workersName, workerBirth;
    private TextView role;
    private ConstraintLayout listLayout, roleLayout;
    private ImageView chatButton, locationShareButton;

    public CompanyMainViewHolder(@NonNull View itemView) {
        super(itemView);

        workersName = itemView.findViewById(R.id.worker_name);
        workerBirth = itemView.findViewById(R.id.user_birth_text);
        role = itemView.findViewById(R.id.role_text_view);

        listLayout = itemView.findViewById(R.id.list_layout);
        roleLayout = itemView.findViewById(R.id.role_layout);

        chatButton = itemView.findViewById(R.id.personal_chat_button);
        locationShareButton = itemView.findViewById(R.id.location_share_button);

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(itemView.getContext(), PersonalChat.class);
            intent.putExtra("myMemberId", Id.getInstance().getId());
            intent.putExtra("memberId", workerId);
            intent.putExtra("memberName", workersName.getText().toString());
            itemView.getContext().startActivity(intent);
        });
    }
}
