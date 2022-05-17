package ru.turbopro.didouble;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ru.turbopro.didouble.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ru.turbopro.didouble.ChatAdapter.MyViewHolder> {

  private List<Message> messageList;
  private Activity activity;

  public ChatAdapter(List<Message> messageList, Activity activity) {
    this.messageList = messageList;
    this.activity = activity;
  }

  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);
    return new MyViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    String message = messageList.get(position).getMessage();
    String time = messageList.get(position).getTime();
    String date = messageList.get(position).getDate();
    String beforeDate;
    if (position > 0) {
      beforeDate = messageList.get(position - 1).getDate();
      if (date.equals(beforeDate)) holder.messageDate.setVisibility(View.GONE);
      else holder.messageDate.setVisibility(View.VISIBLE);
      holder.messageDate.setText(date);
    } else
      holder.messageDate.setText(date);
    boolean isReceived = messageList.get(position).getIsReceived();
    if (isReceived) {
      holder.layoutSend.setVisibility(View.GONE);
      holder.layoutReceive.setVisibility(View.VISIBLE);
      holder.messageReceive.setText(message);
      holder.messageTimeBot.setText(time);
    } else {
      holder.layoutSend.setVisibility(View.VISIBLE);
      holder.layoutReceive.setVisibility(View.GONE);
      holder.messageSend.setText(message);
      holder.messageTimeSender.setText(time);
    }
  }

  @Override
  public int getItemCount() {
    return messageList.size();
  }

  public void finalUpdateList() {
    messageList.clear();
    notifyDataSetChanged();
    messageList = copyMessageList;
  }

  public void filter(String text) {
    List<Message> temp = new ArrayList();
    for (Message d : messageList)
      if (d.getMessage().contains(text)) temp.add(d);

    //update recyclerview
    messageList = temp;
    notifyDataSetChanged();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView messageSend;
    TextView messageReceive;
    TextView messageTimeSender;
    TextView messageTimeBot;
    TextView messageDate;
    ConstraintLayout layoutSend;
    ConstraintLayout layoutReceive;
    RecyclerView recyclerView;

    MyViewHolder(@NonNull View itemView) {
      super(itemView);
      messageSend = itemView.findViewById(R.id.message_send);
      messageReceive = itemView.findViewById(R.id.message_receive);
      messageTimeSender = itemView.findViewById(R.id.time_send);
      messageTimeBot = itemView.findViewById(R.id.time_receive);
      messageDate = itemView.findViewById(R.id.tvDate);
      layoutSend = itemView.findViewById(R.id.constraintLayoutSend);
      layoutReceive = itemView.findViewById(R.id.constraintLayoutReceive);
      recyclerView = itemView.findViewById(R.id.chatView);
    }
  }
}