package com.zgw.qgb.network.download;

import android.os.Handler;
import androidx.annotation.NonNull;

import android.util.Log;
import android.util.SparseLongArray;

import com.zgw.qgb.helper.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.CANCELED;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.PAUSED;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.PROGRESS;
import static com.zgw.qgb.network.download.DownLoadInfoManager.Status.SUCCESS;


/**
 * Created by @author qinling on 2018/11/23 11:43
 * Description: 下载文件管理类  包括 下载进度临时记录文件  以及文件缓存
 */
public class DownLoadInfoManager {
    private static final String TAG = "DownLoadInfoManager";
    private static final String CACHE_FILE_SUFFIX = ".cache";
    private static final String TEMP_FILE_SUFFIX = ".temp";
    private static final long NO_TOTAL_FILE_SIZE = -1L;
    // private static final long DEFAULT_BLOCK_COUNT = 1;
    private static final int DEFAULT_BLOCK_COUNT = 1;
    private static final int FRONT = 0;
    private static final int LAST = 1;
    private static final int BUFFER = 1024 << 2;

    private Handler sHandler;

    public void setHandler(Handler handler) {
        this.sHandler = handler;
    }


    /**
     * 下载信息临时文件格式
     */
    enum TempFileFormat {
        /**
         * 总文件下载状态 1byte  0-255
         * // 已完成块数
         * 文件总大小 4byte  0- 0xffffffff （0 - 4g）
         * 每一块文件大小 4byte 0- 0xffffffff  （0 - 4g）
         * 下载总块数 4byte blocks  0- 0xffffffff （0 - 4g）
         * 每块下载状态 null 字节数动态计算  对应块数下载的进度
         * <p>
         * // 后续 可以自定义拓展 。例如记录以下信息
         * 文件名称 10byte
         * 文件类型 2byte
         * 文件CRC值 2byte
         * 断点信息CRC 2byte
         */
        // STATUS_CODE("FF"),
        // COMPLETE_BLOCK_CODE("FF"),
        TOTAL_FILE_SIZE_CODE("FFFFFFFF"),
        //  CURRENT_FILE_SIZE_CODE("FFFFFFFF"),
        BLOCK_FILE_SIZE_CODE("FFFFFFFF"),
        TOTAL_BLOCK_COUNT_CODE("FFFFFFFF"),
        TOTAL_BLOCK_AREA_CODE(null);

        private String format;

        TempFileFormat(String format) {
            this.format = format;
        }

        /*** @return int 获取对应区域的字节数  */
        public int length() {
            return NumberConvert.getStringByteSize(format);
        }
    }

    // TODO 调整为起始和结束值。
    /*** 记录需要下载的的块的信息， 其中key为块号，值为每块当前已下载的长度  */
    private SparseLongArray remainingBlocks;
    /*** 是不是单线程*/
    // private boolean isSingleThread = true;

    /*** 是否下载状态  0 未完成， 1完成 */
    private int status = Status.START;


    private boolean fileIsExiset;
    /**
     * 文件总大小
     */
    private long totalFileSize = NO_TOTAL_FILE_SIZE;
    AtomicLong currentFileSize = new AtomicLong(0);

    /*** 下载总块数  ,默认为1,即单线程 */
    private int totalBlockCount = DEFAULT_BLOCK_COUNT;

    /*** 文件被分段后，最后一段有可能和前面块长度不一致
     * 每一块数据块的文件长度 [Front] 表示 前面多块， [LAST] 表示最后一块的长度 */
    private long[] blockFileSizeArr = new long[2];

    /*** 所有块下载状态  用半个字节表示一个块的状态*/
    private long blockStatus;
    /*** 文件路径*/
    private String filePath;
    /*** 文件名*/
    private String fileName;
    /*** 校验和 */
    private String compareCodeSumHexStr;
    /*** 记录下载信息的临时文件*/
    private File downloadTempFile;
    // private RandomAccessFile tempRas;

    /**
     * 文件
     */
    private File downloadFile;
    private RandomAccessFile ras;

