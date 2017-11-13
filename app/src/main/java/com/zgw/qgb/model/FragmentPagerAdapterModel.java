package com.zgw.qgb.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.zgw.qgb.R;
import com.zgw.qgb.ui.moudle.find.FindFragment;
import com.zgw.qgb.ui.moudle.message.MessageFragment;
import com.zgw.qgb.ui.moudle.mine.MineFragment;
import com.zgw.qgb.ui.moudle.purchase.PurchaseFragment;
import com.zgw.qgb.ui.moudle.quote.QuoteFragment;
import com.zgw.qgb.ui.moudle.quote.QuoteListFragment;
import com.zgw.qgb.ui.moudle.quote.QuoteMapFragment;

import java.util.List;

/**
 * Created by Kosh on 03 Dec 2016, 9:26 AM
 */

public class FragmentPagerAdapterModel {

    private String title;
    private Fragment fragment;

    private FragmentPagerAdapterModel(String title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @NonNull
    public static List<FragmentPagerAdapterModel> buildForMain(@NonNull Context context) {

        return Stream.of(new FragmentPagerAdapterModel(context.getString(R.string.quotes), QuoteFragment.newInstance(/*context.getString(R.string.quotes)*/)),
                new FragmentPagerAdapterModel(context.getString(R.string.find), FindFragment.newInstance(context.getString(R.string.find))),
                new FragmentPagerAdapterModel(context.getString(R.string.purchase), PurchaseFragment.newInstance(context.getString(R.string.purchase))),
                new FragmentPagerAdapterModel(context.getString(R.string.message), MessageFragment.newInstance(context.getString(R.string.message))),
                new FragmentPagerAdapterModel(context.getString(R.string.mine), MineFragment.newInstance(context.getString(R.string.mine))))
                .collect(Collectors.toList());
    }

    @NonNull
    public static List<FragmentPagerAdapterModel> buildForQuote(@NonNull Context context) {
        String[] title = context.getString(R.string.quote_list_map).split("|");
        return Stream.of(new FragmentPagerAdapterModel(title[0], QuoteListFragment.newInstance(title[0])),
                new FragmentPagerAdapterModel(title[1], QuoteMapFragment.newInstance(title[1])))
                .collect(Collectors.toList());
    }
}
