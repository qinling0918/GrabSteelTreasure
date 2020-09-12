package com.zgw.qgb.mvc_common.helper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Created by qinling on 2020/6/17 14:58
 * Description: 搜索历史工具类
 */
public class HistoryHelper {
    private static HashMap<String, HistoryHelper> historys = new HashMap<>();
    private final ArrayDeque<String> historyCaches;
    private final int MAX_SIZE = 5;
    private String historyName;
    private int maxSize = MAX_SIZE;
    private Persistence persistence;

    /**
     * 持久化的方式，可以自定义持久化的方式，默认使用sp
     */
    public interface Persistence {

        // 保存时，内部实现建议使用线程
        void save(String historyNam, String[] histories);

        ArrayList<String> get(String historyName);
    }



    // 回头改到init里面
    private HistoryHelper(String historyName, Persistence persistence) {
        this.historyName = historyName;
        this.persistence = persistence;
        historyCaches = new ArrayDeque<String>();
        if (null != persistence) {
            historyCaches.addAll(persistence.get(historyName));
        }

    }

    public static HistoryHelper getInstance() {
        return getInstance("", new SP());
    }

    public static HistoryHelper getInstance(String historyName) {
        return getInstance(historyName, new SP());
    }

    public static HistoryHelper getInstance(String historyName, Persistence persistence) {
        if (isSpace(historyName)) historyName = "HistoryHelper";
        HistoryHelper helper = historys.get(historyName);
        if (helper == null) {
            helper = new HistoryHelper(historyName, persistence);
            historys.put(historyName, helper);
        }
        return helper;
    }


    public String[] getAll() {
        return get(getMaxSize());
    }

    public String[] get(int maxNum) {
        if (maxNum < 0) {
            maxNum = 0;
        }
        String[] histories = historyCaches.toArray(new String[0]);
        // 若是传入的数量大于当前缓存中保留的元素数量，则直接返回
        if (maxNum >= histories.length) {
            return histories;
        }
        return Arrays.copyOf(histories, maxNum);
    }

    /**
     * 保存历史记录
     *
     * @param info
     */
    public void save(String info) {
        if (InputHelper.isEmpty(info)) {
            return;
        }
        if (historyCaches.contains(info)) {
            historyCaches.removeFirstOccurrence(info);
        }
        // 加入队列
        boolean added = historyCaches.offerFirst(info);
        // 若是加入成功，并且当前元素个数大于最大值
        if (added && historyCaches.size() > getMaxSize()) {
            String polled = historyCaches.pollLast();
            // System.out.println(polled);
        }
        if (null != persistence) {
            persistence.save(historyName, getAll());
        }

    }

    private int getMaxSize() {
        return maxSize;
    }

    /**
     * 设置需要保留的记录数
     *
     * @param maxSize
     */
    public HistoryHelper setMaxSize(int maxSize) {
        if (maxSize < 0) {
            throw new NegativeArraySizeException("maxNum can not < 0, maxNum =" + maxSize);
        }
        this.maxSize = maxSize;
        resetByMaxSize(maxSize);
        return this;
    }

    /**
     * 根据传入的新的缓冲值，将现有的缓存区中的元素数进行调整
     *
     * @param maxSize
     */
    private void resetByMaxSize(int maxSize) {
        // 若是新设置的最大值小于当前，就把在队列中的最老的元素删除
        int diff = historyCaches.size() - maxSize;
        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                historyCaches.pollLast();
            }
        }
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 默认使用SP实现持久化
     */
    public static final class SP implements Persistence {

        @Override
        public void save(String historyName, String[] histories) {
            PrefUtils.saveHistories(historyName, new ArrayList<String>(Arrays.asList(histories)));
        }

        @Override
        public ArrayList<String> get(String historyName) {
            return PrefUtils.getHistories(historyName);
        }
    }
}
