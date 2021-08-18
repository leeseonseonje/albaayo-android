package com.example.list.accept_company;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.albaayo.EmployerMainPage;
import com.example.albaayo.R;
import com.example.http.dto.CompanyDto;

import java.util.List;

public class CompanyListAdapter extends RecyclerView.Adapter<CompanyListViewHolder> {

    private List<CompanyDto> list;
    private Context context;


    public CompanyListAdapter(List<CompanyDto> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public CompanyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_view, parent, false);

        return new CompanyListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyListViewHolder viewHolder, int position) {

        viewHolder.getName().setText(list.get(position).getName());
        viewHolder.getLocation().setText(list.get(position).getLocation());
        viewHolder.setId(list.get(position).getId());
        String base = list.get(position).getPicture();
        if (base != null) {
            byte[] b = Base64.decode(base, 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            viewHolder.getCompanyPicture().setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
