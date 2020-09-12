package com.zgw.qgb.ui.moudle.mine;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zgw.qgb.R;
import com.zgw.qgb.helper.Bundler;
import com.zgw.qgb.mvc_common.helper.PrefUtils;
import com.zgw.qgb.ui.moudle.main.BaseMainFragment;
import com.zgw.qgb.ui.moudle.mine.contract.MineContract;
import com.zgw.qgb.ui.moudle.mine.presenter.MinePresenter;

import java.io.Serializable;
import java.util.Arrays;

import static com.zgw.qgb.helper.BundleConstant.EXTRA;


public class MineFragment extends BaseMainFragment<MinePresenter> implements MineContract.IMineView{

    private String title;

    public MineFragment() {

    }

    public static MineFragment newInstance(String title) {
        MineFragment fragment = new MineFragment();
        fragment.setArguments(Bundler.start()
                .put(EXTRA, title).end());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA);
        }


    }
public  static  final class Bean implements Serializable {
    public Bean(int age, String name) {
        this.age = age;
        this.name = name;
    }

    int age ;
        String name ;




    @Override
    public String toString() {
        return "Bean{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(title);

        ImageView iv_svg = view.findViewById(R.id.iv_svg);
        TextView tv_svg = view.findViewById(R.id.tv_svg);
        tv_svg.setBackgroundResource(R.drawable.ic_back);
        iv_svg.setImageResource(R.drawable.ic_main_find);






       // tv_svg.getCompoundDrawables()[0].setTintList(getResources().getColor(R.color.colorAccent));
       // Drawable drawable = getResources().getDrawable(R.drawable.ic_icon_text);
      //  Drawable drawable = getResources().getDrawable(R.drawable.ic_note);
       // iv_svg.setImageDrawable(drawable);
       // tv_svg.setCompoundDrawables(drawable,null,null,null);
        //DrawableCompat.get(tv_svg.getCompoundDrawables()[0],);
     /*   VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(),R.drawable.ic_main_find,getContext().getTheme());
        //你需要改变的颜色
       // vectorDrawableCompat.setTint(getResources().getColor(R.color.color_blue));
        iv_svg.setImageDrawable(vectorDrawableCompat);
        Drawable drawable = vectorDrawableCompat;
        tv_svg.setCompoundDrawables(null,null,drawable,null);*/
    }


    @Override
    protected int fragmentLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected MinePresenter getPresenter() {
        return new MinePresenter(this);
    }

    @Override
    public void onLazyLoad() {
        
    }
}
