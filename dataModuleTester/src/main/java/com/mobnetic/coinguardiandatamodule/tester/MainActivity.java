package com.mobnetic.coinguardiandatamodule.tester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.mobnetic.coinguardian.config.MarketsConfig;
import com.mobnetic.coinguardian.model.CheckerInfo;
import com.mobnetic.coinguardian.model.Futures;
import com.mobnetic.coinguardian.model.FuturesMarket;
import com.mobnetic.coinguardian.model.Market;
import com.mobnetic.coinguardian.model.Ticker;
import com.mobnetic.coinguardian.util.CurrencyPairsMapHelper;
import com.mobnetic.coinguardian.util.FormatUtilsBase;
import com.mobnetic.coinguardian.util.MarketsConfigUtils;
import com.mobnetic.coinguardiandatamodule.tester.dialog.DynamicCurrencyPairsDialog;
import com.mobnetic.coinguardiandatamodule.tester.util.CheckErrorsUtils;
import com.mobnetic.coinguardiandatamodule.tester.util.HttpsHelper;
import com.mobnetic.coinguardiandatamodule.tester.util.MarketCurrencyPairsStore;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerErrorParsedError;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerVolleyMainRequest;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerVolleyMainRequest.TickerWrapper;
import com.mobnetic.coinguardiandatamodule.tester.volley.generic.ResponseErrorListener;
import com.mobnetic.coinguardiandatamodule.tester.volley.generic.ResponseListener;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private Spinner marketSpinner;
    private View currencySpinnersWrapper;
    private View dynamicCurrencyPairsWarningView;
    private View dynamicCurrencyPairsInfoView;
    private Spinner currencyBaseSpinner;
    private Spinner currencyCounterSpinner;
    private Spinner futuresContractTypeSpinner;
    private View getResultButton;
    private ProgressBar progressBar;
    private TextView resultView;

    private CurrencyPairsMapHelper currencyPairsMapHelper;

    ArrayList<String> listCu = new ArrayList<>();
    ArrayList<String> listMoi = new ArrayList<>();
    ArrayList<String> listCho = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = HttpsHelper.newRequestQueue(this);

        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        marketSpinner = (Spinner) findViewById(R.id.marketSpinner);
        currencySpinnersWrapper = findViewById(R.id.currencySpinnersWrapper);
        dynamicCurrencyPairsWarningView = findViewById(R.id.dynamicCurrencyPairsWarningView);
        dynamicCurrencyPairsInfoView = findViewById(R.id.dynamicCurrencyPairsInfoView);
        currencyBaseSpinner = (Spinner) findViewById(R.id.currencyBaseSpinner);
        currencyCounterSpinner = (Spinner) findViewById(R.id.currencyCounterSpinner);
        futuresContractTypeSpinner = (Spinner) findViewById(R.id.futuresContractTypeSpinner);
        getResultButton = findViewById(R.id.getResultButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        resultView = (TextView) findViewById(R.id.resultView);

        refreshMarketSpinner();
        Market market = getSelectedMarket();
        currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(this, getSelectedMarket().key));
        refreshCurrencySpinners(market);
        refreshFuturesContractTypeSpinner(market);
        showResultView(true);

        oldvalue();

        marketSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final Market selectedMarket = getSelectedMarket();
                currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(MainActivity.this, selectedMarket.key));
                refreshCurrencySpinners(selectedMarket);
                refreshFuturesContractTypeSpinner(selectedMarket);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
        });

        dynamicCurrencyPairsInfoView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DynamicCurrencyPairsDialog(MainActivity.this, getSelectedMarket(), currencyPairsMapHelper) {
                    public void onPairsUpdated(Market market, CurrencyPairsMapHelper currencyPairsMapHelper) {
                        MainActivity.this.currencyPairsMapHelper = currencyPairsMapHelper;
                        refreshCurrencySpinners(market);
                    }
                }.show();
            }
        });

        currencyBaseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                refreshCurrencyCounterSpinner(getSelectedMarket());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
        });

        getResultButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getNewResult();
            }
        });
    }


    // ====================
    // Get selected items
    // ====================
    private Market getSelectedMarket() {
        int size = MarketsConfig.MARKETS.size();
        int idx = (size - 1) - marketSpinner.getSelectedItemPosition();
        return MarketsConfigUtils.getMarketById(idx);
    }

    private String getSelectedCurrencyBase() {
        if (currencyBaseSpinner.getAdapter() == null)
            return null;
        return String.valueOf(currencyBaseSpinner.getSelectedItem());
    }

    private String getSelectedCurrencyCounter() {
        if (currencyCounterSpinner.getAdapter() == null)
            return null;
        return String.valueOf(currencyCounterSpinner.getSelectedItem());
    }

    private int getSelectedContractType(Market market) {
        if (market instanceof FuturesMarket) {
            final FuturesMarket futuresMarket = (FuturesMarket) market;
            int selection = futuresContractTypeSpinner.getSelectedItemPosition();
            return futuresMarket.contractTypes[selection];
        }
        return Futures.CONTRACT_TYPE_WEEKLY;
    }


    // ====================
    // Refreshing UI
    // ====================
    private void refreshMarketSpinner() {
        final CharSequence[] entries = new String[MarketsConfig.MARKETS.size()];
        int i = entries.length - 1;
        for (Market market : MarketsConfig.MARKETS.values()) {
            entries[i--] = market.name;
        }
        for (int j = 0; j < entries.length; j++) {
            listCho.add(entries[j] + "");
        }
        marketSpinner.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, entries));
    }

    private void refreshCurrencySpinners(Market market) {
        refreshCurrencyBaseSpinner(market);
        refreshCurrencyCounterSpinner(market);
        refreshDynamicCurrencyPairsView(market);

        final boolean isCurrencyEmpty = getSelectedCurrencyBase() == null || getSelectedCurrencyCounter() == null;
        currencySpinnersWrapper.setVisibility(isCurrencyEmpty ? View.GONE : View.VISIBLE);
        dynamicCurrencyPairsWarningView.setVisibility(isCurrencyEmpty ? View.VISIBLE : View.GONE);
        getResultButton.setVisibility(isCurrencyEmpty ? View.GONE : View.VISIBLE);
    }

    private void refreshDynamicCurrencyPairsView(Market market) {
        dynamicCurrencyPairsInfoView.setVisibility(market.getCurrencyPairsUrl(0) != null ? View.VISIBLE : View.GONE);
    }

    private void refreshCurrencyBaseSpinner(Market market) {
        final HashMap<String, CharSequence[]> currencyPairs = getProperCurrencyPairs(market);
        if (currencyPairs != null && currencyPairs.size() > 0) {
            final CharSequence[] entries = new CharSequence[currencyPairs.size()];
            int i = 0;
            for (String currency : currencyPairs.keySet()) {
                entries[i++] = currency;
                listCu.add(currency + "");
            }
            currencyBaseSpinner.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, entries));
        } else {
            currencyBaseSpinner.setAdapter(null);
        }
    }

    private void refreshCurrencyCounterSpinner(Market market) {
        final HashMap<String, CharSequence[]> currencyPairs = getProperCurrencyPairs(market);
        if (currencyPairs != null && currencyPairs.size() > 0) {
            final String selectedCurrencyBase = getSelectedCurrencyBase();
            final CharSequence[] entriesmoi = currencyPairs.get(selectedCurrencyBase).clone();

            for (int i = 0; i < entriesmoi.length; i++) {
                listMoi.add(entriesmoi[i] + "");
            }

            currencyCounterSpinner.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, entriesmoi));
        } else {
            currencyCounterSpinner.setAdapter(null);
        }
    }

    private void refreshFuturesContractTypeSpinner(Market market) {
        SpinnerAdapter spinnerAdapter = null;
        if (market instanceof FuturesMarket) {
            final FuturesMarket futuresMarket = (FuturesMarket) market;
            CharSequence[] entries = new CharSequence[futuresMarket.contractTypes.length];
            for (int i = 0; i < futuresMarket.contractTypes.length; ++i) {
                int contractType = futuresMarket.contractTypes[i];
                entries[i] = Futures.getContractTypeShortName(contractType);
            }
            spinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, entries);
        }
        futuresContractTypeSpinner.setAdapter(spinnerAdapter);
        futuresContractTypeSpinner.setVisibility(spinnerAdapter != null ? View.VISIBLE : View.GONE);
    }

    private void showResultView(boolean showResultView) {
        getResultButton.setEnabled(showResultView);
        progressBar.setVisibility(showResultView ? View.GONE : View.VISIBLE);
        resultView.setVisibility(showResultView ? View.VISIBLE : View.GONE);
    }

    private HashMap<String, CharSequence[]> getProperCurrencyPairs(Market market) {
        if (currencyPairsMapHelper != null && currencyPairsMapHelper.getCurrencyPairs() != null && currencyPairsMapHelper.getCurrencyPairs().size() > 0)
            return currencyPairsMapHelper.getCurrencyPairs();
        else
            return market.currencyPairs;
    }

    // ====================
    // Get && display results
    // ====================
    private void getNewResult() {
        final Market market = getSelectedMarket();
        final String currencyBase = getSelectedCurrencyBase();
        final String currencyCounter = getSelectedCurrencyCounter();
        final String pairId = currencyPairsMapHelper != null ? currencyPairsMapHelper.getCurrencyPairId(currencyBase, currencyCounter) : null;
        final int contractType = getSelectedContractType(market);
        final CheckerInfo checkerInfo = new CheckerInfo(currencyBase, currencyCounter, pairId, contractType);
        Request<?> request = new CheckerVolleyMainRequest(market, checkerInfo, new ResponseListener<TickerWrapper>() {
            @Override
            public void onResponse(String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String responseString, TickerWrapper tickerWrapper) {
                handleNewResult(checkerInfo, tickerWrapper.ticker, url, requestHeaders, networkResponse, responseString, null, null);
            }
        }, new ResponseErrorListener() {
            @Override
            public void onErrorResponse(String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String responseString, VolleyError error) {
                error.printStackTrace();

                String errorMsg = null;
                if (error instanceof CheckerErrorParsedError) {
                    errorMsg = ((CheckerErrorParsedError) error).getErrorMsg();
                }

                if (TextUtils.isEmpty(errorMsg))
                    errorMsg = CheckErrorsUtils.parseVolleyErrorMsg(MainActivity.this, error);

                handleNewResult(checkerInfo, null, url, requestHeaders, networkResponse, responseString, errorMsg, error);
            }
        });
        requestQueue.add(request);
        showResultView(false);
    }

    private void handleNewResult(CheckerInfo checkerInfo, Ticker ticker, String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String rawResponse, String errorMsg, VolleyError error) {
        showResultView(true);
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if (ticker != null) {
            ssb.append(getString(R.string.ticker_last, FormatUtilsBase.formatPriceWithCurrency(ticker.last, checkerInfo.getCurrencyCounter())));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_high, ticker.high, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_low, ticker.low, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_bid, ticker.bid, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_ask, ticker.ask, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_vol, ticker.vol, checkerInfo.getCurrencyBase()));
            ssb.append("\n" + getString(R.string.ticker_timestamp, FormatUtilsBase.formatSameDayTimeOrDate(this, ticker.timestamp)));
        } else {
            ssb.append(getString(R.string.check_error_generic_prefix, errorMsg != null ? errorMsg : "UNKNOWN"));
        }

        CheckErrorsUtils.formatResponseDebug(this, ssb, url, requestHeaders, networkResponse, rawResponse, error);


        String base = currencyCounterSpinner.getSelectedItem().toString();

        String x = ssb + "";
        String price1 = x.substring(6, x.indexOf(base)).trim();
        price1 = price1.replace(",", ".");


        TextView txtCount = (TextView) findViewById(R.id.txt_count);


