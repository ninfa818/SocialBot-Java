package com.hostcart.socialbot.fragments;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.hidely.hidelyviews.HidelyImageView;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.CallsAdapter;
import com.hostcart.socialbot.model.realms.FireCall;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.PerformCall;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;


public class CallsFragment extends BaseFragment implements ActionMode.Callback, CallsAdapter.OnClickListener {
    private RecyclerView rvCalls;

    private RealmResults<FireCall> fireCallList;
    private List<FireCall> selectedFireCallListActionMode = new ArrayList<>();

    private CallsAdapter adapter;
    private ActionMode actionMode;

    @Override
    public boolean showAds() {
        return getResources().getBoolean(R.bool.is_calls_ad_enabled);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            view = inflater.inflate(R.layout.fragment_calls_dark, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_calls_light, container, false);
        }

        rvCalls = view.findViewById(R.id.rv_calls);
        AdView adView = view.findViewById(R.id.ad_view);
        adViewInitialized(adView);
        initAdapter();

        return view;
    }

    private void initAdapter() {
        fireCallList = RealmHelper.getInstance().getAllCalls();
        adapter = new CallsAdapter(fireCallList, selectedFireCallListActionMode, getActivity(), CallsFragment.this);
        rvCalls.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCalls.setAdapter(adapter);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        this.actionMode = actionMode;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            actionMode.getMenuInflater().inflate(R.menu.menu_action_calls_dark, menu);
        } else {
            actionMode.getMenuInflater().inflate(R.menu.menu_action_calls_light, menu);
        }
        actionMode.setTitle("1");
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (actionMode != null && menuItem != null) {
            if (menuItem.getItemId() == R.id.menu_item_delete)
                deleteClicked();
        }
        return true;
    }

    private void deleteClicked() {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogDark);
            dialog.setTitle(R.string.confirmation);
            dialog.setMessage(R.string.delete_calls_confirmation);
            dialog.setNeutralButton(R.string.no, null);
            dialog.setNegativeButton(R.string.yes, (dialogInterface, i) -> {
                for (FireCall fireCall : selectedFireCallListActionMode) {
                    RealmHelper.getInstance().deleteCall(fireCall);
                }
                exitActionMode();
            });
            dialog.show();
        } else {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogLight);
            dialog.setTitle(R.string.confirmation);
            dialog.setMessage(R.string.delete_calls_confirmation);
            dialog.setNeutralButton(R.string.no, null);
            dialog.setNegativeButton(R.string.yes, (dialogInterface, i) -> {
                for (FireCall fireCall : selectedFireCallListActionMode) {
                    RealmHelper.getInstance().deleteCall(fireCall);
                }
                exitActionMode();
            });
            dialog.show();
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        selectedFireCallListActionMode.clear();
        adapter.notifyDataSetChanged();
    }

    private void itemRemovedFromActionList(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        selectedFireCallListActionMode.remove(fireCall);
        if (selectedFireCallListActionMode.isEmpty()) {
            actionMode.finish();
        } else {
            selectedCircle.hide();
            itemView.setBackgroundColor(-1);
            actionMode.setTitle(selectedFireCallListActionMode.size() + "");
        }
    }

    private void itemAddedToActionList(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        selectedCircle.show();
        itemView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        selectedFireCallListActionMode.add(fireCall);
        actionMode.setTitle(selectedFireCallListActionMode.size() + "");
    }

    public boolean isActionModeNull() {
        return actionMode == null;
    }

    public void exitActionMode() {
        if (actionMode != null)
            actionMode.finish();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && actionMode != null)
            actionMode.finish();
    }

    @Override
    public void onQueryTextChange(String newText) {
        super.onQueryTextChange(newText);
        if (adapter != null) {
            adapter.filter(newText);
        }
    }

    @Override
    public void onSearchClose() {
        super.onSearchClose();
        adapter = new CallsAdapter(fireCallList, selectedFireCallListActionMode, getActivity(), CallsFragment.this);
        rvCalls.setAdapter(adapter);
    }


    @Override
    public void onItemClick(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        if (actionMode != null) {
            if (selectedFireCallListActionMode.contains(fireCall))
                itemRemovedFromActionList(selectedCircle, itemView, fireCall);
            else
                itemAddedToActionList(selectedCircle, itemView, fireCall);
        } else if (fireCall.getUser() != null && fireCall.getUser().getUid() != null)
            new PerformCall(getActivity()).performCall(fireCall.isVideo(), fireCall.getUser().getUid());
    }

    @Override
    public void onIconButtonClick(View view, FireCall fireCall) {
        if (actionMode != null) return;

        if (fireCall.getUser() != null && fireCall.getUser().getUid() != null)
            new PerformCall(getActivity()).performCall(fireCall.isVideo(), fireCall.getUser().getUid());
    }

    @Override
    public void onLongClick(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        if (actionMode == null) {
            fragmentCallback.startTheActionMode(CallsFragment.this);
            itemAddedToActionList(selectedCircle, itemView, fireCall);
        }
    }

}
