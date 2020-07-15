package com.zgw.qgb.delete.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.zgw.qgb.base.adapter.AdapterOperator;
import com.zgw.qgb.base.adapter.HolderOperator;
import com.zgw.qgb.base.adapter.InnerClickListener;
import com.zgw.qgb.base.adapter.RecyAdapter;
import com.zgw.qgb.databinding.ActivityMain1Binding;

/**
 * recyclerView的viewholder基类
 * <p/>
 * Created by tianlai on 16-7-16.
 */
public  class ViewBindingRecyHolder<T,VB extends ViewBinding> extends RecyclerView.ViewHolder{
    protected  final String TAG = this.getClass().getSimpleName();

    protected T itemData;
    public  VB mBinding;
    protected int position;

    protected Context context;

    protected RecyAdapter adapter;

    protected InnerClickListener clickListener;

    public ViewBindingRecyHolder(VB mBinding) {
        this(mBinding.getRoot());
        this.mBinding = mBinding;
    }

    private ViewBindingRecyHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
    }


    /**
     * 绑定数据
     *
     * @param itemData
     * @param position
     * @param operator
     */
    public void bindData(AdapterOperator<T> operator,int position, T itemData ){
        this.itemData = itemData;
        this.position = position;

        if (operator instanceof RecyAdapter) {
            adapter = (RecyAdapter) operator;
        }

        if (adapter != null) {
            clickListener = adapter.getInnerClickListener();
        }
    }

}
