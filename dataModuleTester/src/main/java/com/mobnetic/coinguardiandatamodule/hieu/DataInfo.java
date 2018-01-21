package com.mobnetic.coinguardiandatamodule.hieu;

import io.realm.RealmObject;

/**
 * Created by PlayGirl on 1/18/2018.
 */

public class DataInfo extends RealmObject{
    private String market;
    private String goc;
    private String moi;

    public DataInfo() {

    }


    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getGoc() {
        return goc;
    }

    public void setGoc(String goc) {
        this.goc = goc;
    }

    public String getMoi() {
        return moi;
    }

    public void setMoi(String moi) {
        this.moi = moi;
    }
}
