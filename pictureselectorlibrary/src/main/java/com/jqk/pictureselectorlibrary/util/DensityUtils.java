package com.jqk.pictureselectorlibrary.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Administrator on 2018/1/3 0003.
 */

public class DensityUtils {
    float density;
    DisplayMetrics dm;

    public DensityUtils() {
        dm = Resources.getSystem().getDisplayMetrics();
        density = dm.density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static float px2dp(float pxValue) {
        return (pxValue / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        return (int) (0.5f + dpValue * density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public float px2dip(float pxValue) {
        return (pxValue / density);
    }

    /**
     * 屏幕宽
     *
     * @return
     */
    public int getScreenWidth() {
        return dm.widthPixels;
    }

    /**
     * 屏幕高
     *
     * @return
     */
    public int getScreenHeight() {
        return dm.heightPixels;
    }
}
