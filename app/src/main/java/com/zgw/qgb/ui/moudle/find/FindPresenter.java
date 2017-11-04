package com.zgw.qgb.ui.moudle.find;

import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.find.FindContract.*;
/**
 * Created by Tsinling on 2017/9/8 17:11.
 * description:
 */
public class FindPresenter extends BasePresenter<IFindView> implements IFindPresenter {
    FindPresenter(IFindView view) {
        super(view);
    }
}
