package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.ChatActivity;
import com.hostcart.socialbot.activities.ContactDetailsActivity;
import com.hostcart.socialbot.model.AudioRecyclerState;
import com.hostcart.socialbot.model.constants.DownloadUploadStat;
import com.hostcart.socialbot.model.constants.MessageStat;
import com.hostcart.socialbot.model.constants.MessageType;
import com.hostcart.socialbot.model.realms.GroupEvent;
import com.hostcart.socialbot.model.realms.Message;
import com.hostcart.socialbot.model.realms.QuotedMessage;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.ListUtil;
import com.hostcart.socialbot.utils.MessageTypeHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.TimeHelper;
import com.hostcart.socialbot.utils.Util;
import com.hostcart.socialbot.views.ProgressWithCancelView;
import com.hostcart.socialbot.vmeet.meeting.MeetingActivity;
import com.hostcart.socialbot.vmeet.utils.AppConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ak.sh.ay.musicwave.MusicWave;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;

import static com.hostcart.socialbot.utils.AdapterHelper.getMessageStatDrawable;
import static com.hostcart.socialbot.utils.AdapterHelper.getPlayIcon;
import static com.hostcart.socialbot.utils.AdapterHelper.getVoiceMessageIcon;
import static com.hostcart.socialbot.utils.AdapterHelper.isSelectedForActionMode;
import static com.hostcart.socialbot.utils.AdapterHelper.shouldEnableCopyItem;
import static com.hostcart.socialbot.utils.AdapterHelper.shouldEnableForwardButton;
import static com.hostcart.socialbot.utils.AdapterHelper.shouldEnableShareButton;
import static com.hostcart.socialbot.utils.AdapterHelper.shouldHideAllItems;

/**
 * Created by Devlomi on 07/08/2017.
 */
public class MessagingAdapter extends RealmRecyclerViewAdapter<Message, RecyclerView.ViewHolder> implements StickyHeaderAdapter, View.OnLongClickListener {
    private OrderedRealmCollection<Message> messages;
    private Context context;
    User user;
    private String myThumbImg;

    private HashMap<String, AudioRecyclerState> audioRecyclerState = new HashMap<>();
    private HashMap<String, Integer> progressHashmap = new HashMap<>();

    //timestamps to implement the date header
    private HashMap<Integer, Long> timestamps = new HashMap<>();
    private List<Message> selectedItemsForActionMode = new ArrayList<>();

    private int lastTimestampPos;

    private ChatActivity activity;
    private boolean isListContainsMedia = false;
    private boolean isGroup;
    private RealmList<User> groupUsers;


    public List<Message> getSelectedItemsForActionMode() {
        return selectedItemsForActionMode;
    }

    public MessagingAdapter(@Nullable OrderedRealmCollection<Message> data, boolean autoUpdate, Context context, User user) {
        super(data, autoUpdate);
        this.messages = data;
        this.context = context;
        this.user = user;
        isGroup = user.isGroupBool();

        if (isGroup)
            groupUsers = user.getGroup().getUsers();

        myThumbImg = SharedPreferencesManager.getThumbImg();
        getDistinctMessagesTimestamps();
        activity = (ChatActivity) context;
        lastTimestampPos = 0;
    }

    //date header
    @Override
    public long getHeaderId(int position) {
        if (timestamps.containsKey(position)) {
            return timestamps.get(position);
        }
        return 0;
    }

