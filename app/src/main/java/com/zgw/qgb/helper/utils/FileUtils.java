package com.zgw.qgb.helper.utils;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
