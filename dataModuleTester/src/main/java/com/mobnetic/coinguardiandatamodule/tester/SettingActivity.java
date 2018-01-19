package com.mobnetic.coinguardiandatamodule.tester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
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
import com.mobnetic.coinguardiandatamodule.tester.dialog.DynamicCurrencyPairsDialog;
import com.mobnetic.coinguardiandatamodule.tester.util.CheckErrorsUtils;
import com.mobnetic.coinguardiandatamodule.tester.util.HttpsHelper;
import com.mobnetic.coinguardiandatamodule.tester.util.MarketCurrencyPairsStore;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerErrorParsedError;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerVolleyMainRequest;
import com.mobnetic.coinguardiandatamodule.tester.volley.CheckerVolleyMainRequest.TickerWrapper;
import com.mobnetic.coinguardiandatamodule.tester.volley.generic.ResponseErrorListener;
import com.mobnetic.coinguardiandatamodule.tester.volley.generic.ResponseListener;

public class SettingActivity extends Activity {

    private RequestQueue requestQueue;
    private Spinner marketSpinner;
    private View currencySpinnersWrapper;
    private View dynamicCurrencyPairsWarningView;
    private View dynamicCurrencyPairsInfoView;
    private Spinner currencyBaseSpinner;
    private Spinner currencyCounterSpinner;
//    private Spinner futuresContractTypeSpinner;
//    private View getResultButton;
//    private ProgressBar progressBar;
//    private TextView resultView;

    private CurrencyPairsMapHelper currencyPairsMapHelper;

    String TAG = "MainActivity";


    ArrayList<String> listCu = new ArrayList<>();
    ArrayList<String> listMoi = new ArrayList<>();
    ArrayList<String> listCho = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = HttpsHelper.newRequestQueue(this);

        setContentView(R.layout.activity_setting);

        marketSpinner = (Spinner) findViewById(R.id.marketSpinner);
        currencySpinnersWrapper = findViewById(R.id.currencySpinnersWrapper);
        dynamicCurrencyPairsWarningView = findViewById(R.id.dynamicCurrencyPairsWarningView);
        dynamicCurrencyPairsInfoView = findViewById(R.id.dynamicCurrencyPairsInfoView);
        currencyBaseSpinner = (Spinner) findViewById(R.id.currencyBaseSpinner);
        currencyCounterSpinner = (Spinner) findViewById(R.id.currencyCounterSpinner);
//        futuresContractTypeSpinner = (Spinner) findViewById(R.id.futuresContractTypeSpinner);
//        getResultButton = findViewById(R.id.getResultButton);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        resultView = (TextView) findViewById(R.id.resultView);


        refreshMarketSpinner();

        Market market = getSelectedMarket();
        currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(this, getSelectedMarket().key));
        refreshCurrencySpinners(market);
        refreshFuturesContractTypeSpinner(market);


        marketSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final Market selectedMarket = getSelectedMarket();
                currencyPairsMapHelper = new CurrencyPairsMapHelper(MarketCurrencyPairsStore.getPairsForMarket(SettingActivity.this, selectedMarket.key));
                refreshCurrencySpinners(selectedMarket);
                refreshFuturesContractTypeSpinner(selectedMarket);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
        });

        dynamicCurrencyPairsInfoView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DynamicCurrencyPairsDialog(SettingActivity.this, getSelectedMarket(), currencyPairsMapHelper) {
                    public void onPairsUpdated(Market market, CurrencyPairsMapHelper currencyPairsMapHelper) {
                        SettingActivity.this.currencyPairsMapHelper = currencyPairsMapHelper;
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

//        getResultButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                getNewResult();
//            }
//        });

        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("market", marketSpinner.getSelectedItem().toString());
                editor.putString("goc", currencyBaseSpinner.getSelectedItem().toString());
                editor.putString("moi", currencyCounterSpinner.getSelectedItem().toString());
                editor.commit();
                new MainActivity().setDataWidget(SettingActivity.this);
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

    }


    // hieu repair
    private void refreshDynamicCurrencyPairsView(Market market) {
//		dynamicCurrencyPairsInfoView.setVisibility(market.getCurrencyPairsUrl(0)!=null ? View.VISIBLE : View.GONE);
    }

    private void refreshCurrencyBaseSpinner(Market market) {
        final HashMap<String, CharSequence[]> currencyPairs = getProperCurrencyPairs(market);
        if (currencyPairs != null && currencyPairs.size() > 0) {
            final CharSequence[] entriescu = new CharSequence[currencyPairs.size()];
            int i = 0;
            for (String currency : currencyPairs.keySet()) {
                entriescu[i++] = currency;

                listCu.add(currency + "");
            }

            Log.d(TAG, "cu : " + listCu.get(0));
            currencyBaseSpinner.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, entriescu));
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
            Log.d(TAG, "moi : " + listMoi.get(0));

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

    }

//    private void showResultView(boolean showResultView) {
//        getResultButton.setEnabled(showResultView);
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
//


    private String createNewPriceLineIfNeeded(int textResId, double price, String currency) {
        if (price <= Ticker.NO_DATA)
            return "";

        return "\n" + getString(textResId, FormatUtilsBase.formatPriceWithCurrency(price, currency));
    }


    public void oldvalue() {
        SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);

        String cho = sharedPreferences.getString("cho", "");
        String goc = sharedPreferences.getString("goc", "");
        String moi = sharedPreferences.getString("moi", "");

        currencyBaseSpinner.setSelection(listCu.indexOf(goc));
        currencyCounterSpinner.setSelection(listMoi.indexOf(moi));
        marketSpinner.setSelection(listCu.indexOf(cho));

    }
}