    /**
     * 初始化
     * 以块数为基准，判断每块大小
     *
     * @param totalFileSize   文件总大小
     * @param totalBlockCount 所分块数
     */
    public synchronized void initWithBlockCount(long totalFileSize, int totalBlockCount) {

        this.totalFileSize = totalFileSize <= 0 ? NO_TOTAL_FILE_SIZE : totalFileSize;
        // 以缓冲池大小为每一块最小值，计算出最多有多少块
        long maxBlockCount = getDefaultBlockCount(totalFileSize, BUFFER);
        // 若是块数为 0x7fffffff，可能就是类型转换溢出
        totalBlockCount = totalBlockCount > maxBlockCount ? (int) maxBlockCount : totalBlockCount;
        // 文件总大小 小于设置的缓冲区的值，或者传入的块数小于1
        this.totalBlockCount = (totalFileSize <= BUFFER || totalBlockCount <= 1)
                ? 1 : totalBlockCount;

        this.blockFileSizeArr = getDefaultBlockFileSize();
        // 只有totalBlockCount 不为1，且totalFileSize>0
        // isSingleThread = this.totalBlockCount == 1;
        downloadTempFile = null;
        init();
    }

    /**
     * 初始化。
     * 以块为基准，判断分为多少块
     *
     * @param totalFileSize 文件总大小
     * @param blockFileSize 所分块大小
     */
    public synchronized void initWithBlockLength(long totalFileSize, long blockFileSize) {
        this.totalFileSize = totalFileSize <= 0 ? NO_TOTAL_FILE_SIZE : totalFileSize;
        // 若是传入的每块文件大小 > 总文件大小  ,则以总文件大小为准
        blockFileSize = blockFileSize > totalFileSize ? totalFileSize : blockFileSize;
        long lastBlockFileSize = getLastBlockFileSize(totalFileSize, blockFileSize);

        // 若是设置的每一块大小 小于一次写入时的缓冲区大小，则以缓冲区大小为主。
        this.blockFileSizeArr[0] = blockFileSize <= BUFFER || blockFileSize <= 0
                ? BUFFER : blockFileSize;
        this.blockFileSizeArr[1] = lastBlockFileSize <= BUFFER || lastBlockFileSize <= 0
                ? BUFFER : lastBlockFileSize;

        this.totalBlockCount = getDefaultBlockCount(totalFileSize, this.blockFileSizeArr[0]);

        // isMultiThread = this.totalBlockCount != 1;
        downloadTempFile = null;
        init();
    }


    public synchronized SparseLongArray getRemainingBlocks() {
        return remainingBlocks;
    }

    public synchronized long getTotalFileSize() {
        return totalFileSize;
    }

    public synchronized String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTotalBlockCount() {
        return totalBlockCount;
    }

    public int getStatus() {
        return status;
    }

    /**
     * 被下载的文件是否存在
     *
     * @return true： 存在
     */

    public boolean isFileExists() {
        Log.d(TAG, "isFileExists: " + filePath + File.separator + fileName);
        File downloadFile = new File(filePath, fileName);
        if (downloadFile.exists() && downloadFile.isFile()) {
            status = SUCCESS;
            this.downloadFile = downloadFile;
            fileIsExiset = true;
        }
        return fileIsExiset;
    }

    /**
     * @return 获取通过临时记录文件获取到的每一块的大小信息
     * [0] 每一块的大小
     * [1] 最后一块的大小
     */
    public long[] getBlockFileSizeArr() {
        return blockFileSizeArr;
    }

    public File getDownloadCacheFile() {
        return downloadFile;
    }


    /**
     * 删除所下载的文件以及临时记录文件
     *
     * @return true 删除成功
     */
    public boolean deleteFile() {
        return deleteTempFile() && deleteCacheFile();
    }

    /**
     * 删除临时记录文件
     *
     * @return true 删除成功
     */
    public boolean deleteTempFile() {
        File file = new File(filePath, fileName + TEMP_FILE_SUFFIX);
        return FileUtils.deleteFile(file);
    }


    /**
     * 删除缓存文件 两个状态 未完成：.cache   完成后则无后缀。
     * （完成后即为需要下载的文件）
     *
     * @return boolean true 删除成功
     */
    public boolean deleteCacheFile() {
        File cacheFile = new File(filePath, fileName + CACHE_FILE_SUFFIX);
        File file = new File(filePath, fileName);
        return FileUtils.deleteFile(cacheFile) || FileUtils.deleteFile(file);
    }

    @NonNull
    private File createTempFile() throws IOException {
        File file = new File(filePath, fileName + TEMP_FILE_SUFFIX);
        // 如果临时文件不存在则创建以及初始化
        if (!(file.exists() && file.isFile())) {
            if (FileUtils.createOrExistsFile(file)) {
                writeInfoToTempFile(file);
            } else {
                throw new IOException("下载文件信息记录文件创建失败！");
            }
        }
        return file;
        //tempRas.seek(getTempFileHeaderLength()*2);
    }

