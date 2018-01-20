package com.mobnetic.coinguardiandatamodule.tester;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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
import com.mobnetic.coinguardiandatamodule.tester.geticon.Coin;
import com.mobnetic.coinguardiandatamodule.tester.geticon.ParseJson;
import com.mobnetic.coinguardiandatamodule.tester.util.CheckErrorsUtils;
import com.mobnetic.coinguardiandatamodule.tester.util.HttpsHelper;
import com.mobnetic.coinguardiandatamodule.tester.util.MarketCurrencyPairsStore;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerErrorParsedError;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerVolleyMainRequest;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerVolleyMainRequest.TickerWrapper;
import com.mobnetic.coinguardiandatamodule.tester.volley.DynamicCurrencyPairsVolleyMainRequest;
import com.mobnetic.coinguardiandatamodule.tester.volley.generic.ResponseErrorListener;
import com.mobnetic.coinguardiandatamodule.tester.volley.generic.ResponseListener;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    private View getResultButton;


    private CurrencyPairsMapHelper currencyPairsMapHelper;

    ArrayList<String> listCu = new ArrayList<>();
    ArrayList<String> listMoi = new ArrayList<>();
    ArrayList<String> listCho = new ArrayList<>();

    Realm realm;

    ArrayList<DataInfo> listFavo = new ArrayList<>();

    boolean favoCheck = false;

    TextView txtMarket, txtBase;
    EditText edtCount;

    TextView txtResult;
    ProgressBar progressBar;
    Button btnShowResult;
    RecyclerView recy;
    FavoAdapter favoAdapter;

    ArrayList<String> listMarket = new ArrayList<>();

    String goc = "", moi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestQueue = HttpsHelper.newRequestQueue(this);

        setContentView(R.layout.layout_convert);

        inIt();
        getListMarket();

//        setDataWidget(MainActivity.this);

        Realm.init(this);

        currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(this, getSelectedMarket(txtMarket.getText().toString()).key));

        realm = Realm.getDefaultInstance();

        listFavo = getListFavo();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        favoAdapter = new FavoAdapter(listFavo, new FavoAdapter.onClick() {
            @Override
            public void onClick(final DataInfo dataInfo) {
                Log.d("zzzz", "onclick");

                txtMarket.setText(dataInfo.getMarket());

                currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(MainActivity.this, getSelectedMarket(txtMarket.getText().toString()).key));

                goc = dataInfo.getGoc();
                moi = dataInfo.getMoi();

                txtBase.setText(goc + " / " + moi);

                txtResult.setText("");

            }
        });
        favoAdapter.notifyDataSetChanged();

        recy.setLayoutManager(new LinearLayoutManager(this));
        recy.setAdapter(favoAdapter);


        btnShowResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getNewResult(false);

                String count = edtCount.getText().toString().trim().replaceAll(",", ".");
                try {
                    final double tmp = Double.parseDouble(count);
                    new GetPrice(getApplicationContext()) {

                        @Override
                        public void result(Double result) {
                            txtResult.setText(String.format("%.8f", result * tmp));
                        }
                    }.getNewResult();


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }
        });

