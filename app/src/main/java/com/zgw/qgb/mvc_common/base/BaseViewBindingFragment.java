package com.zgw.qgb.mvc_common.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.zgw.qgb.helper.utils.ViewBindingUtil;

/**
 * Created by qinling on 2020/6/5 16:52
 * Description: ViewBinding 的基类，不再使用butterKnife
 */
public class BaseViewBindingFragment<VB extends ViewBinding> extends IViewFragment {
    protected final String TAG = this.getClass().getCanonicalName();
    private Context mContext;
    private Activity mActivity;
    protected VB mBinding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = ViewBindingUtil.create(getClass(), getLayoutInflater(), container, false);
//        LogPrintUtils.e("mBinding == null "+(mBinding == null));
        View root = mBinding.getRoot();
        onCreateView(root, savedInstanceState);
     //   LogPrintUtils.e(TAG+"onCreateView mBinding == null "+(mBinding == null));
        return root;
    }

    @Override
    protected int fragmentLayout() {
        return 0;
    }


    protected void onCreateView(View view, Bundle bundle) {

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;

    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @NonNull
    @Override
    public Context getContext() {
        // return super.getContext()== null ?mContext :getContext();
        return mContext;
    }

    public FragmentActivity getFragmentActivity() {
        return getActivity() == null ? (FragmentActivity) mActivity : getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("life",this.getClass().getSimpleName()+" onResume ");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("life",this.getClass().getSimpleName()+" onCreate ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("life",this.getClass().getSimpleName()+" onPause ");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("life",this.getClass().getSimpleName()+" onHiddenChanged " + hidden);

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        Log.e("life",this.getClass().getSimpleName()+" menuVisible "+ menuVisible);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.e("life",this.getClass().getSimpleName()+" setUserVisibleHint " +isVisibleToUser);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("life",this.getClass().getSimpleName()+" onStart " );
    }
}
