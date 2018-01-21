package com.mobnetic.coinguardiandatamodule.hieu.volley;

import java.util.Map;

import com.android.volley.toolbox.RequestFuture;
import com.mobnetic.coinguardiandatamodule.hieu.volley.generic.GzipVolleyRequest;

public class DynamicCurrencyPairsVolleyNextRequest extends GzipVolleyRequest<String> {

	public DynamicCurrencyPairsVolleyNextRequest(String url, RequestFuture<String> future) {
		super(url, future, future);
	}

	@Override
	protected String parseNetworkResponse(Map<String, String> headers, String responseString) throws Exception {
		return responseString;
	}

}