//        getNewResult(true);

    }

    private void inIt() {
        txtMarket = (TextView) findViewById(R.id.txt_market);
        txtBase = (TextView) findViewById(R.id.txt_base);
        txtResult = (TextView) findViewById(R.id.txt_result);
        btnShowResult = (Button) findViewById(R.id.btn_get_result);
        recy = (RecyclerView) findViewById(R.id.recy_favo);
        edtCount = (EditText) findViewById(R.id.edt_count);


        SharedPreferences preferences = getSharedPreferences("share", MODE_PRIVATE);
        String market = preferences.getString("market", "Kucoin");
        goc = preferences.getString("goc", "ACT");
        moi = preferences.getString("moi", "BCH");

        txtMarket.setText(market);
        txtBase.setText(goc + " / " + moi);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences("share", MODE_PRIVATE);
        String market = preferences.getString("market", "Kucoin");
        goc = preferences.getString("goc", "ACT");
        moi = preferences.getString("moi", "BCH");

        txtMarket.setText(market);
        txtBase.setText(goc + " / " + moi);

        setDataWidget(MainActivity.this);
    }


    private Market getSelectedMarket(String marketName) {
        int size = MarketsConfig.MARKETS.size();
//        int idx = (size - 1) - marketSpinner.getSelectedItemPosition();
        int idx = (size - 1) - listMarket.indexOf(marketName);
        return MarketsConfigUtils.getMarketById(idx);
    }


    private int getSelectedContractType(Market market) {
        if (market instanceof FuturesMarket) {
            final FuturesMarket futuresMarket = (FuturesMarket) market;
//            int selection = futuresContractTypeSpinner.getSelectedItemPosition();
//            return futuresMarket.contractTypes[selection];
        }
        return Futures.CONTRACT_TYPE_WEEKLY;
    }

    public ArrayList<DataInfo> getListFavo() {
        ArrayList<DataInfo> list = new ArrayList<>();
        RealmResults<DataInfo> results1 =
                realm.where(DataInfo.class).findAll();

        for (DataInfo c : results1) {
            list.add(c);

        }
        Log.d("zzzz", "size : " + list.size());
        return list;
    }

    // ====================
    // Refreshing UI
    // ====================
    private void getListMarket() {
        final CharSequence[] entries = new String[MarketsConfig.MARKETS.size()];
        int i = entries.length - 1;
        for (Market market : MarketsConfig.MARKETS.values()) {
            entries[i--] = market.name;
        }
        for (int j = 0; j < entries.length; j++) {
            listMarket.add(entries[j] + "");
        }

    }


//    private void showResultView(boolean showResultView) {
//        progressBar.setVisibility(showResultView ? View.GONE : View.VISIBLE);
//        resultView.setVisibility(showResultView ? View.VISIBLE : View.GONE);
//    }

    private HashMap<String, CharSequence[]> getProperCurrencyPairs(Market market) {
        if (currencyPairsMapHelper != null && currencyPairsMapHelper.getCurrencyPairs() != null && currencyPairsMapHelper.getCurrencyPairs().size() > 0)
            return currencyPairsMapHelper.getCurrencyPairs();
        else
            return market.currencyPairs;
    }

    // ====================
    // Get && display results
    // ====================
    private void getNewResult(final boolean isWidget) {


        SharedPreferences preferences = getSharedPreferences("share", getApplicationContext().MODE_PRIVATE);
        String market1 = preferences.getString("market", "Kucoin");
        String goc1 = preferences.getString("goc", "ACT");
        String moi1 = preferences.getString("moi", "BCH");

        final Market market = getSelectedMarket(market1);

        final String currencyBase = goc1;
        final String currencyCounter = moi1;
        Log.d("yyyy", "goc: " + goc1);
        Log.d("yyyy", "moi: " + moi1);
        if (currencyBase.equals(null) || currencyCounter.equals(null)) {
            Toast.makeText(MainActivity.this, "please select value before", Toast.LENGTH_SHORT).show();
            return;
        }
//        final String pairId = currencyPairsMapHelper != null ? currencyPairsMapHelper.getCurrencyPairId(currencyBase, currencyCounter) : null;

        String pairId = goc1 + "-" + moi1;
        final int contractType = getSelectedContractType(market);
        final CheckerInfo checkerInfo = new CheckerInfo(currencyBase, currencyCounter, pairId, contractType);

        Log.d("yyyy", "pair id : " + pairId);
        Log.d("yyyy", "contractType : " + contractType);

        Request<?> request = new CheckerVolleyMainRequest(market, checkerInfo, new ResponseListener<TickerWrapper>() {
            @Override
            public void onResponse(String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String responseString, TickerWrapper tickerWrapper) {
                handleNewResult(isWidget, checkerInfo, tickerWrapper.ticker, url, requestHeaders, networkResponse, responseString, null, null);
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

                handleNewResult(isWidget, checkerInfo, null, url, requestHeaders, networkResponse, responseString, errorMsg, error);
            }
        });
        requestQueue.add(request);
//        showResultView(false);
    }

    private void handleNewResult(boolean isWidget, CheckerInfo checkerInfo, Ticker ticker, String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String rawResponse, String errorMsg, VolleyError error) {
//        showResultView(true);
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


        String x = (ssb + "").trim();
//        Log.d("yyyy", "data: " + ssb);
        int vt = 0;
        for (int i = 0; ; i++) {
            if (x.charAt(i) == ' ') {
                vt = i;
                break;
            }
        }

        x = x.substring(6, x.length()).trim();


        String price1 = x.substring(0, x.indexOf(" ")).trim();
        price1 = price1.replace(",", ".");

        Log.d("yyyy", "price : " + price1);


        try {
            String tmp = edtCount.getText().toString().trim().replaceAll(",", ".");
            double count = Double.parseDouble(tmp);
            double z = Double.parseDouble(price1.trim()) * count;
            Log.d("yyyy", "z : " + z);
            if (!isWidget) {
                txtResult.setText(String.format("%.8f", z));
            } else {
//                setDataWidget(String.format("%.10f", z));
            }

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "please check count again", Toast.LENGTH_LONG).show();
            return;
        }


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
                txtResult.setText("");
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                return true;
            case R.id.fav:
                setFavoClick();
                return true;

        }
        return false;
    }


    private void setFavoClick() {
        String market = txtMarket.getText().toString();
        String xgoc = goc;
        String xmoi = moi;


        realm.beginTransaction();
        DataInfo dataInfo = new DataInfo();
        dataInfo.setMarket(market);
        dataInfo.setMoi(xmoi);
        dataInfo.setGoc(xgoc);
        DataInfo df = realm.copyToRealm(dataInfo);

        realm.commitTransaction();

        listFavo.add(dataInfo);
        favoAdapter.setLisData(listFavo);
        favoAdapter.notifyDataSetChanged();

        Toast.makeText(MainActivity.this, "favorire done", Toast.LENGTH_SHORT).show();


    }


    public void setDataWidget(final Context context) {
        Log.d("yyyy", "setdat widget");

        // Getting an instance of WidgetManager

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final ComponentName thisWidget = new ComponentName(context, AppWidget.class);
        // Instantiating the class RemoteViews with widget_layout
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);


        SharedPreferences preferences = context.getSharedPreferences("share", MODE_PRIVATE);
        String market = preferences.getString("market", "Kucoin");
        String goc1 = preferences.getString("goc", "ACT");
        String moi1 = preferences.getString("moi", "BCH");


        final String url = "https://www.cryptocompare.com/api/data/coinlist/";


        views.setTextViewText(R.id.txt_goc_moi, market + " : " + goc1 + " / " + moi1);

