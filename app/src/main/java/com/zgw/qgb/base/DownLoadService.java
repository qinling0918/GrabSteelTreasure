package com.zgw.qgb.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownLoadService extends Service {
    public DownLoadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
     /*   Rxdownload.download(mDownloadUrl)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getView().bind2Lifecycle())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.d("content",s);
                    }
                })
        ;*/

    }
}