    private static class SingleTon {
        private static DownLoadInfoManager instance = new DownLoadInfoManager();
    }


    /**
     * 分为多少块，块大小自行计算。
     *
     * @param downloadPath 下载文件地址
     * @param fileName     文件名
     * @return 临时文件记录文件管理
     */


    private static DownLoadInfoManager getInstance(String downloadPath, String fileName) {
        SingleTon.instance.initFile(downloadPath, fileName);
        return SingleTon.instance;
    }

    private void initFile(String downloadPath, String fileName) {
        this.filePath = downloadPath;
        this.fileName = fileName;
        if (isFileExists()) {
            return;
        }


    }


    private File createDownloadTempFile(String downloadPath, String fileName) throws IOException {
        File file = new File(downloadPath, fileName + TEMP_FILE_SUFFIX);
        if (!FileUtils.createOrExistsFile(file)) {
            throw new IOException("下载文件信息记录文件创建失败！");
        }
        return file;
    }


    private DownLoadInfoManager() {

    }

    public DownLoadInfoManager(String downloadPath, String fileName) {
        initFile(downloadPath, fileName);
        init();
    }

    public DownLoadInfoManager(String downloadPath, String fileName, long totalFileSize, int totalBlockCount) {
        initFile(downloadPath, fileName);
        initWithBlockCount(totalFileSize, totalBlockCount);
    }

    public DownLoadInfoManager(String downloadPath, String fileName, long totalFileSize, long blockFileSize) {
        initFile(downloadPath, fileName);
        initWithBlockLength(totalFileSize, blockFileSize);
    }

    /**
     * @param tempRas 记录文件下载信息的文件的RandomAccessFile 对象。
     * @param length  从指针所在位置读取length的长度
     * @return String 读到的数据，此时指针将会往后移动 length 位
     * @throws IOException 文件操作异常
     */
    private String readMessage(RandomAccessFile tempRas, int length) throws IOException {
        //  byte[] bytes = new byte[length];
        byte[] bytes = new byte[length];
        // read后，指针会默认往后移， 移动的长度与所读数据长度相同
        int len = tempRas.read(bytes);
        // return len == -1 ? "" : NumberConvert.bytesToHexString(bytes);
        return len == -1 ? "" : new String(bytes);
    }

    private void writeMessage(RandomAccessFile tempRas, String msg) throws IOException {
        // tempRas.writeChars(msg);
        tempRas.write(msg.getBytes());
        //tempRas.write(NumberConvert.hexStringToBytes(msg));
    }

    /**
     * 获取只读的 RandomAccessFile 对象
     *
     * @return 只读的 RandomAccessFile 对象
     */

    private RandomAccessFile getRandomAccessFile(File file) {
        RandomAccessFile tempRas = null;
        try {
            tempRas = new RandomAccessFile(file, "rwd");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getRandomAccessFile: ", e);
            e.printStackTrace();
        }
        return tempRas;
    }


    private long[] getDefaultBlockFileSize() {

        if (totalFileSize <= 0 || totalBlockCount <= 0) {
            return new long[]{BUFFER, BUFFER};
        }
        long[] blockFileSize = new long[2];
        long remains = totalFileSize % totalBlockCount;
        if (remains == 0) {
            blockFileSize[0] = blockFileSize[1] = totalFileSize / totalBlockCount;
        } else {
            blockFileSize[0] = (totalFileSize / totalBlockCount) + 1;
            blockFileSize[1] = totalFileSize % blockFileSize[0];
        }
        return blockFileSize;
    }

    /**
     * 根据文件总长度，以及定义的每块长度，获取最后一块的长度
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块长度
     * @return 最后一块的长度
     */
    private long getLastBlockFileSize(long totalFileSize, long blockFileSize) {
        if (totalFileSize <= 0 || blockFileSize <= 0) {
            return 0;
        }
        long lastBlockFileSize = totalFileSize % blockFileSize;
        return lastBlockFileSize == 0 ? blockFileSize : lastBlockFileSize;
    }

