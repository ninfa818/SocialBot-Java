package com.hostcart.socialbot.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.LanguageRecyclerViewAdapter;
import com.hostcart.socialbot.model.Language;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class SettingLanguageActivity extends AppCompatActivity {

    private List<Language> languageList;
    private LanguageRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_setting_language_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_setting_language_light);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Languages");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String language = getIntent().getStringExtra("Language");

        loadLanguage(language);

        RecyclerView languageRecycler = findViewById(R.id.language_recycler);

        adapter = new LanguageRecyclerViewAdapter(this, languageList);
        languageRecycler.setAdapter(adapter);
        languageRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        languageRecycler.setHasFixedSize(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveLanguage();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadLanguage( String defaultLang ) {
        languageList = new ArrayList<>();

        Resources res = getResources();
        String[] languages = res.getStringArray(R.array.language_array);

        for (String s : languages) {
            Language language = new Language();

            language.setLanguage(s);
            if (defaultLang.equals(s))
                language.setCheck(true);
            else
                language.setCheck(false);

            languageList.add(language);
        }
    }

    private void saveLanguage() {
        int position = adapter.getCurrentPosition();
        String lang = languageList.get(position).getLanguage();
        SharedPreferencesManager.saveLanguage(lang);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
