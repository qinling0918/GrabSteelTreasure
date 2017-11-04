package com.zgw.qgb.ui.moudle.quote.presenter;

import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteContract;

/**
 * Name:QuotePresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class QuotePresenter extends BasePresenter<QuoteContract.IQuoteView> implements QuoteContract.IQuotePresenter{
    public QuotePresenter(QuoteContract.IQuoteView view) {
        super(view);
    }
}
