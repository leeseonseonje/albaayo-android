package com.example.list.accept_company;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.EmployerCompanyMain;
import com.example.albaayo.R;
import com.example.albaayo.WorkerCompanyMain;
import com.example.http.dto.Id;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CompanyListViewHolder extends RecyclerView.ViewHolder {

    private Long id;
    private TextView name;
    private TextView location;
    private ImageView companyPicture;
    private ProgressDialog progressDialog;

    public CompanyListViewHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.company_name);
        location = itemView.findViewById(R.id.company_address);
        companyPicture = itemView.findViewById(R.id.company_image);

        progressDialog = new ProgressDialog(itemView.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        itemView.setOnClickListener(v -> {
            progressDialog.show();
            if (Id.getInstance().getRole().equals("ROLE_WORKER")) {
                Intent intent = new Intent(itemView.getContext(), WorkerCompanyMain.class);
                intent.putExtra("companyId", id);
                intent.putExtra("companyName", name.getText());
                intent.putExtra("companyLocation", location.getText());
                itemView.getContext().startActivity(intent);
            } else if (Id.getInstance().getRole().equals("ROLE_EMPLOYER")) {
                Intent intent = new Intent(itemView.getContext(), EmployerCompanyMain.class);
                intent.putExtra("companyId", id);
                intent.putExtra("companyName", name.getText());
                intent.putExtra("companyLocation", location.getText());
                itemView.getContext().startActivity(intent);
            }
            progressDialog.dismiss();
        });
    }
}
