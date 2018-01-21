package com.mobnetic.coinguardiandatamodule.hieu;

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

        new MainActivity().setDataWidget(context);
        for (int i = 0; i < appWidgetIds.length; i++) {
            int currentWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

            appWidgetManager.updateAppWidget(currentWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}
