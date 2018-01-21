package com.mobnetic.coinguardiandatamodule.hieu;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

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
import com.mobnetic.coinguardian.util.FormatUtilsBase;
import com.mobnetic.coinguardian.util.MarketsConfigUtils;
import com.mobnetic.coinguardiandatamodule.hieu.util.CheckErrorsUtils;
import com.mobnetic.coinguardiandatamodule.hieu.util.HttpsHelper;
import com.mobnetic.coinguardiandatamodule.hieu.volley.CheckerErrorParsedError;
import com.mobnetic.coinguardiandatamodule.hieu.volley.CheckerVolleyMainRequest;
import com.mobnetic.coinguardiandatamodule.hieu.volley.generic.ResponseErrorListener;
import com.mobnetic.coinguardiandatamodule.hieu.volley.generic.ResponseListener;

import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by PlayGirl on 1/19/2018.
 */

public abstract class GetPrice {

    private Context context;
    private RequestQueue requestQueue;
    ArrayList<String> listMarket = new ArrayList<String>();

    public GetPrice(Context context) {
        this.context = context;
        requestQueue = HttpsHelper.newRequestQueue(context);
        getListMarket();
    }

    public void getNewResult() {


        SharedPreferences preferences = context.getSharedPreferences("share", MODE_PRIVATE);
        String market1 = preferences.getString("market", "Kucoin");
        String goc = preferences.getString("goc", "ACT");
        String moi = preferences.getString("moi", "BCH");

        final Market market = getSelectedMarket(market1);

        final String currencyBase = goc;
        final String currencyCounter = moi;

//        final String pairId = currencyPairsMapHelper != null ? currencyPairsMapHelper.getCurrencyPairId(currencyBase, currencyCounter) : null;

        String pairId = goc + "-" + moi;
        final int contractType = getSelectedContractType(market);
        final CheckerInfo checkerInfo = new CheckerInfo(currencyBase, currencyCounter, pairId, contractType);

        Log.d("yyyy", "pair id : " + pairId);
        Log.d("yyyy", "contractType : " + contractType);

        final Request<?> request = new CheckerVolleyMainRequest(market, checkerInfo, new ResponseListener<CheckerVolleyMainRequest.TickerWrapper>() {
            @Override
            public void onResponse(String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String responseString, CheckerVolleyMainRequest.TickerWrapper tickerWrapper) {
                double result = handleNewResult(checkerInfo, tickerWrapper.ticker, url, requestHeaders, networkResponse, responseString, null, null);

                result(result);

            }
        }, new ResponseErrorListener() {
            @Override
            public void onErrorResponse(String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String responseString, VolleyError error) {

                Log.d("yyyy", "ResponseErrorListener");

                String errorMsg = null;
                if (error instanceof CheckerErrorParsedError) {
                    errorMsg = ((CheckerErrorParsedError) error).getErrorMsg();
                }

                if (TextUtils.isEmpty(errorMsg))
                    errorMsg = CheckErrorsUtils.parseVolleyErrorMsg(context, error);

                handleNewResult(checkerInfo, null, url, requestHeaders, networkResponse, responseString, errorMsg, error);
            }
        });
        requestQueue.add(request);



    }

    private double handleNewResult(CheckerInfo checkerInfo, Ticker ticker, String url, Map<String, String> requestHeaders, NetworkResponse networkResponse, String rawResponse, String errorMsg, VolleyError error) {
//        showResultView(true);
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if (ticker != null) {
            ssb.append(context.getString(R.string.ticker_last, FormatUtilsBase.formatPriceWithCurrency(ticker.last, checkerInfo.getCurrencyCounter())));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_high, ticker.high, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_low, ticker.low, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_bid, ticker.bid, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_ask, ticker.ask, checkerInfo.getCurrencyCounter()));
            ssb.append(createNewPriceLineIfNeeded(R.string.ticker_vol, ticker.vol, checkerInfo.getCurrencyBase()));
            ssb.append("\n" + context.getString(R.string.ticker_timestamp, FormatUtilsBase.formatSameDayTimeOrDate(context, ticker.timestamp)));
        } else {
            ssb.append(context.getString(R.string.check_error_generic_prefix, errorMsg != null ? errorMsg : "UNKNOWN"));
        }

        CheckErrorsUtils.formatResponseDebug(context, ssb, url, requestHeaders, networkResponse, rawResponse, error);


        String x = (ssb + "").trim();
        Log.d("yyyy", "data: " + ssb);


        x = x.substring(6, x.length()).trim();


        String price1 = x.substring(0, x.indexOf(" ")).trim();
        price1 = price1.replace(",", ".");

        Log.d("yyyy", "price : " + price1);


        double z = Double.parseDouble(price1.trim());
        Log.d("yyyy", "z : " + z);

        return z;
    }

    private String createNewPriceLineIfNeeded(int textResId, double price, String currency) {
        if (price <= Ticker.NO_DATA)
            return "";

        return "\n" + context.getString(textResId, FormatUtilsBase.formatPriceWithCurrency(price, currency));
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

    public abstract void result(Double result);
}
