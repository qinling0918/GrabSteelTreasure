package com.zgw.qgb.ui.moudle.message;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.RecyAdapter;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.message.contract.MessageContract;
import com.zgw.qgb.ui.moudle.message.presenter.MessagePresenter;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;

public class MessageFragment extends BaseMainFragment<MessagePresenter> implements MessageContract.IMessageView{

    private String title;

    public MessageFragment() {
    }

    public static MessageFragment newInstance(String title) {
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(Bundler.start()
                .put(EXTRA, title).end());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA);
        }

        RecyAdapter<String> adapter = new RecyAdapter<String>(getContext()) {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
            }
        };

        //BaseRecyclerAdapter<String,BaseViewHolder,>
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_message;
    }

    @Override
    protected MessagePresenter getPresenter() {
        return new MessagePresenter(this);
    }

    @Override
    public void onLazyLoad() {

    }
}
