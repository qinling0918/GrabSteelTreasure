package com.zgw.qgb.ui.moudle.quote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zgw.qgb.R;
import com.zgw.qgb.base.BaseFragment;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.helper.ToastUtils;
import com.zgw.qgb.helper.utils.FileUtils;
import com.zgw.qgb.net.download.DownloadService;
import com.zgw.qgb.net.extension.BaseObserver;
import com.zgw.qgb.ui.moudle.quote.contract.QuoteListContract;
import com.zgw.qgb.ui.moudle.quote.presenter.QuoteListPresenter;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.zgw.qgb.helper.BundleConstant.EXTRA;


/**
 * Comment://报价列表
 * Created by Tsinling on 2017/5/24 17:34.
 */

public class QuoteListFragment extends BaseFragment<QuoteListPresenter> implements QuoteListContract.IQuoteListView {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_wide_nation)
    TextView tvWideNation;
    String url1 = "http://acj2.pc6.com/pc6_soure/2017-6/com.zgw.qgb_29.apk";

    private DownloadService downloadService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.DownloadBinder downloadBinder = (DownloadService.DownloadBinder) service;
            downloadService = downloadBinder.getService();
            checkPermissionAndStartDownload();
            setListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
        }
    };

    private void setListener() {
        downloadService.setOnDownloadListener(new DownloadService.DefaultDownloadListener() {
            @Override
            public void onSuccess(File file) {
                FileUtils.installAPk(getContext(),file);
            }
        });
    }

    private String title;

    public static QuoteListFragment newInstance(String title) {
        QuoteListFragment fragment = new QuoteListFragment();
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

        Intent intent = new Intent(getContext(), DownloadService.class);
        //getContext().startService(intent);//启动服务
        getContext().bindService(intent, connection, BIND_AUTO_CREATE);//绑定服务

    }

    private void checkPermissionAndStartDownload() {
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        rxPermissions.request( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new BaseObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean){
                            downloadService.startDownload(url1);
                        }else{
                            ToastUtils.showNormal("文件写入的权限申请被拒绝");
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);
    }

    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_quote_list;
    }


    @Override
    protected QuoteListPresenter getPresenter() {
        return new QuoteListPresenter(this);
    }



    @SuppressLint("ResourceType")
    @OnClick({R.id.tv_title, R.id.tv_wide_nation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_title:
                downloadService.cancelDownload();
                //https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1517645120430&di=c4851c8646d6f6d086ea6ec6f3edf71c&imgtype=0&src=http%3A%2F%2Fwww.jituwang.com%2Fuploads%2Fallimg%2F160203%2F257953-160203193R164.jpg
                //String url1 = "https://www.baidu.com/link?url=soOQSZR7o_Jy2Tzxj6LIpD6xF0NEvw7tjMx_yi6gS-3az9wGOVqzXQ6hijP18_NR2neyWBMJtn18cMfqD3_LW3hIm6xDLf1wjGXZQMvaQRm&wd=&eqid=846b5b7e00040043000000035a754498";
                //String url1 =url0;
                //String url2 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1517645120430&di=c4851c8646d6f6d086ea6ec6f3edf71c&imgtype=0&src=http%3A%2F%2Fwww.jituwang.com%2Fuploads%2Fallimg%2F160203%2F257953-160203193R164.jpg";

               /* ToastUtils.setBgColor(getResources().getColor(R.color.colorPrimary));
                ToastUtils.setMsgColor(getResources().getColor(android.R.color.white));
                ToastUtils.showLong("Toast" + num++);*/
             /*   LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View toastView = inflate != null ? inflate.inflate(R.layout.fragment_message, null) : null;
                toastView.setBackgroundColor(R.color.colorPrimary);*/
                //((TextView)toastView.findViewById(R.id.toast_text)).setText(R.string.message);
           /*     TextView tv = new TextView(getContext());
                tv.setText("123123");
                ToastUtils.setGravity(Gravity.CENTER,0,0);
                ToastUtils.showCustomShort(tv);*/
                //showMessage(R.string.error,R.string.error);
                //ToastUtils.showLong(getString(R.string.message));
                break;
            case R.id.tv_wide_nation:
                downloadService.pauseDownload();
               /* UserInfo userInfo = new UserInfo();
                List<UserInfo.User> list = new ArrayList<UserInfo.User>();
                for (int i = 0; i < 5; i++) {
                    UserInfo.User user = new UserInfo.User();
                    user.setAge(20+ i);
                    user.setName("tsinling"+i);

                    list.add(user);
                }

                userInfo.setUserList(list);

                PrefGetter.setUserInfo(userInfo);


                List<UserInfo.User> userList =  PrefGetter.getUserInfo().getUserList();
                tvWideNation.setText(PrefGetter.getUserInfo().getUserList().size() +" / "+ userList.get(0).getAge());*/


                break;
        }
    }
    public void getSum(Point... pa) {
        //二维数组中的行数
        int rows = 4;
        //二维数组的列数
        int cols = 5;
        //二维数组中元素的个数
        int len = rows * cols;
        //这个设置要计算的二维数组的内容
        int[][] nums = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                nums[i][j] = i * cols + j;
            }
        }

        float sum = 0;
        StringBuilder build = new StringBuilder();
        for (Point point : pa) {
            if (null != point) {
                sum += nums[point.x][point.y];
                build.append("(" + point.x + "," + point.y + ")+ ");

            }
        }

        Log.d(TAG, "getSum: "+build + sum);
       /* Point pa, pb, pc, pd;
        //任意4个数的和
        float sum = 0;
        //以下四重循环是主体算法，其核心思路是完成20个元素取4个的组合
        for (int a = 0; a < len - 3; a++) {
            pa = getPoint(a);
            sum = nums[pa.x][pa.y];
            for (int b = a + 1; b < len - 2; b++) {
                pb = getPoint(b);
                sum += nums[pb.x][pb.y];
                for (int c = b + 1; c < len - 1; c++) {
                    pc = getPoint(c);
                    sum += nums[pc.x][pc.y];
                    for (int d = c + 1; d < len; d++) {
                        pd = getPoint(d);
                        sum += nums[pd.x][pd.y];
                        //这里根据项目要求处理结果
                        System.out.println("(" + pa.x + "," + pa.y + ")+("
                                + pb.x + "," + pb.y + ")+(" + pc.x + "," + pc.y
                                + ")+(" + pd.x + "," + pd.y + ")=" + sum);

                        Log.d(TAG, "getSum: "+"(" + pa.x + "," + pa.y + ")+("
                                + pb.x + "," + pb.y + ")+(" + pc.x + "," + pc.y
                                + ")+(" + pd.x + "," + pd.y + ")=" + sum);
                    }
                }
            }
        }*/
    }

    //根据二维数组的元素的行优先序号，计算其行号和列号
    public Point getPoint(int v) {
        Point p = new Point();
        p.x = v / 5;
        p.y = v % 5;
        return p;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unbindService(connection);//解绑服务



    }


}
