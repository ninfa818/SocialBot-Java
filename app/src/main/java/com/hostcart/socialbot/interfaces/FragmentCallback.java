package com.hostcart.socialbot.interfaces;

import android.view.ActionMode;

public interface FragmentCallback {

    void addMarginToFab(boolean isAdShowing);
    void startTheActionMode(ActionMode.Callback callback);

}
