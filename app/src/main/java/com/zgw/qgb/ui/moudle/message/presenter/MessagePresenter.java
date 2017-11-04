package com.zgw.qgb.ui.moudle.message.presenter;


import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.message.contract.MessageContract;

/**
 * Name:MessagePresenter
 * Created by Tsinling on 2017/11/1 15:33.
 * description:
 */

public class MessagePresenter extends BasePresenter<MessageContract.IMessageView> implements MessageContract.IMessagePresenter{
    public MessagePresenter(MessageContract.IMessageView view) {
        super(view);
    }
}
