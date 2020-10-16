package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.Language;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;

public class LanguageRecyclerViewAdapter extends RecyclerView.Adapter<LanguageRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<Language> languages;
    private MyViewHolder prevHolder;
    private int currentPosition;

    public LanguageRecyclerViewAdapter(Context mContext, List<Language> languages) {
        this.mContext = mContext;
        this.languages = languages;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        currentPosition = 0;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.language_item_dark, parent, false));
        } else {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.language_item_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Language language = languages.get(position);
        if( language != null ) {
            holder.languageText.setText(language.getLanguage());

            if( language.isCheck() ) {
                prevHolder = holder;
                holder.checkImage.setVisibility(View.VISIBLE);
            }
            else
                holder.checkImage.setVisibility(View.GONE);

            holder.layout.setOnClickListener(v -> {
                currentPosition = position;
                if( prevHolder != null && prevHolder != holder ) {
                    prevHolder.checkImage.setVisibility(View.GONE);
                }
                holder.checkImage.setVisibility(View.VISIBLE);
                prevHolder = holder;
                SharedPreferencesManager.saveLanguage(language.getLanguage());
            });
        }
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView languageText;
        ImageView checkImage;
        LinearLayout layout;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            languageText = itemView.findViewById(R.id.text_language);
            checkImage = itemView.findViewById(R.id.check_language);
            layout = itemView.findViewById(R.id.language_layout);
        }
    }

}
