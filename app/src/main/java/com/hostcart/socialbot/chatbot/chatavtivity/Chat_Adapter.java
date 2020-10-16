package com.hostcart.socialbot.chatbot.chatavtivity;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by AQEEL on 4/3/2018.
 */

class Chat_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Chat_GetSet> mDataSet;
    String myID;
    private static final int mychat = 1;
    private static final int friendchat = 2;
    Context context;
    Integer today_day=0;

    private OnItemClickListener listener;
    private OnLongClickListener long_listener;

    public interface OnItemClickListener {
        void onItemClick(Chat_GetSet item, View view);
    }

    public interface OnLongClickListener {
        void onLongclick(Chat_GetSet item, View view);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param dataSet Message list
     *      Device id
     */

    Chat_Adapter(List<Chat_GetSet> dataSet, String id, Context context, OnItemClickListener listener, OnLongClickListener long_listener) {
        mDataSet = dataSet;
        this.myID=id;
        this.context=context;
        this.listener = listener;
        this.long_listener=long_listener;
        Calendar cal = Calendar.getInstance();
        today_day = cal.get(Calendar.DAY_OF_MONTH);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewtype) {
        View v = null;
        switch (viewtype){
            // we have 4 type of layout in chat activity text chat of my and other and also
            // image layout of my and other
            case mychat:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bot_chat_my, viewGroup, false);
                Chatviewholder mychatHolder = new Chatviewholder(v);
                return mychatHolder;
            case friendchat:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bot_chat_other, viewGroup, false);
                Chatviewholder friendchatHolder = new Chatviewholder(v);
                return friendchatHolder;

        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Chat_GetSet chat = mDataSet.get(position);
            Chatviewholder chatviewholder=(Chatviewholder) holder;
        // make the group of message by date set the gap of 1 min
        // means message send with in 1 min will show as a group
        if (position != 0) {
            Chat_GetSet chat2 = mDataSet.get(position - 1);
            if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                chatviewholder.datetxt.setVisibility(View.GONE);
            } else {
                chatviewholder.datetxt.setVisibility(View.VISIBLE);
                chatviewholder.datetxt.setText(ChangeDate(chat.getTimestamp()));
            }
            chatviewholder.message.setText(Html.fromHtml(chat.getText()));
            chatviewholder.message.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            chatviewholder.datetxt.setVisibility(View.VISIBLE);
            chatviewholder.datetxt.setText(ChangeDate(chat.getTimestamp()));
            chatviewholder.message.setText(chat.getText());
        }

        chatviewholder.bind(chat, long_listener);
    }

    @Override
    public int getItemViewType(int position) {
        // get the type it view ( given message is from sender or receiver)
         if (mDataSet.get(position).sender_id.equals(myID)) {
            return mychat;
            }
        return friendchat;
    }

    /**
     * Inner Class for a recycler view
     */

    class Chatviewholder extends RecyclerView.ViewHolder {
        TextView message,datetxt,message_seen;
        View view;
        public Chatviewholder(View itemView) {
            super(itemView);
            view = itemView;
            this.message = view.findViewById(R.id.msgtxt);
            this.datetxt=view.findViewById(R.id.datetxt);
            message_seen=view.findViewById(R.id.message_seen);
        }

        public void bind(final Chat_GetSet item, final OnLongClickListener long_listener) {
            message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    long_listener.onLongclick(item,v);
                    return false;
                }
            });
        }
    }

    // change the date into (today ,yesterday and date)
    public String ChangeDate(String date){
        //current date in millisecond
        long currenttime= System.currentTimeMillis();

        //database date in millisecond
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
        long databasedate = 0;
        Date d = null;
        try {
            d = f.parse(date);
            databasedate = d.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
       long difference=currenttime-databasedate;
       if(difference<86400000){
           int chatday= Integer.parseInt(date.substring(0,2));
           SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
           if(today_day==chatday)
           return "Today "+sdf.format(d);
           else if((today_day-chatday)==1)
           return "Yesterday "+sdf.format(d);
       }
       else if(difference<172800000){
           int chatday= Integer.parseInt(date.substring(0,2));
           SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
           if((today_day-chatday)==1)
           return "Yesterday "+sdf.format(d);
       }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy hh:mm a", Locale.getDefault());
        return sdf.format(d);
    }

}
