package com.foad.usama.popularmovies.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;

import com.foad.usama.popularmovies.R;
import com.foad.usama.popularmovies.widget.MoviesAppWidgetProvider;
import com.foad.usama.popularmovies.widget.MoviesWidgetService;

import static com.foad.usama.popularmovies.widget.MoviesAppWidgetProvider.ACTION_REFRESH;

public class MoviesAppWidgetConfig extends AppCompatActivity {
    public static final String SHARED_PRES = "prefs";
    public static final String KEY_BUTTON_TEXT = "keyButtonText";
    public static String CURRENT_SELECTION = "Popular";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText editTextButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_app_widget_config);

        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        editTextButton = findViewById(R.id.edit_text_button);
    }

    public void confirmConfiguration(View view) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        Intent buttonIntent = new Intent(this, MainActivity.class);
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this,
                0, buttonIntent, 0);

        // Retrieve text from EditText
        String buttonText = editTextButton.getText().toString();

        Intent serviceIntent = new Intent(this, MoviesWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        Intent clickIntent = new Intent(this, MoviesAppWidgetProvider.class);
        clickIntent.setAction(ACTION_REFRESH);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this,
                0, clickIntent, 0);

        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.movies_widget);
        remoteViews.setOnClickPendingIntent(R.id.movie_widget_button, buttonPendingIntent);
        remoteViews.setCharSequence(R.id.movie_widget_button,
                getString(R.string.chr_sequence_set_text), buttonText);
        remoteViews.setRemoteAdapter(R.id.movie_widget_stack_view, serviceIntent);
        remoteViews.setEmptyView(R.id.movie_widget_stack_view, R.id.movie_widget_empty_view);
        remoteViews.setPendingIntentTemplate(R.id.movie_widget_stack_view, clickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        SharedPreferences prefs = getSharedPreferences(SHARED_PRES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BUTTON_TEXT + appWidgetId, buttonText);
        editor.apply();

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    public void setSelectedPopular(View view) {
        setCurrentSelection(getString(R.string.current_selection_popular));
        CURRENT_SELECTION = getString(R.string.current_selection_popular);
    }

    public void setSelectedFavourites(View view) {
        setCurrentSelection(getString(R.string.current_selection_favourites));
        CURRENT_SELECTION = getString(R.string.current_selection_favourites);
    }

    public void setSelectedUpcoming(View view) {
        setCurrentSelection(getString(R.string.current_selection_upcoming));
        CURRENT_SELECTION = getString(R.string.current_selection_upcoming);
    }

    public void setSelectedTopRated(View view) {
        setCurrentSelection(getString(R.string.current_selection_top_rated));
        CURRENT_SELECTION = getString(R.string.current_selection_top_rated);
    }

    public void setSelectedNowPlaying(View view) {
        setCurrentSelection(getString(R.string.current_selection_now_playing));
        CURRENT_SELECTION = getString(R.string.current_selection_now_playing);
    }

    private void setCurrentSelection(String selection) {
        editTextButton.setText(selection);
    }

}
