package com.foad.usama.popularmovies.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.foad.usama.popularmovies.R;
import com.foad.usama.popularmovies.ui.MainActivity;

import static com.foad.usama.popularmovies.ui.MoviesAppWidgetConfig.KEY_BUTTON_TEXT;
import static com.foad.usama.popularmovies.ui.MoviesAppWidgetConfig.SHARED_PRES;

public class MoviesAppWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "actionRefresh";
    private static final int REQUEST_CODE = 0;
    private static final int FLAGS = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            Intent buttonIntent = new Intent(context, MainActivity.class);
            PendingIntent buttonPendingIntent = PendingIntent
                    .getActivity(context, REQUEST_CODE, buttonIntent, FLAGS);

            SharedPreferences prefs = context.getSharedPreferences(SHARED_PRES, Context.MODE_PRIVATE);
            String buttonText = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, context.getString(R.string.press_me));

            Intent serviceIntent = new Intent(context, MoviesWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            // Click Intent
            Intent clickIntent = new Intent(context, MoviesAppWidgetProvider.class);
            clickIntent.setAction(ACTION_REFRESH);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context,
                    REQUEST_CODE, clickIntent, FLAGS);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.movies_widget);
            remoteViews.setOnClickPendingIntent(R.id.movie_widget_button, buttonPendingIntent);
            remoteViews.setCharSequence(R.id.movie_widget_button, "setText", buttonText);
            remoteViews.setRemoteAdapter(R.id.movie_widget_stack_view, serviceIntent);
            remoteViews.setEmptyView(R.id.movie_widget_stack_view, R.id.movie_widget_empty_view);
            remoteViews.setPendingIntentTemplate(R.id.movie_widget_stack_view, clickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.movie_widget_stack_view);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.movies_widget);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.movie_widget_stack_view);
        }
        super.onReceive(context, intent);
    }
}
