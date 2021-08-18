package com.example.albaayo.chat;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.http.dto.Id;
import com.example.http.dto.ImageDto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.SimpleViewHolder> {

    private final List<ResponseChatMessage> mDataSet;

    public ChatAdapter(List<ResponseChatMessage> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.company_chat_list, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        if (mDataSet.get(position).getMemberId() == Id.getInstance().getId()) {
                holder.recvLayout.setVisibility(View.GONE);
                holder.dateLayout.setVisibility(View.GONE);
                holder.sendLayout.setVisibility(View.VISIBLE);

                holder.sendText.setText(mDataSet.get(position).getMessage());
                holder.sendTime.setText(mDataSet.get(position).getTime().format(DateTimeFormatter.ofPattern("a h시 m분")));

        } else if (mDataSet.get(position).getMemberId() == null) {

            holder.recvLayout.setVisibility(View.GONE);
            holder.sendLayout.setVisibility(View.GONE);
            holder.dateLayout.setVisibility(View.VISIBLE);

            holder.dateText.setText(mDataSet.get(position).getTime().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일(E)").withLocale(Locale.forLanguageTag("ko"))));

        } else {
            holder.dateLayout.setVisibility(View.GONE);
            holder.sendLayout.setVisibility(View.GONE);
            holder.recvLayout.setVisibility(View.VISIBLE);

            holder.recvName.setText(mDataSet.get(position).getName());
            holder.recvText.setText(mDataSet.get(position).getMessage());
            holder.recvTime.setText(mDataSet.get(position).getTime().format(DateTimeFormatter.ofPattern("a h시 m분")));
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return mDataSet.get(position).hashCode();
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        final ConstraintLayout sendLayout, recvLayout, dateLayout;
        final TextView sendText, sendTime, recvText, recvTime, recvName, dateText;


        public SimpleViewHolder(View itemView) {
            super(itemView);
            sendLayout = itemView.findViewById(R.id.send_chat_layout);
            recvLayout = itemView.findViewById(R.id.recv_chat_layout);
            dateLayout = itemView.findViewById(R.id.date_layout);

            sendText = itemView.findViewById(R.id.send_chat);
            sendTime = itemView.findViewById(R.id.send_time);
            recvText = itemView.findViewById(R.id.recv_chat);
            recvTime = itemView.findViewById(R.id.recv_time);
            recvName = itemView.findViewById(R.id.recv_name);
            dateText = itemView.findViewById(R.id.date_text);

        }
    }

    public void addItem(ResponseChatMessage responseChatMessage){
        mDataSet.add(responseChatMessage);
        notifyItemChanged(mDataSet.size()-1);
//        notifyDataSetChanged();
    }
}