//		resultView.setText(ssb);
        double z = Double.parseDouble(price1) * Double.parseDouble(txtCount.getText().toString());
        Log.d("xxxx", "price : " + z);

        resultView.setText(String.format("%.10f", z));

    }

    private String createNewPriceLineIfNeeded(int textResId, double price, String currency) {
        if (price <= Ticker.NO_DATA)
            return "";

        return "\n" + getString(textResId, FormatUtilsBase.formatPriceWithCurrency(price, currency));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                return true;

        }
        return false;
    }

    String cho;
    String goc;
    String moi;

    public void oldvalue() {
        SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);

        cho = sharedPreferences.getString("cho", "Kucoin");
        goc = sharedPreferences.getString("goc", "ACT");
        moi = sharedPreferences.getString("moi", "BTC");

        currencyBaseSpinner.setSelection(listCu.indexOf(goc));
        currencyCounterSpinner.setSelection(listMoi.indexOf(moi));
        marketSpinner.setSelection(listCu.indexOf(cho));

    }


    public void setDataWidget(String s) {

        // Getting an instance of WidgetManager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        // Instantiating the class RemoteViews with widget_layout
        RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.layout_widget);


        // Setting the background color of the widget
//        views.setTextViewText(R.id.txt_market, goc + " / " + moi);

        views.setTextViewText(R.id.txt_market, goc + " / " + moi);
        views.setTextViewText(R.id.txt_price, s + "");

        Intent intent = new Intent(getApplicationContext(), MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        views.setOnClickPendingIntent(R.id.imgbtn_refret, pendingIntent);

        ComponentName thisWidget = new ComponentName(getApplication(), AppWidget.class);
        //  Attach an on-click listener to the clock
//        views.setOnClickPendingIntent(R.id.widget_aclock, pendingIntent);

        // Tell the AppWidgetManager to perform an update on the app widget
        appWidgetManager.updateAppWidget(thisWidget, views);


    }

    public static class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("zzzz", "receiver");


            SharedPreferences sharedPreferences = context.getSharedPreferences("share", MODE_PRIVATE);

            String cho = sharedPreferences.getString("cho", "Kucoin");
            String goc = sharedPreferences.getString("goc", "ACT");
            String moi = sharedPreferences.getString("moi", "BTC");


        }
    }

}
