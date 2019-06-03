package com.zgw.qgb.base.adapter;

import android.view.View;

/**
 * 列表类的内部控件点击监听
 *
 * Created by tianlai on 16-7-16.
 */
public interface InnerClickListener<T> {
     void onClick(View view, T itemData, int postition);
}
