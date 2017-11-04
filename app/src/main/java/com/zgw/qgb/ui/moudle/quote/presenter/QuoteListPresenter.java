package com.zgw.qgb.ui.moudle.quote.presenter;

import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteListContract;

/**
 * Name:QuoteListPresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class QuoteListPresenter extends BasePresenter<QuoteListContract.IQuoteListView> implements QuoteListContract.IQuoteListPresenter{
    public QuoteListPresenter(QuoteListContract.IQuoteListView view) {
        super(view);
    }
}
