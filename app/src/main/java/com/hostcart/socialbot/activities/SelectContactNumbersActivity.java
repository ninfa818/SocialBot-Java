package com.hostcart.socialbot.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.NumbersForContactAdapter;
import com.hostcart.socialbot.model.ExpandableContact;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.ContactUtils;
import com.hostcart.socialbot.utils.IntentUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class SelectContactNumbersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_select_contact_numbers_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_select_contact_numbers_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        RecyclerView rvNumbersForContactSelector = findViewById(R.id.rv_numbers_for_contact_selector);
        FloatingActionButton fabSendContactSelect = findViewById(R.id.fab_send_contact_select);

        if (!getIntent().hasExtra(IntentUtils.EXTRA_CONTACT_LIST))
            return;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.select_numbers);
        }
        List<ExpandableContact> result = getIntent().getParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST);

        final NumbersForContactAdapter adapter = new NumbersForContactAdapter(result);

        //EXPAND ALL GROUPS
        adapter.toggleAllGroups();

        setItemsChecked(adapter);

        rvNumbersForContactSelector.setLayoutManager(new LinearLayoutManager(this));
        rvNumbersForContactSelector.setAdapter(adapter);

        fabSendContactSelect.setOnClickListener(v -> {
            //getting selected numbers from contacts
            List<ExpandableContact> contactNameList = ContactUtils.getContactsFromExpandableGroups(adapter.getGroups());
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST, (ArrayList<? extends Parcelable>) contactNameList);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    //set all numbers as Checked
    private void setItemsChecked(NumbersForContactAdapter adapter) {
        for (int i = 0; i < adapter.getGroups().size(); i++) {
            MultiCheckExpandableGroup group = (MultiCheckExpandableGroup) adapter.getGroups().get(i);
            for (int x = 0; x < group.getItems().size(); x++) {
                group.checkChild(x);
            }
        }
    }

}