    /**
     * 根据文件总长度，每块文件长度 获取块数
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块文件长度
     * @return int 总块数
     */
    private int getDefaultBlockCount(long totalFileSize, long blockFileSize) {
        // 若所传数据均不合法，则默认为一块
        if (totalFileSize <= 0 || blockFileSize <= 0 || totalFileSize <= blockFileSize) {
            return 1;
        }
        // 是否有余数
        boolean hasRemainder = (totalFileSize % blockFileSize) != 0;
        int blockSize = (int) (totalFileSize / blockFileSize);
        return hasRemainder ? blockSize + 1 : blockSize;
    }

    /**
     * 计算临时下载信息文件长度
     */
    long tempFileContentLength = 0;

    private long calculateTempFileLength() {
        if (tempFileContentLength == 0) {
            // 通过每一块起始位置 + 该块的长度。
            // 即 getTempFileHeaderLength() + getOneBlockAreaByteSize() * totalBlockCount-1 + getOneBlockAreaByteSize()
            tempFileContentLength = getTempFileHeaderLength() * 2 + getOneBlockAreaByteSize() * totalBlockCount;
        }
        return tempFileContentLength;

    }


    /**
     * 获取临时文件格式头  状态码+ 文件大小总长度 + 总块数 所占字节数。
     * 即获取记录每一块数据域起始位置。
     * ps：该方法多次调用，则定义一个全局变量， 若已经有值，则直接返回，不用再执行代码块
     */
    private int tempFileHeaderLength = 0;

    private int getTempFileHeaderLength() {
        if (tempFileHeaderLength == 0) {
            tempFileHeaderLength = getStartIndexByTempFileFormat(TempFileFormat.TOTAL_BLOCK_AREA_CODE);
        }
        // 获取记录每一块数据域起始位置。
        return tempFileHeaderLength;
    }


    /**
     * 获取每一块的起始位置。
     *
     * @param format TempFileFormat 对应的数据域格式
     * @return int 对应的数据信息域块的起始位置
     */
    private int getStartIndexByTempFileFormat(TempFileFormat format) {
        int len = 0;
        for (TempFileFormat tempFileFormat : TempFileFormat.values()) {
            if (format.equals(tempFileFormat)) {
                return len;
            }
            len += tempFileFormat.length();
        }
        return len;
    }

    /**
     * 获取分块数据 对应区域长度
     *
     * @return int
     */
    private int getOneBlockStatusAreaLength() {
        // 记录每块信息的一块信息域的字节数
        return NumberConvert.getStringByteSize(Long.toHexString(blockFileSizeArr[FRONT]));
        // 记录前面若干块的长度的16进制所占字节数。
        // 记录最后一块的长度的16进制所占字节数。
        //int lastBlockFileSizeStringLen = Long.toHexString(blockFileSizeArr[LAST]).length();
        // return oneBlockFilleSizeAreaByte * totalBlockCount ;
    }


    /**
     * @return 获取下载信息记录临时文件的格式头信息
     */
    private String getHeaderInfo() {
        return String.format("%s%s%s",
                //     NumberConvert.toHexStrWithAddZero(START.value, TempFileFormat.STATUS_CODE.length()*2),
                NumberConvert.toHexStrWithAddZero(totalFileSize, TempFileFormat.TOTAL_FILE_SIZE_CODE.length() * 2),
                NumberConvert.toHexStrWithAddZero(blockFileSizeArr[FRONT], TempFileFormat.BLOCK_FILE_SIZE_CODE.length() * 2),
                NumberConvert.toHexStrWithAddZero(totalBlockCount, TempFileFormat.TOTAL_BLOCK_COUNT_CODE.length() * 2));
    }


    /**
     * 记录每一块当前进度。
     *
     * @param blockNum      块号
     * @param contentLength 块长度
     */
    public void writeBlockCurrentSizeToFile(RandomAccessFile tempRas, int blockNum, long contentLength) {
        //RandomAccessFile tempRas = getRandomAccessFile(downloadTempFile);
        try {
            // RandomAccessFile tempRas = getRandomAccessFile(downloadTempFile);
            // 将指针调整到指定位置
            long pos = getBlockPos(blockNum);
            tempRas.seek(pos);
            int len = getOneBlockAreaByteSize();
            String currentBlockLengthHexStr = NumberConvert.toHexStrWithAddZero(contentLength, len);
            // Log.e(TAG, "writeBlockCurrentSizeToFile, blockNum: " + blockNum + "  contentLength: " + contentLength + " index " + getBlockPos(blockNum) + " hex:" + currentBlockLengthHexStr);
            writeMessage(tempRas, currentBlockLengthHexStr);
            // tempRas.close();
        } catch (IOException e) {
            // Log.e(TAG, "writeBlockCurrentSizeToFile: ", e);
            e.printStackTrace();
        }
    }


