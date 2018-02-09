package com.zgw.qgb.helper.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.zgw.qgb.App;
import com.zgw.qgb.R;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Name:FileUtil
 * Created by Tsinling on 2017/10/20 15:05.
 * description:
 */

public final class FileUtils {
    private FileUtils() {
        throw new AssertionError("No instances.");
    }

    private static void saveFile(InputStream is, String destFileDir, String fileName) {
        //String destFileDir = Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name);
        //String fileName = new File(mDownloadUrl).getName();
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            //onCompleted();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e("saveFile", e.getMessage());
            }
        }
    }


    public static void close(Closeable... closeables) {

        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    closeable = null;
                }
            }
        }


      /*  int length = closeables.length;

        try {

            for (int i = 0; i < length; i++) {

                Closeable closeable = closeables[i];

                if (null != closeable)

                    closeables[i].close();

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            for (int i = 0; i < length; i++) {

                closeables[i] = null;

            }

        }*/

    }


    /**
     * 将输入流写入文件
     *
     * @param file   文件
     * @param is     输入流
     * @param append 是否追加在文件末
     * @return {@code true}: 写入成功<br>{@code false}: 写入失败
     */
    public static boolean writeFileFromIS(final File file,
                                          final InputStream is,
                                          final boolean append) {


        if (!createOrExistsFile(file) || is == null) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte[] buf = new byte[1024 << 2];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            return true;
        } catch (IOException e) {
            deleteFile(file);
            e.printStackTrace();
            return false;
        } finally {
            close(is, os);

        }
    }



    public static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    /**
     * 文件是否已经存在
     * @param file
     * @return
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }


    @NonNull
    public static File getFile(String mDownloadUrl, String filePath, String fileName) {
        //String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        filePath = TextUtils.isEmpty(filePath)
                ? Environment.getExternalStorageDirectory() + File.separator + App.getContext().getString(R.string.app_name)
                :filePath;
        fileName = TextUtils.isEmpty(fileName)
                ?new File(mDownloadUrl).getName()
                :fileName;

        File fileParent = new File(filePath);
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        File file = new File(fileParent, fileName );
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
