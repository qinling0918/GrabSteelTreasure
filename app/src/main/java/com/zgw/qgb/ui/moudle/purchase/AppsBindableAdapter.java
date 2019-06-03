package com.zgw.qgb.ui.moudle.purchase;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zgw.qgb.base.adapter.BindableAdapter;

import java.util.List;

/**
 * Created by qinling on 2019/3/29 14:56
 * Description:
 */
public class AppsBindableAdapter extends BindableAdapter<ApplicationInfo> {

    public void setAppInfos(List<ApplicationInfo> appInfos) {
        this.appInfos = appInfos;
        notifyDataSetChanged();
    }

    private List<ApplicationInfo> appInfos;
    public AppsBindableAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        return null;
    }

    @Override
    public void bindView(ApplicationInfo item, int position, View view) {

    }


}