//        views.setTextViewText(R.id.txt_market, market);

        new GetPrice(context) {
            @Override
            public void result(Double result) {
                Log.d("vvvv", "result : " + result);

                views.setTextViewText(R.id.txt_price, String.format("%.8f", result));
                appWidgetManager.updateAppWidget(thisWidget, views);
                Log.d("vvvv", "after");
            }
        }.getNewResult();


        Intent intent = new Intent(context, MyReceiver.class);
        intent.setAction("refret");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.imgbtn_refret, pendingIntent);

        Intent i = new Intent(context, MyReceiver.class);
        i.setAction("setting");

        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.layout_widget, pendingIntent1);


        appWidgetManager.updateAppWidget(thisWidget, views);

        new ParseJson(this, url, goc1, new ParseJson.MyCallBack() {
            @Override
            public void success(Coin coin) {

                final String coinUrl = "https://www.cryptocompare.com" + coin.getUrl();
                Log.d("aaaa", "icon url : " + coinUrl);

                new Thread() {
                    public void run() {
                        Bitmap img = getBitmapFromUrl(context, coinUrl);

                        views.setImageViewBitmap(R.id.img_icon, img);
                        appWidgetManager.updateAppWidget(thisWidget, views);
                    }
                }.start();


            }
        }).getCoin();


    }

    public static class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("zzzz", "receiver");

            if (intent.getAction().equals("setting")) {
                Log.d("zzzz", "setting");
                Intent i = new Intent(context, SettingActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } else {
                Log.d("zzzz", "refret");
                new MainActivity().setDataWidget(context);
            }

        }
    }


    private static Bitmap getBitmapFromUrl(Context context, final String url) {
        try {
            Log.d("aaaa", "Downloading image from  url: " + url);
            return BitmapFactory.decodeStream((InputStream) new java.net.URL(url).getContent());
        } catch (Exception e) {
            Log.d("aaaa", "Image cannot be loaded, using default image: " + e);
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        }
    }


}
