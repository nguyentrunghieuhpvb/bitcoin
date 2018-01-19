package com.mobnetic.coinguardiandatamodule.tester;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by PlayGirl on 1/18/2018.
 */

public class FavoAdapter extends RecyclerView.Adapter<FavoAdapter.ViewHolder> {

    ArrayList<DataInfo> lisData = new ArrayList<>();

    public interface onClick {
        public void onClick(DataInfo dataInfo);
    }

    private onClick myOnClick;

    public FavoAdapter(ArrayList<DataInfo> lisData, onClick myOnClick) {
        this.lisData.addAll(lisData);
        this.myOnClick = myOnClick;
    }


    public void setLisData(ArrayList<DataInfo> lisData1) {
        lisData.clear();
        lisData.addAll(lisData1);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_favo, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DataInfo dataInfo = lisData.get(position);
        holder.txtMarket.setText(dataInfo.getMarket());
        holder.txtGocMoi.setText(dataInfo.getGoc() + "  / " + dataInfo.getMoi());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myOnClick.onClick(dataInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lisData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMarket, txtGocMoi;
        RelativeLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);

            txtMarket = (TextView) itemView.findViewById(R.id.txt_market);
            txtGocMoi = (TextView) itemView.findViewById(R.id.txt_goc_moi);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout_favo);
        }
    }
}
