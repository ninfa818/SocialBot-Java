package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.ViewStatusActivity;
import com.hostcart.socialbot.model.TextStatus;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.model.realms.UserStatuses;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.views.TextViewWithShapeBackground;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmResults;

import static com.hostcart.socialbot.utils.FontUtil.isFontExists;

public class UserStatusRecyclerViewAdapter extends RecyclerView.Adapter<UserStatusRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private RealmResults<UserStatuses> userStatusesList;
    private RealmResults<UserStatuses> originalList;

    public UserStatusRecyclerViewAdapter(Context mContext, RealmResults<UserStatuses> userStatuses) {
        this.mContext = mContext;
        this.userStatusesList = userStatuses;
        this.originalList = this.userStatusesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.status_item_dark, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserStatuses userStatuses = userStatusesList.get(position);
        if( userStatuses != null ) {
            if (userStatuses.getUser() == null) {

            } else {
                User user = userStatuses.getUser();
                holder.nameText.setText(user.getUserName());

                Glide.with(mContext)
                        .load(user.getPhoto())
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                        .into(holder.userImage);

                TextStatus textStatus = userStatuses.getStatuses().where()
                        .equalTo("userId", userStatuses.getUserId())
                        .findAllAsync().last().getTextStatus();
                if( textStatus == null ) {
                    holder.backgroundImage.setVisibility(View.VISIBLE);
                    holder.textStatus.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .asBitmap()
                            .load(BitmapUtils.encodeImageAsBytes(userStatuses.getStatuses().last().getThumbImg()))
                            .into(holder.backgroundImage);
                    holder.setInitialTextStatusValues();
                } else {
                    holder.backgroundImage.setVisibility(View.GONE);
                    holder.textStatus.setVisibility(View.VISIBLE);

                    holder.textStatus.setText(textStatus.getText());
                    holder.textStatus.setShapeColor(Color.parseColor(textStatus.getBackgroundColor()));
                    holder.setTypeFace(holder.textStatus, textStatus.getFontName());
                }

                holder.storyCardView.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, ViewStatusActivity.class);
                    intent.putExtra(IntentUtils.UID, userStatuses.getUserId());
                    mContext.startActivity(intent);
                });
            }
        }
    }

    public void filter(String query) {
        if (query.trim().isEmpty()) {
            userStatusesList = originalList;
        } else {
            RealmResults<UserStatuses> userStatuses = RealmHelper.getInstance().searchForStatus(query);
            userStatusesList = userStatuses;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userStatusesList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView backgroundImage;
        CircleImageView userImage;
        TextView nameText;
        CardView storyCardView;
        TextViewWithShapeBackground textStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundImage = itemView.findViewById(R.id.back_image);
            userImage = itemView.findViewById(R.id.user_circle);
            nameText = itemView.findViewById(R.id.name_text);
            storyCardView = itemView.findViewById(R.id.story_cardview);
            textStatus = itemView.findViewById(R.id.text_status);
        }

        private void setInitialTextStatusValues() {
            textStatus.setText("");
            textStatus.setShapeColor(Color.BLACK);
        }

        private void setTypeFace(TextView textView, String fontName) {
            if (isFontExists(fontName)) {
                textView.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/" + fontName));
            }
        }
    }

}
