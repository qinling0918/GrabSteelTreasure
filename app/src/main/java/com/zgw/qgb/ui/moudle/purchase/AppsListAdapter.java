package com.zgw.qgb.ui.moudle.purchase;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.AdapterOperator;
import com.zgw.qgb.base.adapter.ListAdapter;
import com.zgw.qgb.base.adapter.ListHolder;

/**
 * Created by qinling on 2019/3/29 14:56
 * Description:
 */
public class AppsListAdapter extends ListAdapter<ApplicationInfo> {
    private static final int VIEW_TYPE_ONE = 0;
    private static final int VIEW_TYPE_TWO = 1;
    private static final int VIEW_TYPE_THREE = 2;

    public AppsListAdapter(Context context) {
        super(context);
    }

    @Override
    public ListHolder<ApplicationInfo> onCreateViewHolder(ViewGroup parent, int viewType, LayoutInflater inflater) {
        ListHolder listHolder;
        switch (viewType) {
            case VIEW_TYPE_ONE:
                listHolder = new OneHolder(parent);
                // inflater.inflate(android.R.layout.simple_list_item_1,parent,false));
                break;
            case VIEW_TYPE_TWO:
                listHolder = new TwoHolder(
                        inflater.inflate(android.R.layout.simple_list_item_2, parent, false));
                break;
            case VIEW_TYPE_THREE:
            default:
                listHolder = new ThreeHolder(
                        inflater.inflate(android.R.layout.activity_list_item, parent, false));
                break;
        }
        return listHolder;

    }


    @Override
    public int getItemViewType(int position) {
        // 取余  0，1，2  不建议这么写， 只是因为取余结果正好使  三个类型
        return position % 3;
    }

    private static class OneHolder extends ListHolder<ApplicationInfo> {
        TextView textView;

        public OneHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext())
                     .inflate(android.R.layout.simple_list_item_1,parent,false));
                    //.inflate(R.layout.layout_toolbar, parent, false));

            textView = (TextView) find(android.R.id.text1);
            //.inflate(R.layout.fixed_bottom_navigation_item,parent,false));
        }


        @Override
        public void bindData(AdapterOperator<ApplicationInfo> operator, int position, ApplicationInfo itemData) {
            super.bindData(operator, position, itemData);
            textView.setText(itemData.packageName);
            // textView.setBackground(context.getResources().getDrawable(itemData.icon));

        }
    }

    private static class TwoHolder extends ListHolder<ApplicationInfo> {
        TextView textView;

        public TwoHolder(View convertView) {
            super(convertView);
            textView = (TextView) find(android.R.id.text1);
        }

        @Override
        public void bindData(AdapterOperator<ApplicationInfo> operator, int position, ApplicationInfo itemData) {
            super.bindData(operator, position, itemData);
            textView.setText(itemData.packageName);
            //  textView.setBackground(context.getResources().getDrawable(itemData.icon));
        }
    }

    private static class ThreeHolder extends ListHolder<ApplicationInfo> {

        TextView textView;

        public ThreeHolder(View convertView) {
            super(convertView);
            textView = (TextView) find(android.R.id.text1);
        }

        @Override
        public void bindData(AdapterOperator<ApplicationInfo> operator, int position, ApplicationInfo itemData) {
            super.bindData(operator, position, itemData);
            textView.setText(itemData.packageName);
            //  textView.setBackground(context.getResources().getDrawable(itemData.icon));
        }
    }
}