    /**
     * 根据块号 获取 块号起始对应的位置
     *
     * @param blockNum 块号  0到 totalBlockCount -1
     * @return 块号对应块所在的起始长度。
     */
    private long getBlockPos(/*@IntRange(from = 0)*/ int blockNum) {
        // 一块 记录每块当前进度信息的区域的字节数
        int oneBlockAreaByteSize = getOneBlockAreaByteSize();
        if (blockNum > totalBlockCount) {
            String errMsg = String.format(Locale.CHINA, "获取块号为%d的信息域的起始位置失败，超出总块数%d。", blockNum, totalBlockCount);
            throw new IndexOutOfBoundsException(errMsg);
        }
        if (blockNum < 0) {
            String errMsg = String.format(Locale.CHINA, "获取块号为%d的信息域的起始位置失败，不能为负值。", blockNum);
            throw new IndexOutOfBoundsException(errMsg);
        }
        return getTempFileHeaderLength() * 2 + oneBlockAreaByteSize * blockNum;
    }

    /**
     * 获取记录每一块进度信息的信息域的字节数
     * ps：该方法多次调用，则定义一个全局变量， 若已经有值，则直接返回，不用再执行代码块
     */
    private int oneBlockAreaByteSize = -1;

    private int getOneBlockAreaByteSize() {
        if (oneBlockAreaByteSize == -1) {
            //  oneBlockAreaByteSize = NumberConvert.getStringByteSize(Long.toHexString(blockFileSizeArr[FRONT]));
            oneBlockAreaByteSize = Long.toHexString(blockFileSizeArr[FRONT]).length();
        }
        return oneBlockAreaByteSize;
    }

