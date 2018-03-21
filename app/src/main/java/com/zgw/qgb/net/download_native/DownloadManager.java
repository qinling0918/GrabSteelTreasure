package com.zgw.qgb.net.download_native;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.zgw.qgb.R;
import com.zgw.qgb.base.mvp.IView;
import com.zgw.qgb.helper.InputHelper;

import java.io.File;

/**
 * Name:DownloadManager
 * Created by Tsinling on 2018/3/5 15:32.
 * description:
 */

public class DownloadManager {

    /**
     *
     * @param context
     * @param url
     * @return downloadId
     */
    public static long downloadFile(@NonNull Context context, @NonNull String url) {
        if (InputHelper.isEmpty(url)) return -1;
        Uri uri = Uri.parse(url);
        android.app.DownloadManager downloadManager = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(uri);

        File direct = new File(Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name));
        if (!direct.isDirectory() || !direct.exists()) {
            boolean isCreated = direct.mkdirs();
            if (!isCreated) {
                ((IView)context).showMessage(R.string.error,R.string.create_directory_error);
                //Toast.makeText(App.getInstance(), "Unable to create directory to download file", Toast.LENGTH_SHORT).show();
                return -1;
            }
        }
        String fileName = new File(url).getName();

        request.setDestinationInExternalPublicDir(context.getString(R.string.app_name), fileName);
        request.setTitle(fileName);
        request.setDescription(context.getString(R.string.downloading_file));
        request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_MOBILE | android.app.DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        return downloadManager.enqueue(request);




    }

    /**
     *
     * @param context
     * @param downloadId
     * @return 根据downloadId  获取文件路径
     */
    private static String findFileByDownloadId(@NonNull Context context, long downloadId) {
        android.app.DownloadManager dm = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        android.app.DownloadManager.Query query = new android.app.DownloadManager.Query().setFilterById(downloadId);
        Cursor c = dm.query(query);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndexOrThrow(android.app.DownloadManager.COLUMN_LOCAL_URI));
            }
            c.close();
        }
        return "";
    }
    /**
     *
     * @param context
     * @param downloadId
     * @return
     *      STATUS_PENDING
     *      STATUS_PAUSED
     *      STATUS_RUNNING
     *      STATUS_SUCCESSFUL
     *      STATUS_FAILED
     */
    private static int getFileDownloadStatusById(@NonNull Context context, long downloadId) {
        android.app.DownloadManager dm = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        android.app.DownloadManager.Query query = new android.app.DownloadManager.Query().setFilterById(downloadId);
        Cursor c = dm.query(query);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndexOrThrow(android.app.DownloadManager.COLUMN_STATUS));
            }
            c.close();
        }
        return android.app.DownloadManager.STATUS_FAILED;

    }

}