    //date header
    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_day, parent, false);
        return new HeaderHolder(view);
    }

    //date header
    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewholder, int position) {
        HeaderHolder mHolder = (HeaderHolder) viewholder;

        long headerId = getHeaderId(position);
        if (headerId == 0)
            mHolder.header.setVisibility(View.GONE);
        else {
            String formatted = TimeHelper.getChatTime(headerId);
            mHolder.header.setText(formatted);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // check the type of view and return holder
        return getHolderByType(parent, viewType);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, int position) {
        //get itemView type
        int type = getItemViewType(position);
        final Message message = messages.get(position);
        //select the message and start action mode
        mHolder.itemView.setOnLongClickListener(view -> {
            onLongClicked(view, mHolder.getAdapterPosition());
            return true;
        });

        //select or un-select the message and start action mode
        mHolder.itemView.setOnClickListener(view -> {
            if (!isInActionMode())
                return;
            if (selectedItemsForActionMode.contains(message))
                itemRemoved(view, message);
            else
                itemAdded(view, message);
        });

        //save selected state for scrolling
        if (isSelectedForActionMode(message, selectedItemsForActionMode))
            setBackgroundColor(mHolder.itemView, true);
        else
            setBackgroundColor(mHolder.itemView, false);

        /**
         * check message type and init holder to user it and set data in the right place for every view
         */

        switch (type) {
            case MessageType.SENT_TEXT:
                SentTextHolder sentTextHolder = (SentTextHolder) mHolder;
                sentTextHolder.bind(message);
                break;

            case MessageType.SENT_IMAGE:
                SentImageHolder sentImageHolder = (SentImageHolder) mHolder;
                sentImageHolder.bind(message);
                break;

            case MessageType.SENT_VOICE_MESSAGE:
                SentVoiceMessageHolder sentVoiceMessageHolder = (SentVoiceMessageHolder) mHolder;
                sentVoiceMessageHolder.bind(message);
                break;

            case MessageType.SENT_VIDEO:
                SentVideoMessageHolder sentVideoMessageHolder = (SentVideoMessageHolder) mHolder;
                sentVideoMessageHolder.bind(message);
                break;

            case MessageType.SENT_FILE:
                SentFileHolder sentFileHolder = (SentFileHolder) mHolder;
                sentFileHolder.bind(message);
                break;

            case MessageType.SENT_AUDIO:
                SentAudioHolder sentAudioHolder = (SentAudioHolder) mHolder;
                sentAudioHolder.bind(message);
                break;

            case MessageType.SENT_CONTACT:
                SentContactHolder sentContactHolder = (SentContactHolder) mHolder;
                sentContactHolder.bind(message);
                break;


            case MessageType.SENT_LOCATION:
                SentLocationHolder sentLocationHolder = (SentLocationHolder) mHolder;
                sentLocationHolder.bind(message);
                break;

            case MessageType.RECEIVED_TEXT:
                ReceivedTextHolder holder = (ReceivedTextHolder) mHolder;
                holder.bind(message);
                break;

            case MessageType.RECEIVED_IMAGE:
                ReceivedImageHolder receivedImageHolder = (ReceivedImageHolder) mHolder;
                receivedImageHolder.bind(message);
                break;

            case MessageType.RECEIVED_VOICE_MESSAGE:
                ReceivedVoiceMessageHolder receivedVoiceMessageHolder = (ReceivedVoiceMessageHolder) mHolder;
                receivedVoiceMessageHolder.bind(message);
                break;

            case MessageType.RECEIVED_VIDEO:
                ReceivedVideoMessageHolder receivedVideoMessageHolder = (ReceivedVideoMessageHolder) mHolder;
                receivedVideoMessageHolder.bind(message);
                break;

            case MessageType.RECEIVED_FILE:
                ReceivedFileHolder receivedFileHolder = (ReceivedFileHolder) mHolder;
                receivedFileHolder.bind(message);
                break;


            case MessageType.RECEIVED_AUDIO:
                ReceivedAudioHolder receivedAudioHolder = (ReceivedAudioHolder) mHolder;
                receivedAudioHolder.bind(message);
                break;

            case MessageType.RECEIVED_CONTACT:
                ReceivedContactHolder receivedContactHolder = (ReceivedContactHolder) mHolder;
                receivedContactHolder.bind(message);
                break;

            case MessageType.RECEIVED_LOCATION:
                ReceivedLocationHolder receivedLocationHolder = (ReceivedLocationHolder) mHolder;
                receivedLocationHolder.bind(message);
                break;

            case MessageType.SENT_DELETED_MESSAGE:
                SentDeletedMessageHolder sentDeletedMessageHolder = (SentDeletedMessageHolder) mHolder;
                sentDeletedMessageHolder.bind(message);
                break;

            case MessageType.RECEIVED_DELETED_MESSAGE:
                ReceivedDeletedMessageHolder receivedDeletedMessageHolder = (ReceivedDeletedMessageHolder) mHolder;
                receivedDeletedMessageHolder.bind(message);
                break;

            case MessageType.GROUP_EVENT:
                GroupEventHolder groupEventHolder = (GroupEventHolder) mHolder;
                groupEventHolder.bind(message);
                break;

            default:
                NotSupportedTypeHolder notSupportedTypeHolder = (NotSupportedTypeHolder) mHolder;
                notSupportedTypeHolder.bind(message);
                break;
        }

    }


    private void seek(SeekBar seekBar, int progress, Message message, TextView tvDuration) {
        String duration = Util.milliSecondsToTimer(progress);

        if (seekBar.getMax() == 100) {
            int max = (int) Util.getMediaLengthInMillis(context, message.getLocalPath());
            if (max == 0) return;//if file not found or missing permissions
            seekBar.setMax(max);
            int realProgress = (max / 100) * progress;
            AudioRecyclerState audioRecyclerState = this.audioRecyclerState.get(message.getMessageId());
            if (audioRecyclerState == null) {
                this.audioRecyclerState.put(message.getMessageId(), new AudioRecyclerState(false, duration, realProgress));
            } else {
                audioRecyclerState.setProgress(realProgress);
            }
        }
        tvDuration.setText(duration);

        activity.seekTo(message.getMessageId(), progress);
    }


    private int getPreviousProgressIfAvailable(String messageId) {
        int progress = -1;
        if (audioRecyclerState.containsKey(messageId))
            progress = audioRecyclerState.get(messageId).getProgress();
        return progress;
    }


    //start action mode and select this message
    private void onLongClicked(View view, int pos) {
        if (messages.get(pos).getType() == MessageType.GROUP_EVENT)
            return;

        if (!isInActionMode()) {
            itemAdded(view, messages.get(pos));
        }

        //handleNewMessage activity
        activity.onActionModeStarted();
        updateToolbarButtons();
    }

    private void itemAdded(View view, Message message) {
        if (message.getType() == MessageType.GROUP_EVENT)
            return;

        if (message.isMediaType()) {
            if (message.getFromId().equals(FireManager.getUid())) {
                isListContainsMedia = true;
            } else {
                if (message.getDownloadUploadStat() == DownloadUploadStat.SUCCESS)
                    isListContainsMedia = true;
            }
        }

        selectedItemsForActionMode.add(message);
        activity.stopAnimation();
        setBackgroundColor(view, true);
        activity.updateActionModeItemsCount(selectedItemsForActionMode.size());
        updateToolbarButtons();
    }


    //hide or show toolbar button in activity depending on conditions
    private void updateToolbarButtons() {
        if (shouldHideAllItems(selectedItemsForActionMode)) {
            activity.hideShareItem();
            activity.hideCopyItem();
            activity.hideForwardItem();
            activity.hideReplyMenuItem();
        } else {
            if (shouldEnableCopyItem(selectedItemsForActionMode))
                activity.showCopyItem();
            else
                activity.hideCopyItem();

            if (shouldEnableForwardButton(selectedItemsForActionMode))
                activity.showForwardItem();
            else
                activity.hideForwardItem();

            if (shouldEnableShareButton(selectedItemsForActionMode))
                activity.showShareItem();
            else
                activity.hideShareItem();

            if (shouldEnableReplyItem())
                activity.showReplyItem();
            else
                activity.hideReplyMenuItem();
        }
    }

    private boolean shouldEnableReplyItem() {
        if (selectedItemsForActionMode.size() != 1)
            return false;

        Message message = selectedItemsForActionMode.get(0);

        if (MessageType.isDeletedMessage(message.getType()))
            return false;

        if (message.isMessageFromMe() && message.getMessageStat() == MessageStat.PENDING)//if it's sent message then check if the message was sent before replying.
            return false;

        if (isGroup) {
            return activity.isGroupActive();
        }

        return true;
    }


    private void itemRemoved(View view, Message message) {
        if (message.isMediaType())
            isListContainsMedia = false;

        setBackgroundColor(view, false);
        selectedItemsForActionMode.remove(message);
        activity.updateActionModeItemsCount(selectedItemsForActionMode.size());

        if (selectedItemsForActionMode.isEmpty()) {
            activity.exitActionMode();
        } else
            updateToolbarButtons();
    }

    public void exitActionMode() {
        selectedItemsForActionMode.clear();
        notifyDataSetChanged();
    }

    //set background color of item if it's selected
    private void setBackgroundColor(View view, boolean isAdded) {
        int addedColor = context.getResources().getColor(R.color.item_selected_background_color);
        int notAddedColor = 0x00000000;
        if (isAdded)
            view.setBackgroundColor(addedColor);
        else
            view.setBackgroundColor(notAddedColor);
    }

    // hide or show some views depending on download/upload state
    private void hideOrShowDownloadLayout(FrameLayout progressLayout, View btnRetry, int stat) {

        switch (stat) {
            case DownloadUploadStat.FAILED:
            case DownloadUploadStat.CANCELLED:
                progressLayout.setVisibility(View.GONE);
                btnRetry.setVisibility(View.VISIBLE);
                break;

            case DownloadUploadStat.LOADING:
                progressLayout.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.GONE);
                break;

            case DownloadUploadStat.SUCCESS:
                progressLayout.setVisibility(View.GONE);
                btnRetry.setVisibility(View.GONE);
                break;
        }
    }

    private void hideOrShowDownloadLayout(FrameLayout progressLayout, View btnRetry, View btnPlay, int stat) {
        switch (stat) {
            case DownloadUploadStat.FAILED:
            case DownloadUploadStat.CANCELLED:
                progressLayout.setVisibility(View.GONE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnRetry.setVisibility(View.VISIBLE);
                break;

            case DownloadUploadStat.LOADING:
                progressLayout.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                btnRetry.setVisibility(View.GONE);
                break;

            case DownloadUploadStat.SUCCESS:
                progressLayout.setVisibility(View.GONE);
                btnRetry.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void hideOrShowDownloadLayout(View downloadLayout, FrameLayout progressLayout, ImageButton playBtn, int stat) {
        switch (stat) {
            case DownloadUploadStat.FAILED:
            case DownloadUploadStat.CANCELLED:
                downloadLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                playBtn.setVisibility(View.GONE);
                break;

            case DownloadUploadStat.LOADING:
                progressLayout.setVisibility(View.VISIBLE);
//                uploadLayout.setVisibility(View.GONE);
                downloadLayout.setVisibility(View.GONE);
                playBtn.setVisibility(View.GONE);
                break;

            case DownloadUploadStat.SUCCESS:
                progressLayout.setVisibility(View.GONE);
                downloadLayout.setVisibility(View.GONE);
                playBtn.setVisibility(View.VISIBLE);
//                uploadLayout.setVisibility(View.GONE);
                break;
        }
    }


    //keep item background as selected when scroll
    private void keepActionModeItemsSelected(View itemView, Message message) {
        if (selectedItemsForActionMode.contains(message)) {
            setBackgroundColor(itemView, true);
        } else {
            setBackgroundColor(itemView, false);
        }
    }

    //start FullScreenActivity with transitions
    private void startImageVideoActivity(String path, User user, String selectedMessageId, View imgView, int pos) {
        onItemClick.onClick(path, user, selectedMessageId, imgView, pos);
    }

    //delete items from database
    //if boolean 'deleteFile' is true then delete the file from device
    public void clearItems() {
        selectedItemsForActionMode.clear();
        isListContainsMedia = false;
    }

    @Override
    public boolean onLongClick(View view) {
        activity.onActionModeStarted();
        return true;
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView header;

        HeaderHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.tv_day);
        }
    }

    class BaseSentMessageHolder extends RecyclerView.ViewHolder {

        TextView tvTime;
        ImageView messageStatImg;

        //Quoted MessageLayouts
        private FrameLayout quotedMessageFrame;
        private View quotedColor;
        private EmojiconTextView tvQuotedName;
        private EmojiconTextView tvQuotedText;
        private ImageView quotedThumb;


        BaseSentMessageHolder(View itemView) {
            super(itemView);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            messageStatImg = (ImageView) itemView.findViewById(R.id.message_stat_img);
            quotedMessageFrame = itemView.findViewById(R.id.quoted_message_frame);
            quotedColor = itemView.findViewById(R.id.quoted_color);
            tvQuotedName = itemView.findViewById(R.id.tv_quoted_name);
            tvQuotedText = itemView.findViewById(R.id.tv_quoted_text);
            quotedThumb = itemView.findViewById(R.id.quoted_thumb);

        }

        public void bind(final Message message) {
            if (tvTime != null)
                tvTime.setText(message.getTime());
            //imgStat (received or read)
            messageStatImg.setImageResource(getMessageStatDrawable(message.getMessageStat()));

            if (quotedMessageFrame != null) {
                if (message.getQuotedMessage() == null) {
                    quotedMessageFrame.setVisibility(View.GONE);
                } else {
                    quotedMessageFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.quoted_sent_background_color));
                    tvQuotedName.setTextColor(ContextCompat.getColor(context, R.color.quoted_sent_text_color));
                    quotedColor.setBackgroundColor(ContextCompat.getColor(context, R.color.quoted_sent_quoted_color));

                    Message quotedMessage = QuotedMessage.quotedMessageToMessage(message.getQuotedMessage());

                    quotedMessageFrame.setVisibility(View.VISIBLE);
                    tvQuotedName.setText(getQuotedUsername(quotedMessage));
                    tvQuotedText.setText(MessageTypeHelper.getMessageContent(quotedMessage, false));
                    setQuotedTextDrawable(quotedMessage, tvQuotedText);

                    if (quotedMessage.getThumb() != null) {
                        quotedThumb.setVisibility(View.VISIBLE);
                        Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(quotedMessage.getThumb())).into(quotedThumb);
                    } else quotedThumb.setVisibility(View.GONE);

                    quotedMessageFrame.setOnClickListener(view -> {
                        if (isInActionMode()) {
                            if (selectedItemsForActionMode.contains(message))
                                itemRemoved(itemView, message);
                            else itemAdded(itemView, message);
                        } else
                            activity.highlightQuotedMessage(message.getQuotedMessage());
                    });
                }
            }
            //keep items selected if action mode is activated while scrolling
            keepActionModeItemsSelected(itemView, message);
        }

    }

    // sent message with type text
    public class SentTextHolder extends BaseSentMessageHolder {
        EmojiconTextView tvMessageContent;

        SentTextHolder(View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);

        }

        @Override
        public void bind(Message message) {
            super.bind(message);
            tvMessageContent.setText(message.getContent());
//            tvMessageContent.setText(Html.fromHtml(message.getContent()));
//            tvMessageContent.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    // sent message with type image
    public class SentImageHolder extends BaseSentMessageHolder {
        ImageView imgMsg;
        LinearLayout linearLayoutImgDownload;
        TextView tvFileSizeImgDownload;
        ProgressWithCancelView progressWithCancelView;

        SentImageHolder(View itemView) {
            super(itemView);
            imgMsg = itemView.findViewById(R.id.img_msg);
            progressWithCancelView = itemView.findViewById(R.id.progress_bar_cancel);
            linearLayoutImgDownload = itemView.findViewById(R.id.linear_layout_img_download);
            tvFileSizeImgDownload = itemView.findViewById(R.id.tv_file_size_img_download);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            tvFileSizeImgDownload.setText(message.getMetadata());
            // if image deleted then show the blurred thumbnail
            if (!FileUtils.isFileExists(message.getLocalPath())) {
                byte[] thumb = BitmapUtils.encodeImageAsBytes(message.getThumb());
                try {
                    Glide.with(context).asBitmap().load(thumb).into(imgMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Glide.with(context).load(message.getLocalPath()).into(imgMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ViewCompat.setTransitionName(imgMsg, message.getMessageId());
            }


            imgMsg.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    startImageVideoActivity(message.getLocalPath(), user, message.getMessageId(), imgMsg, getAdapterPosition());
                }
            });

            hideOrShowDownloadLayout(progressWithCancelView, linearLayoutImgDownload, message.getDownloadUploadStat());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());

                progressWithCancelView.setVisibility(View.VISIBLE);
                progressWithCancelView.setProgress(progress);
            }

            progressWithCancelView.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    activity.cancelDownloadOrUpload(message);
                }
            });

            imgMsg.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            linearLayoutImgDownload.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    activity.upload(message);
                }
            });
        }
    }

    // received message holders
    class BaseReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView userName;

        //Quoted MessageLayouts
        private FrameLayout quotedMessageFrame;
        private View quotedColor;
        private EmojiconTextView tvQuotedName;
        private EmojiconTextView tvQuotedText;
        private ImageView quotedThumb;

        BaseReceivedMessageHolder(View itemView) {
            super(itemView);

            tvTime = itemView.findViewById(R.id.tv_time);
            quotedMessageFrame = itemView.findViewById(R.id.quoted_message_frame);
            quotedColor = itemView.findViewById(R.id.quoted_color);
            tvQuotedName = itemView.findViewById(R.id.tv_quoted_name);
            tvQuotedText = itemView.findViewById(R.id.tv_quoted_text);
            quotedThumb = itemView.findViewById(R.id.quoted_thumb);

            if (isGroup) {
                userName = itemView.findViewById(R.id.tv_username_group);
            }
        }

        public void bind(final Message message) {
            tvTime.setText(message.getTime());
            if (isGroup && userName != null) {
                userName.setVisibility(View.VISIBLE);
                String fromId = message.getFromId();
                User userById = ListUtil.getUserById(fromId, user.getGroup().getUsers());
                if (userById != null) {
                    String name = userById.getUserName();
                    if (name != null)
                        userName.setText(name);
                } else {
                    userName.setText(message.getFromPhone());
                }
            }

            if (quotedMessageFrame != null) {
                if (message.getQuotedMessage() == null) {
                    quotedMessageFrame.setVisibility(View.GONE);
                } else {
                    Message quotedMessage = QuotedMessage.quotedMessageToMessage(message.getQuotedMessage());
                    quotedMessageFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.quoted_received_background_color));
                    tvQuotedName.setTextColor(ContextCompat.getColor(context, R.color.quoted_received_text_color));
                    quotedColor.setBackgroundColor(ContextCompat.getColor(context, R.color.quoted_received_quoted_color));

                    quotedMessageFrame.setVisibility(View.VISIBLE);
                    tvQuotedName.setText(getQuotedUsername(quotedMessage));
                    tvQuotedText.setText(MessageTypeHelper.getMessageContent(quotedMessage, false));
                    setQuotedTextDrawable(quotedMessage, tvQuotedText);

                    if (quotedMessage.getThumb() != null) {
                        quotedThumb.setVisibility(View.VISIBLE);
                        Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(quotedMessage.getThumb())).into(quotedThumb);
                    } else quotedThumb.setVisibility(View.GONE);

                    quotedMessageFrame.setOnClickListener(view -> {
                        if (isInActionMode()) {
                            if (selectedItemsForActionMode.contains(message))
                                itemRemoved(itemView, message);
                            else itemAdded(itemView, message);
                        } else
                            activity.highlightQuotedMessage(message.getQuotedMessage());
                    });
                }
            }

            keepActionModeItemsSelected(itemView, message);
        }
    }

    private void setQuotedTextDrawable(Message quotedMessage, TextView tvQuotedText) {
        if (!quotedMessage.isTextMessage() && MessageTypeHelper.getMessageTypeDrawable(quotedMessage.getType()) != -1) {
            Drawable drawable = context.getResources()
                    .getDrawable(MessageTypeHelper.getMessageTypeDrawable(quotedMessage.getType()));
            drawable.mutate().setColorFilter(ContextCompat.getColor(context, R.color.grey), PorterDuff.Mode.SRC_IN);

            tvQuotedText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        } else
            tvQuotedText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private String getQuotedUsername(Message quotedMessage) {
        String fromId = quotedMessage.getFromId();
        if (fromId.equals(FireManager.getUid()))
            return context.getResources().getString(R.string.you);

        if (isGroup && groupUsers != null) {
            User user = ListUtil.getUserById(fromId, groupUsers);
            if (user != null) {
                return getUserNameOrPhone(user);
            }
            return "";
        }

        return user.getUserName();
    }

    //return Phone number if user name is not exist
    //since a user maybe removed from a group
    private String getUserNameOrPhone(User user) {
        if (user.getUserName() == null || user.getUserName().equals("")) {
            return user.getPhone();
        }
        return user.getUserName();
    }

    // received message with type text
    public class ReceivedTextHolder extends BaseReceivedMessageHolder {
        EmojiconTextView tvMessageContent, tvMessageContentTrans;
        ImageView img_translate;
        boolean isTranslated = false;

        ReceivedTextHolder(View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            tvMessageContentTrans = itemView.findViewById(R.id.tv_message_content_trans);
            img_translate = itemView.findViewById(R.id.img_message_translate);
        }

        @Override
        public void bind(Message message) {
            super.bind(message);
            tvMessageContent.setText(message.getContent());
            if (message.getContent().contains("MEETING ID - ")) {
                tvMessageContent.setOnClickListener(v -> {
                    String[] strings = message.getContent().split("\n\n");
                    AppConstants.MEETING_ID = strings[1].replace("MEETING ID - ", "");
                    AppConstants.NAME = SharedPreferencesManager.getUserName();
                    AppUtils.showOtherActivity(context, MeetingActivity.class, 0);
                });
            }
            img_translate.setOnClickListener(v -> {
                if (isTranslated) {
                    isTranslated = false;
                    tvMessageContentTrans.setVisibility(View.GONE);
                } else {
                    String trans = AppUtils.translate(context, SharedPreferencesManager.getLanguage(), message.getContent());
                    if (!trans.equals(message.getContent())) {
                        tvMessageContentTrans.setVisibility(View.VISIBLE);
                        tvMessageContentTrans.setText(trans);
                        isTranslated = true;
                    } else {
                        Toast.makeText(context, "You can't translate both languages the same.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

// received message with type image
    public class ReceivedImageHolder extends BaseReceivedMessageHolder {
        ImageView imgMsg;
        LinearLayout linearLayoutImgDownload;
        TextView tvFileSizeImgDownload;
        private ProgressWithCancelView progressBarCancel;

        ReceivedImageHolder(View itemView) {
            super(itemView);
            imgMsg = itemView.findViewById(R.id.img_msg);
            linearLayoutImgDownload = itemView.findViewById(R.id.linear_layout_img_download);
            tvFileSizeImgDownload = itemView.findViewById(R.id.tv_file_size_img_download);
            progressBarCancel = itemView.findViewById(R.id.progress_bar_cancel);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            tvFileSizeImgDownload.setText(message.getMetadata());

            //if the image is not downloaded show thumb img
            if (message.getLocalPath() == null) {
                byte[] thumb = BitmapUtils.encodeImageAsBytes(message.getThumb());
                try {
                    Glide.with(context).asBitmap().load(thumb).into(imgMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (!new File(message.getLocalPath()).exists()) {
                // if image deleted from device then show the blurred thumbnail
                byte[] thumb = BitmapUtils.encodeImageAsBytes(message.getThumb());
//            imgMsg.setImageBitmap(thumb);
                try {
                    Glide.with(context).asBitmap().load(thumb).into(imgMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //these try catch exceptions because glide does not support set tag to an image view
                try {
                    Glide.with(context).load(Uri.fromFile(new File(message.getLocalPath()))).into(imgMsg);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                ViewCompat.setTransitionName(imgMsg, message.getMessageId());
            }

            imgMsg.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else
                        itemAdded(itemView, message);
                } else {
                    if (message.getDownloadUploadStat() == DownloadUploadStat.SUCCESS)
                        startImageVideoActivity(message.getLocalPath(), user, message.getMessageId(), imgMsg, getAdapterPosition());
                }
            });

            imgMsg.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });
            linearLayoutImgDownload.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            hideOrShowDownloadLayout(progressBarCancel, linearLayoutImgDownload, message.getDownloadUploadStat());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressBarCancel.setVisibility(View.VISIBLE);
                progressBarCancel.setProgress(progress);
            }

            progressBarCancel.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    activity.cancelDownloadOrUpload(message);
                }
            });

            linearLayoutImgDownload.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else
                    activity.download(message);
            });
        }
    }


    public class ReceivedVoiceMessageHolder extends BaseReceivedMessageHolder {
        public ImageView playBtn;
        public SeekBar seekBar;
        private CircleImageView circleImg;
        public TextView tvDuration;
        private ProgressWithCancelView progressBarCancel;
        private ImageButton btnRetry;
        private ImageView voiceMessageStat;

        ReceivedVoiceMessageHolder(View itemView) {
            super(itemView);
            playBtn = itemView.findViewById(R.id.voice_play_btn);
            seekBar = itemView.findViewById(R.id.voice_seekbar);
            circleImg = itemView.findViewById(R.id.voice_circle_img);

            tvDuration = itemView.findViewById(R.id.tv_duration);
            voiceMessageStat = itemView.findViewById(R.id.voice_message_stat);
            progressBarCancel = itemView.findViewById(R.id.progress_bar_cancel);
            btnRetry = itemView.findViewById(R.id.btn_retry);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            itemView.setOnLongClickListener(view -> {
                onLongClicked(view, getAdapterPosition());
                return true;
            });

            //set initial values
            seekBar.setProgress(0);
            playBtn.setImageResource(getPlayIcon(false));

            loadUserPhoto(message.getFromId(), circleImg);
            tvDuration.setText(message.getMediaDuration());

            hideOrShowDownloadLayout(progressBarCancel, btnRetry, playBtn, message.getDownloadUploadStat());
            voiceMessageStat.setImageResource(getVoiceMessageIcon(message.isVoiceMessageSeen()));

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressBarCancel.setProgress(progress);
            }

            if (audioRecyclerState.containsKey(message.getMessageId())) {
                AudioRecyclerState mAudioRecyclerState = audioRecyclerState.get(message.getMessageId());
                if (mAudioRecyclerState.getCurrentDuration() != null)
                    tvDuration.setText(mAudioRecyclerState.getCurrentDuration());
                if (mAudioRecyclerState.getProgress() != -1)
                    seekBar.setProgress(mAudioRecyclerState.getProgress());
                if (mAudioRecyclerState.getMax() != -1) {
                    int max = mAudioRecyclerState.getMax();
                    seekBar.setMax(max);
                }
                playBtn.setImageResource(getPlayIcon(mAudioRecyclerState.isPlaying()));
            } else {
                playBtn.setImageResource(getPlayIcon(false));
            }

            playBtn.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);

                } else
                    activity.playAudio(message.getMessageId(), message.getLocalPath(), getAdapterPosition(), getPreviousProgressIfAvailable(message.getMessageId()));
            });

            progressBarCancel.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            btnRetry.setOnClickListener(v -> activity.download(message));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        seek(seekBar, progress, message, tvDuration);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    public class SentVoiceMessageHolder extends BaseSentMessageHolder {
        public ImageView playBtn;
        public SeekBar seekBar;
        public CircleImageView circleImg;
        public TextView tvDuration;
        public ProgressWithCancelView progressVoiceMessage;
        public ImageButton btnRetry;
        private ImageView voiceMessageStat;


        SentVoiceMessageHolder(View itemView) {
            super(itemView);
            playBtn = itemView.findViewById(R.id.voice_play_btn);
            seekBar = itemView.findViewById(R.id.voice_seekbar);
            circleImg = itemView.findViewById(R.id.voice_circle_img);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            progressVoiceMessage = itemView.findViewById(R.id.progress_voice_message);
            voiceMessageStat = itemView.findViewById(R.id.voice_message_stat);
            btnRetry = itemView.findViewById(R.id.btn_retry);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            if (myThumbImg != null)
                Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(myThumbImg)).into(circleImg);
            tvDuration.setText(message.getMediaDuration());

            //Set Initial Values
            seekBar.setProgress(0);
            playBtn.setImageResource(getPlayIcon(false));

            hideOrShowDownloadLayout(progressVoiceMessage, btnRetry, playBtn, message.getDownloadUploadStat());
            voiceMessageStat.setImageResource(getVoiceMessageIcon(message.isVoiceMessageSeen()));

            if (audioRecyclerState.containsKey(message.getMessageId())) {
                AudioRecyclerState audioRecyclerState = MessagingAdapter.this.audioRecyclerState.get(message.getMessageId());

                if (audioRecyclerState.getCurrentDuration() != null)
                    tvDuration.setText(audioRecyclerState.getCurrentDuration());
                if (audioRecyclerState.getProgress() != -1) {
                    seekBar.setProgress(audioRecyclerState.getProgress());
                }
                if (audioRecyclerState.getMax() != -1) {
                    int max = audioRecyclerState.getMax();
                    seekBar.setMax(max);
                }

                playBtn.setImageResource(getPlayIcon(audioRecyclerState.isPlaying()));
            }

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressVoiceMessage.setProgress(progress);
            }

            playBtn.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    int progress = getPreviousProgressIfAvailable(message.getMessageId());
                    activity.playAudio(message.getMessageId(), message.getLocalPath(), getAdapterPosition(), progress);
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        seek(seekBar, progress, message, tvDuration);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            progressVoiceMessage.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            btnRetry.setOnClickListener(v -> activity.upload(message));
        }
    }

    public class SentVideoMessageHolder extends BaseSentMessageHolder {
        private ImageView thumbImg;
        private ImageButton btnPlayVideo;
        private ProgressWithCancelView progressBarCancel;
        private TextView tvMediaDuration;
        private Button btnRetry;

        SentVideoMessageHolder(View itemView) {
            super(itemView);
            thumbImg = itemView.findViewById(R.id.thumb_img);
            btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
            progressBarCancel = itemView.findViewById(R.id.progress_bar_cancel);
            btnRetry = itemView.findViewById(R.id.btn_retry);
            tvMediaDuration = itemView.findViewById(R.id.tv_media_duration);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);

            if (message.getDownloadUploadStat() != DownloadUploadStat.SUCCESS) {
                tvMediaDuration.setVisibility(View.GONE);
            } else {
                tvMediaDuration.setVisibility(View.VISIBLE);
                tvMediaDuration.setText(message.getMediaDuration());
            }

            byte[] videoThumb = BitmapUtils.encodeImageAsBytes(message.getVideoThumb());
            Glide.with(context).asBitmap().load(videoThumb).into(thumbImg);

            hideOrShowDownloadLayout(progressBarCancel, btnRetry, message.getDownloadUploadStat());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());

                progressBarCancel.setVisibility(View.VISIBLE);
                progressBarCancel.setProgress(progress);
            }

            btnRetry.setOnClickListener(v -> activity.upload(message));

            progressBarCancel.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);

                }
                activity.cancelDownloadOrUpload(message);
            });

            thumbImg.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            btnPlayVideo.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            thumbImg.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else
                    startImageVideoActivity(message.getLocalPath(), user, message.getMessageId(), thumbImg, getAdapterPosition());
            });

            btnPlayVideo.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else
                    startImageVideoActivity(message.getLocalPath(), user, message.getMessageId(), thumbImg, getAdapterPosition());
            });
        }
    }


    public class ReceivedVideoMessageHolder extends BaseReceivedMessageHolder {
        private ImageView thumbImg;
        private ProgressWithCancelView progressBarCancel;
        private LinearLayout linearLayoutVideoDownload;
        private TextView tvFileSizeVideoDownload;
        private ImageButton btnPlayVideo;
        private LinearLayout container;
        private TextView tvMediaDuration;

        ReceivedVideoMessageHolder(View itemView) {
            super(itemView);
            thumbImg = itemView.findViewById(R.id.thumb_img);
            linearLayoutVideoDownload = itemView.findViewById(R.id.linear_layout_video_download);
            tvFileSizeVideoDownload = itemView.findViewById(R.id.tv_file_size_video_download);
            btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
            container = itemView.findViewById(R.id.container);
            progressBarCancel = itemView.findViewById(R.id.progress_bar_cancel);
            tvMediaDuration = itemView.findViewById(R.id.tv_media_duration);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            container.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            btnPlayVideo.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            linearLayoutVideoDownload.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });


            //set duration
            tvMediaDuration.setText(message.getMediaDuration());

            //Video is not downloaded yet
            //show the blurred thumb
            if (message.getLocalPath() == null) {
                byte[] thumb = BitmapUtils.encodeImageAsBytes(message.getThumb());
                Glide.with(context).asBitmap().load(thumb).into(thumbImg);

                //set video size
                tvFileSizeVideoDownload.setText(message.getMetadata());
            } else {
                //if it's downloaded but the user deleted the file from device
                if (!FileUtils.isFileExists(message.getLocalPath())) {
                    //show the blurred image
                    byte[] thumb = BitmapUtils.encodeImageAsBytes(message.getThumb());
                    Glide.with(context).asBitmap().load(thumb).into(thumbImg);
                } else {
                    //if it's downloaded ,show the Video Thumb (Without blur)
                    byte[] thumb = BitmapUtils.encodeImageAsBytes(message.getVideoThumb());
                    Glide.with(context).asBitmap().load(thumb).into(thumbImg);
                }
            }

            hideOrShowDownloadLayout(linearLayoutVideoDownload, progressBarCancel, btnPlayVideo, message.getDownloadUploadStat());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressBarCancel.setProgress(progress);
            }

            container.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);

                } else {
                    if (message.getDownloadUploadStat() == DownloadUploadStat.SUCCESS) {
                        startImageVideoActivity(message.getLocalPath(), user, message.getMessageId(), thumbImg, getAdapterPosition());
                    }
                }
            });
            btnPlayVideo.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);

                } else if (message.getDownloadUploadStat() == DownloadUploadStat.SUCCESS) {
                    startImageVideoActivity(message.getLocalPath(), user, message.getMessageId(), thumbImg, getAdapterPosition());
                }
            });

            progressBarCancel.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            linearLayoutVideoDownload.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);

                } else
                    activity.download(message);
            });
        }
    }

    public class TimestampHolder extends RecyclerView.ViewHolder {
        private TextView label;

        TimestampHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tv_day);
        }
    }

    public class SentFileHolder extends BaseSentMessageHolder {
        private TextView tvFileSize;
        private TextView tvFileName;
        private TextView tvFileExtension;
        private ProgressWithCancelView progressBarCancel;
        private ImageButton btnRetry;
        private RelativeLayout fileRootContainer;
        private ImageView fileIcon;


        SentFileHolder(View itemView) {
            super(itemView);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileExtension = itemView.findViewById(R.id.tv_file_extension);

            progressBarCancel = itemView.findViewById(R.id.progress_bar_cancel);
            btnRetry = itemView.findViewById(R.id.btn_retry);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileRootContainer = itemView.findViewById(R.id.file_root_container);

        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            String fileExtension = Util.getFileExtensionFromPath(message.getMetadata()).toUpperCase();
            tvFileExtension.setText(fileExtension);
            tvFileName.setText(message.getMetadata());
            tvFileSize.setText(message.getFileSize());
            hideOrShowDownloadLayout(progressBarCancel, btnRetry, fileIcon, message.getDownloadUploadStat());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressBarCancel.setProgress(progress);
            }

            progressBarCancel.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            fileRootContainer.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else
                    activity.onFileClick(message);
            });
            btnRetry.setOnClickListener(view -> activity.upload(message));
        }
    }

    public class ReceivedFileHolder extends BaseReceivedMessageHolder {
        private ImageView fileIcon;
        private TextView tvFileName;
        private TextView tvFileExtension;

        private ProgressWithCancelView progressBarCancel;
        private TextView tvFileSize;
        private ImageButton btnRetry;
        private LinearLayout fileRootContainer;


        ReceivedFileHolder(View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileExtension = itemView.findViewById(R.id.tv_file_extension);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            btnRetry = itemView.findViewById(R.id.btn_retry);
            progressBarCancel = itemView.findViewById(R.id.progress_bar_cancel);
            fileRootContainer = itemView.findViewById(R.id.file_root_container);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            //get file extension
            String fileExtension = Util.getFileExtensionFromPath(message.getMetadata()).toUpperCase();
            tvFileExtension.setText(fileExtension);
            //set file name
            tvFileName.setText(message.getMetadata());

            //file size
            tvFileSize.setText(message.getFileSize());

            hideOrShowDownloadLayout(progressBarCancel, btnRetry, fileIcon, message.getDownloadUploadStat());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressBarCancel.setProgress(progress);
            }

            progressBarCancel.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            fileRootContainer.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    if (message.getDownloadUploadStat() != DownloadUploadStat.SUCCESS)
                        return;
                    activity.onFileClick(message);
                }
            });

            btnRetry.setOnClickListener(view -> activity.download(message));
        }
    }

    public class SentAudioHolder extends BaseSentMessageHolder {
        public MusicWave waveView;
        private ProgressWithCancelView progressVoiceMessage;
        private ImageButton btnRetry;
        public ImageView playBtn;
        public SeekBar seekBar;
        private TextView tvAudioSize;
        public TextView tvDuration;
        public ImageView imgHeadset;

        SentAudioHolder(View itemView) {
            super(itemView);
            waveView = itemView.findViewById(R.id.wave_view);
            progressVoiceMessage = itemView.findViewById(R.id.progress_voice_message);
            btnRetry = itemView.findViewById(R.id.btn_retry);
            playBtn = itemView.findViewById(R.id.voice_play_btn);
            seekBar = itemView.findViewById(R.id.voice_seekbar);
            tvAudioSize = itemView.findViewById(R.id.tv_audio_size);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            imgHeadset = itemView.findViewById(R.id.img_headset);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);

            //Set Initial Values
            seekBar.setProgress(0);
            playBtn.setImageResource(getPlayIcon(false));

            hideOrShowDownloadLayout(progressVoiceMessage, btnRetry, playBtn, message.getDownloadUploadStat());

            if (message.getDownloadUploadStat() != DownloadUploadStat.SUCCESS) {
                tvAudioSize.setVisibility(View.VISIBLE);
                tvAudioSize.setText(message.getMetadata());
            } else {
                tvAudioSize.setVisibility(View.GONE);
            }

            if (audioRecyclerState.containsKey(message.getMessageId())) {
                AudioRecyclerState audioRecyclerState = MessagingAdapter.this.audioRecyclerState.get(message.getMessageId());
                if (audioRecyclerState.getCurrentDuration() != null)
                    tvDuration.setText(audioRecyclerState.getCurrentDuration());

                if (audioRecyclerState.getProgress() != -1)
                    seekBar.setProgress(audioRecyclerState.getProgress());

                if (audioRecyclerState.getMax() != -1) {
                    int max = audioRecyclerState.getMax();
                    seekBar.setMax(max);
                }

                playBtn.setImageResource(getPlayIcon(audioRecyclerState.isPlaying()));
            } else
                tvDuration.setText(message.getMediaDuration());

            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressVoiceMessage.setProgress(progress);
            }

            playBtn.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {


                    int progress = getPreviousProgressIfAvailable(message.getMessageId());
                    activity.playAudio(message.getMessageId(), message.getLocalPath(), getAdapterPosition(), progress);


                }
            });


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        seek(seekBar, progress, message, tvDuration);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            progressVoiceMessage.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            btnRetry.setOnClickListener(v -> activity.upload(message));
        }
    }

    public class ReceivedAudioHolder extends BaseReceivedMessageHolder {
        public MusicWave waveView;
        private ProgressWithCancelView progressVoiceMessage;
        private ImageButton btnRetry;
        public ImageView playBtn;
        public SeekBar seekBar;
        private TextView tvAudioSize;
        public TextView tvDuration;
        public ImageView imgHeadset;

        ReceivedAudioHolder(View itemView) {
            super(itemView);
            waveView = itemView.findViewById(R.id.wave_view);
            progressVoiceMessage = itemView.findViewById(R.id.progress_bar_cancel);
            btnRetry = itemView.findViewById(R.id.btn_retry);
            playBtn = itemView.findViewById(R.id.voice_play_btn);
            seekBar = itemView.findViewById(R.id.voice_seekbar);
            tvAudioSize = itemView.findViewById(R.id.tv_audio_size);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            imgHeadset = itemView.findViewById(R.id.img_headset);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);

            //Set Initial Values
            seekBar.setProgress(0);
            playBtn.setImageResource(getPlayIcon(false));

            hideOrShowDownloadLayout(progressVoiceMessage, btnRetry, playBtn, message.getDownloadUploadStat());

            //if it's sending set the audio size
            if (message.getDownloadUploadStat() != DownloadUploadStat.SUCCESS) {
                tvAudioSize.setVisibility(View.VISIBLE);
                tvAudioSize.setText(message.getMetadata());
            } else {
                //otherwise hide the audio textview
                tvAudioSize.setVisibility(View.GONE);
            }

            if (audioRecyclerState.containsKey(message.getMessageId())) {
                AudioRecyclerState mAudioRecyclerState = audioRecyclerState.get(message.getMessageId());

                if (mAudioRecyclerState.getCurrentDuration() != null)
                    tvDuration.setText(mAudioRecyclerState.getCurrentDuration());

                if (mAudioRecyclerState.getProgress() != -1) {
                    seekBar.setProgress(mAudioRecyclerState.getProgress());
                }

                if (mAudioRecyclerState.getMax() != -1) {
                    int max = mAudioRecyclerState.getMax();
                    seekBar.setMax(max);
                }

                if (mAudioRecyclerState.isPlaying()) {
                    imgHeadset.setVisibility(View.GONE);
                    waveView.setVisibility(View.VISIBLE);
                } else {
                    imgHeadset.setVisibility(View.VISIBLE);
                    waveView.setVisibility(View.GONE);
                }

                playBtn.setImageResource(getPlayIcon(mAudioRecyclerState.isPlaying()));

            } else {
                tvDuration.setText(message.getMediaDuration());
                imgHeadset.setVisibility(View.VISIBLE);
                waveView.setVisibility(View.GONE);
            }

            //Loading Progress
            if (progressHashmap.containsKey(message.getMessageId()) && message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                int progress = progressHashmap.get(message.getMessageId());
                progressVoiceMessage.setProgress(progress);
            }

            playBtn.setOnClickListener(view -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    int progress = getPreviousProgressIfAvailable(message.getMessageId());
                    activity.playAudio(message.getMessageId(), message.getLocalPath(), getAdapterPosition(), progress);
                }
            });


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        seek(seekBar, progress, message, tvDuration);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            //cancel download process
            progressVoiceMessage.setOnClickListener(v -> activity.cancelDownloadOrUpload(message));

            //re-download this
            btnRetry.setOnClickListener(v -> activity.download(message));
        }
    }

    public class SentContactHolder extends BaseSentMessageHolder {
        private RelativeLayout relativeContactInfo;
        private TextView tvContactName;
        private Button btnMessageContact;
        private FrameLayout container;


        SentContactHolder(View itemView) {
            super(itemView);
            relativeContactInfo = itemView.findViewById(R.id.relative_contact_info);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);

            btnMessageContact = itemView.findViewById(R.id.btn_message_contact);
            container = itemView.findViewById(R.id.container);

        }

        @Override
        public void bind(final Message message) {
            super.bind(message);

            tvContactName.setText(message.getContent());

            relativeContactInfo.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    Intent intent = new Intent(context, ContactDetailsActivity.class);
                    intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, message.getMessageId());
                    intent.putExtra(IntentUtils.EXTRA_CHAT_ID, message.getChatId());
                    context.startActivity(intent);
                }
            });

            btnMessageContact.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    activity.onContactBtnMessageClick(message.getContact());
                }
            });

            container.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            relativeContactInfo.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            btnMessageContact.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });
        }
    }

    public class ReceivedContactHolder extends BaseReceivedMessageHolder {
        private RelativeLayout relativeContactInfo;
        private TextView tvContactName;
        private Button btnMessageContact;
        private Button btnAddContact;


        ReceivedContactHolder(View itemView) {
            super(itemView);
            relativeContactInfo = itemView.findViewById(R.id.relative_contact_info);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);
            btnMessageContact = itemView.findViewById(R.id.btn_message_contact);
            btnAddContact = itemView.findViewById(R.id.btn_add_contact);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            //set contact name
            tvContactName.setText(message.getContent());

            //show contact info
            relativeContactInfo.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    Intent intent = new Intent(context, ContactDetailsActivity.class);
                    intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, message.getMessageId());
                    intent.putExtra(IntentUtils.EXTRA_CHAT_ID, message.getChatId());
                    context.startActivity(intent);
                }
            });

            //send a message to this contact if installed this app
            btnMessageContact.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    activity.onContactBtnMessageClick(message.getContact());
                }
            });

            //add this contact to phonebook
            btnAddContact.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    Intent addContactIntent = IntentUtils.getAddContactIntent(message.getContact());
                    context.startActivity(addContactIntent);
                }
            });

            //select this message and start action mode
            relativeContactInfo.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            //select this message and start action mode
            btnAddContact.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });

            //select this message and start action mode
            btnMessageContact.setOnLongClickListener(v -> {
                onLongClicked(itemView, getAdapterPosition());
                return true;
            });
        }
    }


    public class SentLocationHolder extends BaseSentMessageHolder implements OnMapReadyCallback {
        private GoogleMap mGoogleMap;
        private LatLng mMapLocation;
        private TextView placeName;
        private TextView placeAddress;

        SentLocationHolder(View itemView) {
            super(itemView);

            MapView mapView = itemView.findViewById(R.id.map_view);
            placeName = itemView.findViewById(R.id.place_name);
            placeAddress = itemView.findViewById(R.id.place_address);

            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            MapsInitializer.initialize(context);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // If we have mapView data, update the mapView content.
            if (mMapLocation != null) {
                updateMapContents();
            }
        }

        void setMapLocation(LatLng location) {
            mMapLocation = location;

            // If the mapView is ready, update its content.
            if (mGoogleMap != null) {
                updateMapContents();
            }
        }

        void updateMapContents() {
            // Since the mapView is re-used, need to remove pre-existing mapView features.
            mGoogleMap.clear();

            // Update the mapView feature data and camera position.
            mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation, 17f);
            mGoogleMap.moveCamera(cameraUpdate);
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);

            //get latLng to set the map location
            final LatLng latlng = message.getLocation().getLatlng();

            //set place address text
            placeAddress.setText(message.getLocation().getAddress());

            //if the location name is only numbers
            //then hide it,otherwise set the location name
            if (!Util.isNumeric(message.getLocation().getName())) {
                placeName.setText(message.getLocation().getName());
                placeName.setVisibility(View.VISIBLE);
            } else
                placeName.setVisibility(View.GONE);


            //set map location in mapView
            setMapLocation(latlng);
            //set time
            itemView.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {

                    //open this location in maps app (like google maps or uber etc..)
                    Intent openMapIntent = IntentUtils.getOpenMapIntent(message.getLocation());

                    //check if there is a maps app
                    if (openMapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(openMapIntent);
                    }
                }
            });
        }
    }

    public class ReceivedLocationHolder extends BaseReceivedMessageHolder implements OnMapReadyCallback {
        private GoogleMap mGoogleMap;
        private LatLng mMapLocation;
        private TextView placeName;
        private TextView placeAddress;

        ReceivedLocationHolder(View itemView) {
            super(itemView);

            MapView mapView = itemView.findViewById(R.id.map_view);
            placeName = itemView.findViewById(R.id.place_name);
            placeAddress = itemView.findViewById(R.id.place_address);

            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            MapsInitializer.initialize(context);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // If we have mapView data, update the mapView content.
            if (mMapLocation != null) {
                updateMapContents();
            }
        }

        void setMapLocation(LatLng location) {
            mMapLocation = location;

            // If the mapView is ready, update its content.
            if (mGoogleMap != null) {
                updateMapContents();
            }
        }

        @Override
        public void bind(final Message message) {
            super.bind(message);
            final LatLng latlng = message.getLocation().getLatlng();

            placeAddress.setText(message.getLocation().getAddress());

            if (!Util.isNumeric(message.getLocation().getName())) {
                placeName.setText(message.getLocation().getName());
                placeName.setVisibility(View.VISIBLE);
            } else
                placeName.setVisibility(View.GONE);

            setMapLocation(latlng);

            itemView.setOnClickListener(v -> {
                if (isInActionMode()) {
                    if (selectedItemsForActionMode.contains(message))
                        itemRemoved(itemView, message);
                    else itemAdded(itemView, message);
                } else {
                    Intent openMapIntent = IntentUtils.getOpenMapIntent(message.getLocation());
                    if (openMapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(openMapIntent);
                    }
                }
            });


        }

        void updateMapContents() {
            // Since the mapView is re-used, need to remove pre-existing mapView features.
            mGoogleMap.clear();

            // Update the mapView feature data and camera position.
            mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation, 17f);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    class SentDeletedMessageHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;

        SentDeletedMessageHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(Message message) {
            tvTime.setText(message.getTime());
        }

    }

    class ReceivedDeletedMessageHolder extends BaseReceivedMessageHolder {
        ReceivedDeletedMessageHolder(View itemView) {
            super(itemView);
        }
    }

    class GroupEventHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupEvent;

        GroupEventHolder(View itemView) {
            super(itemView);
            tvGroupEvent = itemView.findViewById(R.id.tv_group_event);
        }

        public void bind(Message message) {
            tvGroupEvent.setText(GroupEvent.extractString(message.getContent(), user.getGroup().getUsers()));
        }
    }

    private void getDistinctMessagesTimestamps() {
        for (int i = 0; i < messages.size(); i++) {
            long timestamp = Long.parseLong(messages.get(i).getTimestamp());
            if (i == 0) {
                timestamps.put(i, timestamp);
                lastTimestampPos = i;
            } else {
                long oldTimestamp = Long.parseLong(messages.get(i - 1).getTimestamp());
                if (!TimeHelper.isSameDay(timestamp, oldTimestamp)) {
                    timestamps.put(i, timestamp);
                    lastTimestampPos = i;
                }
            }
        }
    }

    class NotSupportedTypeHolder extends BaseReceivedMessageHolder {
        NotSupportedTypeHolder(View itemView) {
            super(itemView);
        }
    }

    //update timestamps if needed when a new message inserted
    public void messageInserted() {
        int index = messages.size() - 1;
        long newTimestamp = Long.parseLong(messages.get(index).getTimestamp());
        if (timestamps.isEmpty()) {
            timestamps.put(index, newTimestamp);
            lastTimestampPos = index;
            return;
        }

        long lastTimestamp = timestamps.get(lastTimestampPos);
        if (!TimeHelper.isSameDay(lastTimestamp, newTimestamp)) {
            timestamps.put(index, newTimestamp);
            lastTimestampPos = index;
        }
    }

    private boolean isInActionMode() {
        return activity.isInActionMode;
    }

    private void loadUserPhoto(String fromId, final ImageView imageView) {
        //if it's a group load the user image
        if (isGroup && groupUsers != null) {
            User mUser = ListUtil.getUserById(fromId, groupUsers);
            if (mUser != null && mUser.getThumbImg() != null) {
                Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(mUser.getThumbImg())).into(imageView);
            }
        } else {
            if (user.getThumbImg() != null)
                Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).into(imageView);

        }
    }

    private RecyclerView.ViewHolder getHolderByType(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MessageType.DAY_ROW:
                return new MessagingAdapter.TimestampHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_day, parent, false));
            case MessageType.SENT_DELETED_MESSAGE:
                return new MessagingAdapter.SentDeletedMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_deleted_message, parent, false));
            case MessageType.RECEIVED_DELETED_MESSAGE:
                return new MessagingAdapter.ReceivedDeletedMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_deleted_message, parent, false));
            case MessageType.SENT_TEXT:
                return new MessagingAdapter.SentTextHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_message_text, parent, false));
            case MessageType.SENT_IMAGE:
                return new MessagingAdapter.SentImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_message_img, parent, false));
            case MessageType.RECEIVED_TEXT:
                return new MessagingAdapter.ReceivedTextHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_message_text, parent, false));
            case MessageType.RECEIVED_IMAGE:
                return new MessagingAdapter.ReceivedImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_message_img, parent, false));
            case MessageType.SENT_VOICE_MESSAGE:
                return new MessagingAdapter.SentVoiceMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_voice_message, parent, false));
            case MessageType.RECEIVED_VOICE_MESSAGE:
                return new MessagingAdapter.ReceivedVoiceMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_message_voice, parent, false));
            case MessageType.RECEIVED_VIDEO:
                return new MessagingAdapter.ReceivedVideoMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_message_video, parent, false));
            case MessageType.SENT_VIDEO:
                return new MessagingAdapter.SentVideoMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_message_video, parent, false));
            case MessageType.SENT_FILE:
                return new MessagingAdapter.SentFileHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_file, parent, false));
            case MessageType.RECEIVED_FILE:
                return new MessagingAdapter.ReceivedFileHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_file, parent, false));
            case MessageType.SENT_AUDIO:
                return new MessagingAdapter.SentAudioHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_audio, parent, false));
            case MessageType.RECEIVED_AUDIO:
                return new MessagingAdapter.ReceivedAudioHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_audio, parent, false));
            case MessageType.SENT_CONTACT:
                return new MessagingAdapter.SentContactHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_contact, parent, false));
            case MessageType.RECEIVED_CONTACT:
                return new MessagingAdapter.ReceivedContactHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_contact, parent, false));
            case MessageType.SENT_LOCATION:
                return new MessagingAdapter.SentLocationHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent_location, parent, false));
            case MessageType.RECEIVED_LOCATION:
                return new MessagingAdapter.ReceivedLocationHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_received_location, parent, false));
            case MessageType.GROUP_EVENT:
                return new GroupEventHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_group_event, parent, false));
        }
        return new NotSupportedTypeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_not_supported, parent, false));
    }

    public boolean isListContainsMedia() {
        return isListContainsMedia;
    }

    public HashMap<String, AudioRecyclerState> getVoiceMessageStateHashmap() {
        return audioRecyclerState;
    }

    public HashMap<String, Integer> getProgressHashmap() {
        return progressHashmap;
    }

    //this is for image/video onClick so we can handle transitions in the Activity NOT in the Adapter
    public interface OnClickListener {
        void onClick(String path, User user, String selectedMessageId, View imgView, int pos);
    }

    private OnClickListener onItemClick;

    public void setOnItemClick(OnClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

}
