package com.zgw.qgb.ui.moudle.quote;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.zgw.qgb.R;
import com.zgw.qgb.helper.ToastUtils;

@Route(path="/ui/moudle/quote/city")
public class CityActivity extends AppCompatActivity {
@Autowired  int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        ARouter.getInstance().inject(this);
        ToastUtils.showNormal(id+"");
    }
}

