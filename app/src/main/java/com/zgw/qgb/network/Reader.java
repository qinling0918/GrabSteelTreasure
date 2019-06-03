package com.zgw.qgb.network;

import android.os.Environment;
import android.util.Log;


import com.zgw.qgb.helper.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 要点:
 * 1, 将一个文件分为 ,大小为 {@link Reader#segmentLength} 的 {@link Reader#segments} 个文件
 * ,每个线程通过读取pure block来读取数据,被访问的块会通过{@link Reader#segment}标记
 * ,自增1.
 * 2, 多个线程的全局变量是final {@link Reader#source}, final {@link Reader#target}和
 * {@link Reader#segment},需要保证这些变量的安全性
 */
public class Reader implements Runnable {
    private static final int BYTE = 1024;
    private static final long NEGATIVE_ONE = -1L;
    private static final long ZERO = 0L;
    private static final long ONE = 1L;
    // 全局变量 供多线程访问块
    private final AtomicLong segment = new AtomicLong(NEGATIVE_ONE);
    // 单个文件大小
    private final int segmentLength = 30 * BYTE * BYTE;
    // 原始文件
    private final File source;
    // 复制后的文件
    private final File target;
    // 文件被分割后的块数
    private final long segments;
    // 最后一块文件实际大小
    private final long remains;

    public Reader(String sourcePath, String targetPath) throws IOException {
        this.source = new File(sourcePath);
        this.target = new File(targetPath);
        if (!this.target.exists()) {
            this.target.createNewFile();
        }
        this.remains = (this.source.length() % segmentLength);
        //如果余数不为0, 则需要多一个块来存储多余的bytes,否则会丢失
        if (this.remains != ZERO) {
            this.segments = this.source.length() / segmentLength + ONE;
        } else {
            this.segments = sourcePath.length() / segmentLength;
        }

    }

    /**
     * run:
     * 1, while true: 当前块未被访问, 从{@link Reader#segment = 0}开始第一次访问
     * 2, {@link Reader#readBlock(RandomAccessFile, long)}从文件中读取数据,并返回 byte[]
     * 3, {@link Reader#writeBlock(RandomAccessFile, byte[], long)},设置position后将缓冲写入文件
     */
    public void run() {
        RandomAccessFile reader = null;
        RandomAccessFile writer = null;
        try {
            reader = new RandomAccessFile(source, "r");
            writer = new RandomAccessFile(target, "rw");
            long position = -1L;
            //循环计数当前segment, 多个线程均可修改
            while ((position = segment.incrementAndGet()) < segments) {
                final byte[] bytes = readBlock(reader, position);
                writeBlock(writer, bytes, position);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
            close(reader);
        }
    }

    private void writeBlock(RandomAccessFile writer, byte[] bytes, long position) throws IOException {
        writer.seek(position * segmentLength);
        writer.write(bytes);
    }

    /**
     * 1, reader设置position
     * 2, 创建缓冲数组
     * 3, 将数据写入byte[]
     * 4, 返回缓冲数组
     *
     * @return position 供 {@link RandomAccessFile#write(byte[])}使用
     */
    private byte[] readBlock(RandomAccessFile reader, long position) throws IOException {
        reader.seek(position * segmentLength);
        final byte[] bytes = new byte[getWriteLength(position)];
        reader.read(bytes);
        return bytes;
    }

    /**
     * 获得当前byte[]实际可写入长度可能是{@link Reader#segmentLength} 或者 {@link Reader#remains}
     */
    private int getWriteLength(long position) throws IOException {
        if (position == segments + NEGATIVE_ONE && remains > ZERO) {
            return (int) remains;
        }
        return segmentLength;
    }

    /**
     * 关闭流的通用接口方法
     *
     * @param closeable
     */

    private void close(Closeable closeable) {
        try {
            if (Objects.nonNull(closeable)) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
