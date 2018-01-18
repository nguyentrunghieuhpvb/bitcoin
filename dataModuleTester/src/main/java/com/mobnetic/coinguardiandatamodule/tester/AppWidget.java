package com.mobnetic.coinguardiandatamodule.tester;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

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
//        for (int i = 0; i < appWidgetIds.length; i++) {
//            int currentWidgetId = appWidgetIds[i];
//
//            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
//
//
//            SharedPreferences preferences = context.getSharedPreferences("share", Context.MODE_PRIVATE);
//            goc = preferences.getString("goc", "ACT");
//            moi = preferences.getString("moi", "BTC");
//
//            marketPos = preferences.getInt("marketPos", 0);
//
//
//            currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(context, getSelectedMarket().key));
//
//            views.setTextViewText(R.id.txt_market, goc + " => " + moi);
//
//            price = MainActivity.price;
//
//
//            views.setTextViewText(R.id.txt_price, price + "");
//
//            appWidgetManager.updateAppWidget(currentWidgetId, views);
//            Toast.makeText(context, "widget added", Toast.LENGTH_SHORT).show();
//
//
//        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

    }
}
