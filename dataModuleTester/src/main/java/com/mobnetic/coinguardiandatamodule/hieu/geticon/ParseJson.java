package com.mobnetic.coinguardiandatamodule.hieu.geticon;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mobnetic.coinguardiandatamodule.hieu.HelperApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nguyen Hung Son on 1/20/2018.
 */

public class ParseJson {
    private Context context;
    private String url;
    private RequestQueue requestQueue;
    private String coin;

    public interface MyCallBack {
        public void success(Coin coin);
    }

    MyCallBack callBack;

    public ParseJson(Context context, String url, String coin, MyCallBack callBack) {
        Log.d("aaaa", "ParseJson");
        this.context = context;
        this.url = url;
        requestQueue = Volley.newRequestQueue(HelperApplication.getAppContext());
        this.coin = coin;
        this.callBack = callBack;

    }

    public void getCoin() {
        Log.d("aaaa", "getcoint");
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Coin mcoin = new Coin();
                try {
                    JSONObject object = response.getJSONObject("Data");
                    JSONObject objCoin = object.getJSONObject(coin);
                    mcoin.setNameCoin(objCoin.getString("Name"));
                    mcoin.setUrl(objCoin.getString("ImageUrl"));
                } catch (JSONException e) {
                    Log.d("aaaa", "JSONException : " + e.toString());
                    e.printStackTrace();
                }
                callBack.success(mcoin);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("aaaa", "onErrorResponse : " + error.toString());
            }
        });
        requestQueue.add(request);
    }


}
