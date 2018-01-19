package com.mobnetic.coinguardiandatamodule.tester;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mobnetic.coinguardian.util.CurrencyPairsMapHelper;

/**
 * Created by PlayGirl on 1/17/2018.
 */

public class AppWidget extends AppWidgetProvider {

    private CurrencyPairsMapHelper currencyPairsMapHelper;
    private String goc;
    private String moi;
    private int marketPos;

    private String price = "";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i = 0; i < appWidgetIds.length; i++) {
            int currentWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

            Intent intent = new Intent(context, SettingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.layout_widget, pendingIntent1);

            appWidgetManager.updateAppWidget(currentWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}
