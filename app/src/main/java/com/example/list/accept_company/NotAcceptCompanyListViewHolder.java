package com.example.list.accept_company;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.albaayo.WorkerMainPage;
import com.example.http.Http;
import com.example.http.dto.CompanyDto;
import com.example.http.dto.Id;
import com.example.http.dto.RequestAcceptCompanyDto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
