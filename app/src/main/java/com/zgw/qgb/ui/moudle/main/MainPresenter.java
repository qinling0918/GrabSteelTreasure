package com.zgw.qgb.ui.moudle.main;

import android.os.Bundle;

import com.zgw.qgb.base.mvp.BasePresenter;
import com.zgw.qgb.ui.moudle.main.MainContract.IMainPresenter;
import com.zgw.qgb.ui.moudle.main.MainContract.IMainView;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


/**
 * Created by Tsinling on 2017/9/8 17:11.
 * description:
 */

public class MainPresenter extends BasePresenter<IMainView> implements IMainPresenter {
    private boolean downloading;

    MainPresenter(IMainView view) {
        super(view);
    }
    String url = new String("http://acj2.pc6.com/pc6_soure/2017-6/com.zgw.qgb_29.apk");
    @Override
    public void login() {
        /*http://192.168.1.18:8026/Notice/GetPushMessageList?page=1&MsgTypeId=22&pageSize=10&memberId=73740*/
      /*  RetrofitProvider.getService(MainService.class)
                .getNotification(1,22,10,73740)
                .compose(RxProgress.bindToLifecycle(getView(), R.string.message))
                .compose(getView().bind2Lifecycle())
                .toObservable()
                .subscribe(new BaseObserver<MainBean>() {
                    @Override
                    public void onSuccess(MainBean mainBean) {
                        getView().setText(mainBean.toString());
                    }
                });*/
     //if (! downloading){
         downloading = true ;
         //Rxdownload.downloadBigFile(url,3);
                 //.compose(RxProgress.bindToLifecycle(getView(), R.string.message))
                 //.compose(getView().bind2Lifecycle())
                 /*.subscribe(new BaseObserver<File>() {
                     @Override
                     public void onSuccess(File file) {
                         Log.d("Rxdownload", file.getAbsolutePath()+"   "+file.length());
                     }
                 });*/
     //}

       /* ProgressManager.getInstance().addResponseListener(url, new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                Log.d("Rxdownload", progressInfo.getPercent()+"'"+progressInfo.getId());
            }

            @Override
            public void onError(long id, Exception e) {

            }
        });*/
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        downloading =savedInstanceState.getBoolean(EXTRA, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA,downloading);
    }

    @Override
    public void detachView() {
        url = null;
        super.detachView();
    }
}
