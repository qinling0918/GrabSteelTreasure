package com.zgw.qgb.ui.moudle.main;

import android.content.Context;

import com.zgw.qgb.base.BaseLazyFragment;
import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.interf.OnBadgeCountChangeListener;

/**
 * description: main界面接收小红点的基类
 *
 * Created by Tsinling on 2017/11/3 11:20.
 */

public abstract class BaseMainFragment<P extends IPresenter> extends BaseLazyFragment<P> {
    private OnBadgeCountChangeListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBadgeCountChangeListener) {
            mListener = (OnBadgeCountChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnToolBarChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * 设置小红点
     * @param fromIndex  需要显示在bottomBar的哪一个位置,即当前页面的position
     * @param count 小红点的数量
     */
    public void setBadgeCount(int fromIndex, int count) {
        if (mListener != null) {
            mListener.onBadgeCountChange(fromIndex,count);
        }
    }


}
