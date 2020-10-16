package com.hostcart.socialbot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.realms.PhoneNumber;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import io.realm.RealmList;


public class ContactDetailsAdapter extends RecyclerView.Adapter {

    private RealmList<PhoneNumber> contactList;

    public ContactDetailsAdapter(RealmList<PhoneNumber> contactList) {
        this.contactList = contactList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new ContactNumberHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contact_details_dark, parent, false));
        } else {
            return new ContactNumberHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contact_details_light, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ContactNumberHolder mHolder = (ContactNumberHolder) holder;
        PhoneNumber phoneNumber = contactList.get(position);
        mHolder.tvNumber.setText(phoneNumber.getNumber());

        mHolder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(v,holder.getAdapterPosition());
            }
        });

        mHolder.itemView.setOnLongClickListener(v -> {
            if (onItemClick != null)
                onItemClick.onItemLongClick(v,holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    class ContactNumberHolder extends RecyclerView.ViewHolder {
        private TextView tvNumber;

        ContactNumberHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number_details);
        }
    }

    public interface OnItemClick {
        void onItemClick(View view,int pos);
        void onItemLongClick(View view,int pos);
    }

    private OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

}
