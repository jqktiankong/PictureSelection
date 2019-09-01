package com.jqk.pictureselectorlibrary.base;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.util.AppConstant;

/**
 * Created by Administrator on 2018/6/4 0004.
 */

public class BaseActivity extends AppCompatActivity {
    private LinearLayout emptyView;
    private RelativeLayout contentView;

    public void initView() {
        emptyView = (LinearLayout) findViewById(R.id.emptyView);
        contentView = (RelativeLayout) findViewById(R.id.contentView);
    }

    public void showView(int type) {
        switch (type) {
            case AppConstant.VIEW_EMPTY:
                emptyView.setVisibility(View.VISIBLE);
                contentView.setVisibility(View.GONE);
                break;
            case AppConstant.VIEW_CONTENT:
                emptyView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
