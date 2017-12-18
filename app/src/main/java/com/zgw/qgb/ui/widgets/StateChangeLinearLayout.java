package com.zgw.qgb.ui.widgets;

/**
 * Name:StateChangeLinearLayout
 * Created by Tsinling on 2017/12/5 16:21.
 * description: 根据布局尺寸变化,判断键盘显示与隐藏
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class StateChangeLinearLayout extends LinearLayout{

    public StateChangeLinearLayout(Context context) {
        super(context);
    }

    public StateChangeLinearLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
    }

    public StateChangeLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int mChangeSize = 200;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (oldw == 0 || oldh == 0)
            return;

        if (boardListener != null) {
            if ( h - oldh < -mChangeSize) {
                boardListener.keyBoardVisable(Math.abs(h - oldh));
            }

            if (  h - oldh > mChangeSize) {
                boardListener.keyBoardInvisable(Math.abs(h - oldh));
            }
        }
    }

    public interface SoftkeyBoardListener {

         void keyBoardVisable(int move);

         void keyBoardInvisable(int move);
    }

    private SoftkeyBoardListener boardListener;

    public void setSoftKeyBoardListener(SoftkeyBoardListener boardListener) {
        this.boardListener = boardListener;
    }
}

/*
作者：Anderson大码渣
        链接：http://www.jianshu.com/p/ff1d8bcc5253
        來源：简书
        著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。*/
