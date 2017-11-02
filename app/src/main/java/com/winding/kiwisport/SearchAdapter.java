package com.winding.kiwisport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.help.Tip;

import java.util.List;

/**
 * Created by 刘少帅 on 2017/11/2
 */

public class SearchAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private Context context;
    private  List<Tip> mData;

    public SearchAdapter(Context context, List<Tip> mData) {
        this.context = context;
        this.mData = mData;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view=mLayoutInflater.inflate(R.layout.item_search,viewGroup,false);

        TextView tvCity = view.findViewById(R.id.tv_item_city);
        tvCity.setText(mData.get(i).getName());

        return view;
    }
}