    private void init() {
        remainingBlocks = initRemainingBlocks();

        // isSingleThread = this.totalBlockCount == 1;
        // 多线程模式
        RandomAccessFile tempRas = null;
        if (!isSingleThread()) {
            if (null == downloadTempFile && totalBlockCount > 1) {
                try {
                    // 若文件不存在，则创建以及初始化
                    downloadTempFile = createTempFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            readInfoFromFile();
        }
    }

    private SparseLongArray initRemainingBlocks() {
        SparseLongArray array = new SparseLongArray(totalBlockCount);
        for (int i = 0; i < totalBlockCount; i++) {
            array.put(i, 0);
        }
        return array;
    }


    private boolean isSingleThread() {
        return totalBlockCount == 1;
    }

    private RandomAccessFile writeInfoToTempFile(File downloadTempFile) {
        RandomAccessFile tempRas = null;
        try {
            tempRas = getRandomAccessFile(downloadTempFile);
            String headInfo = getHeaderInfo();
            String blockInfo = getBlockInfo();
            // 将指针调整到 0
            //tempRas.setLength(calculateTempFileLength()*2);
            tempRas.seek(0);
            writeMessage(tempRas, headInfo + blockInfo);

            //tempRas.close();
        } catch (IOException e) {
            Log.e(TAG, "writeInfoToTempFile", e);
            e.printStackTrace();
        }
        return tempRas;
    }

    private String getBlockInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < getOneBlockAreaByteSize() * totalBlockCount; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }


    /**
     * 通过临时文件获取 文件总长度，所分块数。每块大小等信息。
     */
    private RandomAccessFile readInfoFromFile() {
        RandomAccessFile tempRas = getRandomAccessFile(downloadTempFile);

        try {
            //   Log.e(TAG, "readBlockCurrentSize: readInfoFromFile" + tempRas.readLine());
            if (downloadTempFile.length() < getTempFileHeaderLength()) {
                throw new IndexOutOfBoundsException("文件长度有错误！");
            }
            readHeaderInfoFromFile(tempRas);
            // checkFileLength(tempRas);
            // 此时指针已经到了 记录每块信息的起始位置
            readBlockCurrentSize(tempRas);

            tempRas.close();
        } catch (Exception e) {
            Log.e(TAG, "readInfoFromFile:Exception ", e);
            e.printStackTrace();
        }
        return tempRas;

    }

    /**
     * 读取每一块当前下载的文件长度， 若与定义的长度不符，则记录到集合中。
     *
     * @param tempRas
     */
    private void readBlockCurrentSize(RandomAccessFile tempRas) throws IOException {
        // 指针已经指向 记录块长度的区域的起始位置
        // int blockFileSizeAreaStartIndex = getTempFileHeaderLength() ;
        // tempRas.seek(blockFileSizeAreaStartIndex);

        remainingBlocks = initRemainingBlocks();
        for (int i = 0; i < totalBlockCount; i++) {
            long oneBlockSize = i == totalBlockCount - 1 ? blockFileSizeArr[LAST] : blockFileSizeArr[FRONT];
            String hex = readMessage(tempRas, getOneBlockAreaByteSize());
            Log.e("readBlockCurrentSize", "hex " + hex + " point: " + (tempRas.getFilePointer() - hex.length() * 2));
            long blockCurrentSize = Long.parseLong(hex, 16);

            // 通过下载记录文件，获取已经下载文件长度，（多线程下，并不连续）
            currentFileSize.addAndGet(blockCurrentSize);
            // 例如某块为512， 则该区域的值为0-511，
            if (blockCurrentSize + 1 == oneBlockSize) {
                remainingBlocks.removeAt(remainingBlocks.indexOfKey(i));
            } else if (blockCurrentSize > 0) {
                remainingBlocks.put(i, blockCurrentSize);
            }
            Log.e("readBlockCurrentSize", "blockCurrentSize " + remainingBlocks.get(i));
        }
    }

    /**
     * 读取头信息 ，包括 下载状态 ，文件总长度， 每一块的文件大小， 总块数
     *
     * @param tempRas
     * @throws Exception 文件读写
     */
    private void readHeaderInfoFromFile(RandomAccessFile tempRas) throws Exception {
        // 将指针调整到 0
        tempRas.seek(0);
        //   int statusCode = Integer.parseInt(readMessage(tempRas,TempFileFormat.STATUS_CODE.length()), 16);
        // 若是已完成，则保存。 若是其他状态则重置
        //  status = Status.values()[statusCode] == SUCCESS ? SUCCESS : START;
        //  tempRas.skipBytes(TempFileFormat.STATUS_CODE.length());
        String hex = readMessage(tempRas, TempFileFormat.TOTAL_FILE_SIZE_CODE.length() * 2);
        totalFileSize = Long.parseLong(hex, 16);
        //tempRas.skipBytes(TempFileFormat.TOTAL_FILE_SIZE_CODE.length());

      /*  currentFileSize = Long.parseLong(readMessage(TempFileFormat.CURRENT_FILE_SIZE_CODE.length()), 16);
        tempRas.skipBytes(TempFileFormat.CURRENT_FILE_SIZE_CODE.length());*/

        blockFileSizeArr[FRONT] = Long.parseLong(readMessage(tempRas, TempFileFormat.BLOCK_FILE_SIZE_CODE.length() * 2), 16);
        blockFileSizeArr[LAST] = getLastBlockFileSize(totalFileSize, blockFileSizeArr[FRONT]);
        //tempRas.skipBytes(TempFileFormat.BLOCK_FILE_SIZE_CODE.length());

        totalBlockCount = Integer.parseInt(readMessage(tempRas, TempFileFormat.TOTAL_BLOCK_COUNT_CODE.length() * 2), 16);
        //tempRas.skipBytes(TempFileFormat.TOTAL_BLOCK_COUNT_CODE.length());


    }

    /**
     * 校验文件长度。
     *
     * @param tempRas
     */
    private void checkFileLength(RandomAccessFile tempRas) {
        long contentLength = calculateTempFileLength();
        if (downloadTempFile.length() != contentLength) {
            throw new IndexOutOfBoundsException(String.format(
                    Locale.CHINA, "文件长度不正确。现有文件长度：%s，应为%s",
                    downloadTempFile.length(), contentLength));
        }
    }

    @Override
    public String toString() {
        return "DownLoadInfoManager{" +
                "status=" + status +
                ", totalFileSize=" + totalFileSize +
                ", totalBlockCount=" + totalBlockCount +
                ", blockFileSizeArr=" + Arrays.toString(blockFileSizeArr) +
                ", blockStatus=" + blockStatus +
                ", remainingBlocks=" + remainingBlocks.toString() +
                '}';
    }

    /**
     * 获取在调用该方法时 一些信息
     *
     * @return String  全局类名+方法名+方法所在行数
     */
    private String getStackTraceInfo() {
        //获取堆栈跟踪单元
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int pos = 2;
        pos = stackTraceElements.length > pos ? pos : stackTraceElements.length - 1;
        StackTraceElement stackTraceElement = stackTraceElements[pos];
        return stackTraceElement.getClassName() + "."
                + stackTraceElement.getMethodName() + "."
                + stackTraceElement.getLineNumber();
    }


    public void afterDownLoadSuccess() {
        //下载完毕后，重命名目标文件名
        downloadFile.renameTo(new File(filePath, fileName));
    }


    public void writeToCacheFile(InputStream inputStream, long startIndex, long endIndex) {

        // 获取前面已创建的文件.
        if (isSingleThread()) {
            // 通常endIndex为文件长度
            writeToCacheFileSingleThread(inputStream, startIndex, endIndex);
        } else {
            writeToCacheFileMultiThread(inputStream, startIndex, endIndex);
        }
    }


    public void writeToCacheFileMultiThread(InputStream is, long startIndex, long endIndex) {
        int blockNum = (int) (startIndex / blockFileSizeArr[FRONT]);
        // 获取流
        // 获取前面已创建的文件.
        downloadFile = new File(filePath, fileName + CACHE_FILE_SUFFIX);
        FileUtils.createOrExistsFile(downloadFile);
        AtomicLong blockLength = new AtomicLong(remainingBlocks.get(blockNum));
        Log.e(TAG, "writeBlockCurrentSizeToFile, blockNum: " + blockNum + "blockLength: " + blockLength.get());
        RandomAccessFile tempRas = null;
        try {
            tempRas = new RandomAccessFile(downloadTempFile, "rwd");
            RandomAccessFile ras = new RandomAccessFile(downloadFile, "rwd");
            //  RandomAccessFile ras = getRandomAccessFile(downloadFile);
            // 文件写入的开始位置.
            // Log.e(TAG, "writeToCacheFileMultiThread, blockNum: " + blockNum + "blockLength: " + blockLength.get());
            ras.seek(startIndex);
            /*  将网络流中的文件写入本地*/
            int len;
            byte[] buffer = new byte[BUFFER];
            //读取流
            while ((len = is.read(buffer)) != -1) {
                if (status == PAUSED) {
                    writeBlockCurrentSizeToFile(tempRas, blockNum, blockLength.get());
                    FileUtils.close(is, tempRas, ras);
                    if (sHandler != null) {
                        sHandler.sendEmptyMessage(PAUSED);
                    }

                }
                if (status == CANCELED) {
                    FileUtils.close(is, ras);
                    deleteFile();
                    if (sHandler != null) {
                        sHandler.sendEmptyMessage(CANCELED);
                    }
                }
                // os.flush();
                ras.write(buffer, 0, len);
                blockLength.addAndGet(len);

                currentFileSize.addAndGet(len);
                if (currentFileSize.get() == totalFileSize) {
                    downloadFile.renameTo(new File(filePath, fileName));
                    deleteTempFile();
                    setStatus(SUCCESS);
                    if (sHandler != null) {
                        sHandler.sendEmptyMessage(SUCCESS);
                    }
                }
                // Log.e(TAG, "writeToCacheFileMultiThread, blockNum: " + blockNum + "currentFileSize: " + currentFileSize.get());

                writeBlockCurrentSizeToFile(tempRas, blockNum, blockLength.get());
                if (status == PROGRESS) {
                    progress();
                }

                //  onFizeSizeChange(onFileChangeListener, totalFileSize, currentFileSize.get());
            }
            //  Log.e(TAG, "writeToCacheFileMultiThread, blockNum: " + blockNum + "BlockCurrentSize: " + blockLength.get());
            FileUtils.close(ras, is, tempRas);


        } catch (IOException e) {
            Log.e(TAG, "writeToCacheFileMultiThread", e);
            writeBlockCurrentSizeToFile(tempRas, blockNum, blockLength.get());
            FileUtils.close(ras, is, tempRas);
            e.printStackTrace();
        }


    }

    private void progress() {
        if (sHandler != null) {
            sHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sHandler.sendEmptyMessage(PROGRESS);
                }
            }, 1000);
        }
    }

    public synchronized void writeToCacheFileSingleThread(InputStream is, long startIndex, long endIndex) {
        //int blockNum = (int) (startIndex / blockFileSizeArr[FRONT]);
        AtomicLong blockLength = new AtomicLong(0);
        // 获取流
        OutputStream os = null;
        try {
            downloadFile = new File(filePath, fileName + CACHE_FILE_SUFFIX);
            FileUtils.createOrExistsFile(downloadFile);
            Log.d(TAG, "writeToCacheFileSingleThread: " + downloadFile.getAbsolutePath());
            os = new BufferedOutputStream(new FileOutputStream(downloadFile, true));
            blockLength.addAndGet(downloadFile.length());
            byte[] buffer =  new byte[BUFFER];
            int len;
            while ((len = is.read(buffer)) != -1) {
                if (status == PAUSED) {
                    FileUtils.close(is);
                }
                if (status == CANCELED) {
                    FileUtils.close(is);
                    deleteFile();

                }
                os.flush();
                os.write(buffer, 0, len);

                blockLength.addAndGet(len);
                currentFileSize.addAndGet(len);
                if (status == PROGRESS) {
                    progress();
                }
                if (currentFileSize.get() == totalFileSize) {
                    downloadFile.renameTo(new File(filePath, fileName));
                    deleteTempFile();
                    Log.d(TAG, "writeToCacheFileSingleThread: " + downloadFile.getAbsolutePath());
                    setStatus(SUCCESS);
                    if (sHandler != null) {
                        Log.e("downloadTASK", "onSuccess, currentFileSize: " + currentFileSize.get());
                        sHandler.sendEmptyMessage(SUCCESS);
                    }
                }
            }


        } catch (IOException e1) {
            FileUtils.close(os, is);
            e1.printStackTrace();
        }

        //  FileUtils.close(response);
        //  return downloadInfo.setStatus(SUCCESS);
    }

    /*    public synchronized void writeToCacheFileSingleThread(SequenceInputStream is) {
            //int blockNum = (int) (startIndex / blockFileSizeArr[FRONT]);
            AtomicLong blockLength = new AtomicLong(0);
            // 获取流
            OutputStream os = null;
            try {
                downloadFile = new File(filePath, fileName + CACHE_FILE_SUFFIX);
                FileUtils.createOrExistsFile(downloadFile);
                Log.d(TAG, "writeToCacheFileSingleThread: " + downloadFile.getAbsolutePath());
                os = new BufferedOutputStream(new FileOutputStream(downloadFile, false));
                blockLength.addAndGet(downloadFile.length());
                int len;
                while ((len = is.read(BUFFER)) != -1) {
                    if (status == PAUSED) {
                        FileUtils.close(is);
                    }
                    if (status == CANCELED) {
                        FileUtils.close(is);
                        deleteFile();

                    }
                    os.flush();
                    os.write(BUFFER, 0, len);

                    blockLength.addAndGet(len);
                    currentFileSize.addAndGet(len);
                    onFizeSizeChange(onFileChangeListener, totalFileSize, blockLength.get());
                    if (currentFileSize.get() == totalFileSize) {
                        downloadFile.renameTo(new File(filePath, fileName));
                        // deleteTempFile();
                        Log.d(TAG, "writeToCacheFileSingleThread: " + downloadFile.getAbsolutePath());
                        setStatus(SUCCESS);
                        if (sHandler != null) {
                            Log.e("downloadTASK", "onSuccess, currentFileSize: " + currentFileSize.get());
                            sHandler.sendEmptyMessage(SUCCESS);
                        }
                    }
                }


            } catch (IOException e1) {
                FileUtils.close(os, is);
                e1.printStackTrace();
            }
        }*/
    public class Status {
        public static final int START = 0;//进度
        public static final int PROGRESS = 1;//进度

        public static final int SUCCESS = 2;//完成下载

        public static final int PAUSED = 3;//暂停

        public static final int CANCELED = 4;//取消

        public static final int FAILED = 5;//失败

    }


    void setStatus(int status) {
        this.status = status;
    }

    public void setOnFileChangeListener(OnFileChangeListener listener) {
        this.onFileChangeListener = listener;
    }

    private OnFileChangeListener onFileChangeListener;

    interface OnFileChangeListener {
        void onFileSizeChange(long totalLength, long curLength);
    }

    private void onFizeSizeChange(OnFileChangeListener listener, long totalLength, long curLength) {
        if (null != listener) {
            listener.onFileSizeChange(totalLength, curLength);
        }
    }
}
// onFizeSizeChange(onFizeSizeChangeListener,blockLength.get(),blockLength.addAndGet(len));