package com.zgw.qgb.ui.moudle.message.contract;

import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.ui.widgets.SegmentControl;

/**
 * Created by Tsinling on 2017/9/16 14:14.
 * description:
 */

public interface MessageContract {
    interface IMessageView extends IView{
    }

    interface IMessagePresenter extends IPresenter<IMessageView> {

    }
}
