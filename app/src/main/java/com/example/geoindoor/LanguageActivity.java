package com.example.geoindoor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    TextView continueText, selectText;
    Button english, france;
    String lang = "en";


    private void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private String getPersistedData(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, "na");
    }


    public void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        if (continueText != null)
            continueText.setText(resources.getString(R.string.lang_continue));
        if (selectText != null)
            selectText.setText(resources.getString(R.string.lang_select));
        persist(activity, languageCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lang = getPersistedData(this);
        setContentView(R.layout.activity_language);

        continueText = findViewById(R.id.continue_btn);
        selectText = findViewById(R.id.selection_text);

        english = findViewById(R.id.langEnBtn);
        france = findViewById(R.id.langFrBtn);
        if (!lang.equals("na")) {
            Log.d(this.getLocalClassName(), "onCreate: Pre-selected language=" + lang);
            selectLang(lang);
//            setLocale(this, lang);
//            redirectToLogin();
        }
    }

    public void continueBtn(View view) {
        setLocale(this, lang);
        redirectToLogin();
    }

    public void selectedEnglish(View view) {
        lang = "en";
        selectLang(lang);
    }

    public void selectedFrench(View view) {
        lang = "fr";
        selectLang(lang);
    }

    public void redirectToLogin() {
        Intent intent = new Intent(this, LoginForm.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void selectLang(String lang) {
        if (lang.equals("en")) {
            english.setBackgroundResource(R.drawable.round_button);
            english.setTextColor(Color.WHITE);
            france.setBackgroundResource(R.drawable.border_btn);
            france.setTextColor(getColor(R.color.colorPrimary));
        } else {
            france.setBackgroundResource(R.drawable.round_button);
            france.setTextColor(Color.WHITE);
            english.setBackgroundResource(R.drawable.border_btn);
            english.setTextColor(getColor(R.color.colorPrimary));
        }
        setLocale(this, lang);
    }
}