package com.zgw.qgb.helper;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

/**
 * description:
 * Created by Tsinling on 2017/11/4 9:56.
 */

public class ParcelableSparseIntArray extends SparseIntArray implements Parcelable {

    public ParcelableSparseIntArray() {
        super();
    }

    public ParcelableSparseIntArray(Parcel in) {
    }

    public static final Creator<ParcelableSparseIntArray> CREATOR = new Creator<ParcelableSparseIntArray>() {
        @Override
        public ParcelableSparseIntArray createFromParcel(Parcel in) {
            return new ParcelableSparseIntArray(in);
        }

        @Override
        public ParcelableSparseIntArray[] newArray(int size) {
            return new ParcelableSparseIntArray[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

}
