package com.zgw.qgb.ui.moudle.quote;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseFragment;
import com.zgw.qgb.network.download.DownLoadInfoManager;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteMapContract;
import com.zgw.qgb.ui.moudle.quote.presenter.QuoteMapPresenter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;

import static java.lang.Thread.NORM_PRIORITY;
import static java.lang.Thread.sleep;


/**
 * Name:MapFragment
 * Comment://todo
 * Created by Tsinling on 2017/5/24 17:35.
 */

public class QuoteMapFragment extends BaseFragment<QuoteMapPresenter> implements QuoteMapContract.IQuoteMapView {
    private static final String ARG_PARAM1 = "param1";
    @BindView(R.id.recycleview)
    RecyclerView recycleview;

    private String mParam1;


    public QuoteMapFragment() {
    }

    public static QuoteMapFragment newInstance(String param1) {
        QuoteMapFragment fragment = new QuoteMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "DownloadThread #" + mCount.getAndIncrement());
            thread.setPriority(NORM_PRIORITY);
            return thread;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycleview.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d("Thread: ", "onViewCreated");
     /*   long contentLength = 25*1024;
        long blockLength = 512;
        // 是否有余数
        boolean hasRemainder = (contentLength % blockLength) != 0;
        int blockSize = (int) (contentLength / blockLength);
        blockSize = hasRemainder ? blockSize + 1 : blockSize;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                sThreadFactory);
      //  DownLoadInfoManager manager = null;
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
      //  manager = DownLoadInfoManager.getInstance(path, "a.apk");
       // manager.initWithBlockLength(contentLength, blockLength);
        for (int i = 0; i < blockSize; i++) {
            int startIndex = (int) (i * blockLength);
            int endIndex;
            if (i == blockLength - 1) {
                endIndex = (int) ((i + 1) * blockLength - 1);
            } else {
                endIndex = (int) (contentLength - i * blockLength);
            }

            // DownloadThread thread = new DownloadThread(i, manager, startIndex, endIndex);
           // executor.execute(thread);
            //thread.start();
        }
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("Thread: ", "getCompletedTaskCount" + executor.getCompletedTaskCount());

*/
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_quote_map;
    }

    @Override
    protected QuoteMapPresenter getPresenter() {
        return new QuoteMapPresenter(this);
    }

    class DownloadThread extends Thread {
        int startIndex;
        int endIndex;
        int pos;
        DownLoadInfoManager manager;

        public DownloadThread(int pos, DownLoadInfoManager manager, int startIndex, int endIndex) {
            this.pos = pos;
            this.manager = manager;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }


        public void run() {
            int index = startIndex;
            Log.d("Thread: ", pos + "end");
            if (manager != null) {
             if (pos == 29){
                 try {
                     Thread.sleep(15000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }

                //manager.add();
               // manager.writeBlockCurrentSizeToFile(0, pos);
            }
        }
            /*while ( index < endIndex) {

                try {
                    Thread.sleep(1);
                    index = endIndex;
                    Log.d("Thread: ", pos + "end");
                    // String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) ;
                    if (manager != null) {
                        manager.writeBlockCurrentSizeToFile(pos, index);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }}}*/








        @Override
        public String toString() {
            return "DownloadThread{" +
                    "startIndex=" + startIndex +
                    ", endIndex=" + endIndex +
                    '}';
        }
    }


}
