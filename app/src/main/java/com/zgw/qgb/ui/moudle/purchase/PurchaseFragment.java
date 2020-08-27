package com.zgw.qgb.ui.moudle.purchase;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.helper.ConfigContextWrapper;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.purchase.contract.PurchaseContract;
import com.zgw.qgb.ui.moudle.purchase.presenter.PurchasePresenter;

import java.util.List;

import butterknife.BindView;
import io.reactivex.rxjava3.internal.schedulers.NewThreadWorker;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


public class PurchaseFragment extends BaseMainFragment<PurchasePresenter> implements PurchaseContract.IPurchaseView, LoaderManager.LoaderCallbacks<List<ApplicationInfo>> {

    @BindView(R.id.lv)
    ListView lv;
    @BindView(R.id.debug_network_endpoint)
    Spinner debugNetworkEndpoint;

    private String title;
    private AppsListAdapter adapter;
    private static final int LOADER_ID = 0;

    public PurchaseFragment() {

    }



    public static PurchaseFragment newInstance(String title) {
        PurchaseFragment fragment = new PurchaseFragment();
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

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);
        setBadgeCount(2, 2);
        initAdapter();

    }

    private void initAdapter() {
        adapter = new AppsListAdapter(getContext());
        lv.setAdapter(adapter);
        if (getLoaderManager().getLoader(LOADER_ID) == null) {
            Log.i("TAG", "Initializing the new Loader...");
        } else {
            Log.i("TAG", "Reconnecting with existing Loader (id '1')...");
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_purchase;
    }

    @Override
    protected PurchasePresenter getPresenter() {
        return new PurchasePresenter(this);
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    protected void initData() {
        super.initData();

    }

    public static class AppsAsyncTaskLoader extends AsyncTaskLoader<List<ApplicationInfo>> {
        public AppsAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        public List<ApplicationInfo> loadInBackground() {
            return getContext().getPackageManager().getInstalledApplications(0);
        }

        @Override
        protected void onStartLoading() {

            forceLoad();//强制加载数据
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AppsAsyncTaskLoader(getContext());
    }


    @Override
    public void onLoadFinished(Loader<List<ApplicationInfo>> loader, List<ApplicationInfo> data) {
        Log.i("TAG", "onLoadFinished()");
        adapter.setItems(data);
    }


    @Override
    public void onLoaderReset(Loader loader) {
        Log.i("TAG", "onLoaderReset()");
        adapter.setItems(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter = null;
    }
}
