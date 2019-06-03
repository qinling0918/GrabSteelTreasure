package com.zgw.qgb.mvc_common.download;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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

    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    public static byte[][] readFile2BytesArrByChannel(final File file, int blockSize) {
        if (!isFileExists(file)) return null;
        FileChannel fc = null;
        byte[][] bytesArr = null;
        try {
            fc = new RandomAccessFile(file, "r").getChannel();
            long contentLength = fc.size();
            int bufferLength = blockSize <= 0 || blockSize >= contentLength
                    ? (int) (contentLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : contentLength)
                    : blockSize;
            int lastBufferLength = (int) getLastBlockFileSize(contentLength, bufferLength);
            int arrlen = getDefaultBlockCount(contentLength, bufferLength);

            bytesArr = new byte[arrlen][];
            for (int i = 0; i < arrlen; i++) {
                ByteBuffer byteBuffer = i == arrlen - 1
                        ? ByteBuffer.allocate(lastBufferLength)
                        : ByteBuffer.allocate(bufferLength);
                while (true) {
                    if (!((fc.read(byteBuffer)) > 0)){
                        bytesArr[i] = byteBuffer.array();
                        break;
                    }
                }
            }
            System.out.println(bytesArr.length);
            return bytesArr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fc != null) {
                    fc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件总长度，以及定义的每块长度，获取最后一块的长度
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块长度
     * @return 最后一块的长度
     */
    private static long getLastBlockFileSize(long totalFileSize, long blockFileSize) {
        if (totalFileSize <= 0 || blockFileSize <= 0) {
            return 0;
        }
        long lastBlockFileSize = totalFileSize % blockFileSize;
        return lastBlockFileSize == 0 ? blockFileSize : lastBlockFileSize;
    }

    private static int getDefaultBlockCount(long totalFileSize, long blockFileSize) {
        // 若所传数据均不合法，则默认为一块
        if (totalFileSize <= 0 || blockFileSize <= 0 || totalFileSize <= blockFileSize) {
            return 1;
        }
        // 是否有余数
        boolean hasRemainder = (totalFileSize % blockFileSize) != 0;
        int blockSize = (int) (totalFileSize / blockFileSize);
        return hasRemainder ? blockSize + 1 : blockSize;
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





}
