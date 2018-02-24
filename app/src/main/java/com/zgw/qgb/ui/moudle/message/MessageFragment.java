package com.zgw.qgb.ui.moudle.message;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.base.adapter.AdapterOperator;
import com.zgw.qgb.base.adapter.RecyAdapter;
import com.zgw.qgb.base.adapter.RecyHolder;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.message.contract.MessageContract;
import com.zgw.qgb.ui.moudle.message.presenter.MessagePresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;

public class MessageFragment extends BaseMainFragment<MessagePresenter> implements MessageContract.IMessageView {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
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
    public void initData() {
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add(i+"");
        }
        RecyAdapter adapter = new RecyAdapter<String>(list) {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
                View view = inflater.inflate(R.layout.layout_toolbar,null);
                return new RecyHolder<String>(view){
                    @Override
                    public void bindData(AdapterOperator<String> operator, int position, String itemData) {
                        super.bindData(operator, position, itemData);
                        TextView tv_title = (TextView) find(R.id.tv_title);
                        tv_title.setText(itemData);
                    }
                };
            }

        };
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

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
