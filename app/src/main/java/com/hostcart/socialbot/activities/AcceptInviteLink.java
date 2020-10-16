package com.hostcart.socialbot.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.GroupLinkUtil;
import com.hostcart.socialbot.utils.GroupManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.Util;
import com.hostcart.socialbot.views.AcceptInviteBottomSheet;

public class AcceptInviteLink extends AppCompatActivity implements AcceptInviteBottomSheet.BottomSheetCallbacks {
    String groupId;
    private AcceptInviteBottomSheet bottomSheet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!Util.isOreoOrAbove()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        bottomSheet = new AcceptInviteBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "");

        if (intent.getData() == null || intent.getData().getLastPathSegment() == null) {
            onInvalidLink();
        } else {
            String groupLink = intent.getData().getLastPathSegment();
            GroupLinkUtil.isGroupLinkValid(groupLink, new GroupLinkUtil.GetGroupByLinkCallback() {
                @Override
                public void onFound(final String groupId) {
                    AcceptInviteLink.this.groupId = groupId;
                    User user = RealmHelper.getInstance().getUser(groupId);
                    if (user != null && user.getGroup() != null && user.getGroup().isActive()) {
                        alreadyInGroup();
                        return;
                    }

                    //check if user is banned from group
                    GroupManager.isUserBannedFromGroup(groupId, FireManager.getUid(), new GroupManager.IsUserBannedCallback() {
                        @Override
                        public void onComplete(boolean isBanned) {
                            if (isBanned) {
                                Toast.makeText(AcceptInviteLink.this, R.string.banned_from_group, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                GroupManager.fetchGroupPartialInfo(AcceptInviteLink.this, groupId, new GroupManager.FetchPartialGroupInfoCallback() {
                                    @Override
                                    public void onComplete(User user, int usersCount) {
                                        if (bottomSheet != null)
                                            bottomSheet.showData(user, usersCount);
                                    }

                                    @Override
                                    public void onFailed() { }
                                });
                            }
                        }

                        @Override
                        public void onFailed() {
                            Toast.makeText(AcceptInviteLink.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                @Override
                public void onError() {
                    onInvalidLink();
                }
            });
        }
    }

    private void alreadyInGroup() {
        Toast.makeText(this, R.string.you_are_already_joined_the_group, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onInvalidLink() {
        Toast.makeText(this, getString(R.string.invalid_group_link), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onDismiss() {
        finish();
    }

    @Override
    public void onJoinBtnClick() {
        if (groupId == null) return;
        if (bottomSheet != null) {
            bottomSheet.showLoadingOnJoin();
        }
        GroupManager.fetchAndCreateGroupFromLink(AcceptInviteLink.this, groupId, new GroupManager.OnFetchGroupsComplete() {
            @Override
            public void onComplete(String groupId) {
                Intent mIntent = new Intent(AcceptInviteLink.this, ChatActivity.class);
                mIntent.putExtra(IntentUtils.UID, groupId);
                startActivity(mIntent);
                finish();
            }
        });
    }
}
