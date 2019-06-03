package com.zgw.qgb.download;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zgw.qgb.download.bean.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qinling on 2018/12/5 11:52
 * Description:
 */
public class ThreadDaoImpl implements ThreadDao {

    private DBHelper dbHelper;

    public ThreadDaoImpl(Context context){
        dbHelper = DBHelper.getInstance(context);
    }
  //  thread_id integer,url text,startIndex long,endIndex long,finished long
    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("insert into thread_info ( thread_id, url, startIndex ,endIndex, finished) values (?,?,?,?,?)"
                ,new Object[]{threadInfo.getId(),threadInfo.getUrl(),threadInfo.getStartIndex(),threadInfo.getEndIndex(),threadInfo.getFinished()});
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, long finished) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?"
                ,new Object[]{finished,url,thread_id});
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url,int thread_id) {
        Log.e("Download", "deleteThread: "+url +"thread_id "+thread_id );
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? and thread_id = ?"
                ,new Object[]{url,thread_id});
        db.close();
    }
    @Override
    public synchronized void deleteTask(String url) {
        Log.e("Download", "deleteThread: "+url  );
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? "
                ,new Object[]{url});
        db.close();
    }
  /*  @Override
    public synchronized void deleteTask(String url) {
        Log.e("Download", "deleteThread: "+url +"thread_id "+thread_id );
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? "
                ,new Object[]{url});
        db.close();
    }*/
    @Override
    public synchronized List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?",new String[]{url});
        List<ThreadInfo> threadInfoList = new ArrayList<>();
        while (cursor.moveToNext()){
            ThreadInfo bean = new ThreadInfo();
            bean.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            bean.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            bean.setStartIndex(cursor.getLong(cursor.getColumnIndex("startIndex")));
            bean.setEndIndex(cursor.getLong(cursor.getColumnIndex("endIndex")));
            bean.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));

            threadInfoList.add(bean);
        }
        cursor.close();
        db.close();
        return threadInfoList;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[]{url,thread_id+""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
