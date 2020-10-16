package com.hostcart.socialbot.services;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

import com.hostcart.socialbot.events.SyncContactsFinishedEvent;
import com.hostcart.socialbot.utils.ContactUtils;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;

public class SyncContactsService extends IntentService {


    //Required Constructor
    public SyncContactsService() {
        super("SyncContactsService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null && intent.getAction() != null && intent.getAction().equals(IntentUtils.INTENT_ACTION_SYNC_CONTACTS)) {
            ContactUtils.syncContacts(this, new ContactUtils.OnContactSyncFinished() {
                @Override
                public void onFinish() {
                    //update ui when sync is finished
                    EventBus.getDefault().post(new SyncContactsFinishedEvent());
                    //to prevent initial sync contacts when the app is launched for first time
                    SharedPreferencesManager.setContactSynced(true);
                    stopSelf();
                }
            });
        }
    }


}
