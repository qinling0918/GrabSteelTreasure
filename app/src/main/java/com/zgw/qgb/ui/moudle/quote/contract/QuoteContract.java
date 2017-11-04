package com.zgw.qgb.ui.moudle.quote.contract;

import com.zgw.qgb.base.mvp.IPresenter;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.ui.widgets.SegmentControl;

/**
 * Created by Tsinling on 2017/9/16 14:14.
 * description:
 */

public interface QuoteContract {
    interface IQuoteView extends IView, SegmentControl.OnSegmentControlClickListener{
    }

    interface IQuotePresenter extends IPresenter<IQuoteView> {

    }
}
