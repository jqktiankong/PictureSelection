package com.jqk.pictureselectorlibrary.util;

/**
 * Created by Administrator on 2018/6/6.
 */

public class MathUtils {
    /**
     * 获取两点之间距离
     *
     * @param x1 点1
     * @param y1 点1
     * @param x2 点2
     * @param y2 点2
     * @return 距离
     */
    public static float getDistance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 获取两点的中点
     *
     * @param x1 点1
     * @param y1 点1
     * @param x2 点2
     * @param y2 点2
     * @return float[]{x, y}
     */
    public static float[] getCenterPoint(float x1, float y1, float x2, float y2) {
        return new float[]{(x1 + x2) / 2f, (y1 + y2) / 2f};
    }
}
