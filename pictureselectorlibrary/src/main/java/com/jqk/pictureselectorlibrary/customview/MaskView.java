package com.jqk.pictureselectorlibrary.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jqk.pictureselectorlibrary.R;
import com.jqk.pictureselectorlibrary.util.L;

public class MaskView extends FrameLayout {
    private ImageView leftImg, rightImg;

    float x;
    float x2;

    float leftImgMoveX, rightImgMoveX;

    float space;

    float unit;

    float viewWidth, leftImgWidth, rightImgWidth;

    float viewHeight;

    float leftImgL, leftImgR, rightImgL, rightImgR;

    private Paint progressPaint;

    float progressX1, progressY1;
    float progressX2, progressY2;

    float time_length;


    private OnScrollXListener onScrollXListener;

    public interface OnScrollXListener {
        void onLeftScroll(float leftX);

        void onRightScroll(float rightX);

        void onDown();

        void onLeftUp();

        void onRightUp();

        void onStop();
    }

    public void setOnScrollXListener(OnScrollXListener onScrollXListener) {
        this.onScrollXListener = onScrollXListener;
    }

    public MaskView(Context context) {
        super(context);
        init();
    }

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setWillNotDraw(false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_mask, this, false);
        leftImg = view.findViewById(R.id.left);
        rightImg = view.findViewById(R.id.right);

        addView(view);

        leftImg.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getRawX();
                        if (onScrollXListener != null) {
                            onScrollXListener.onDown();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x2 = event.getRawX();
                        float diffX = x2 - x;

                        if (diffX + leftImgMoveX <= 0) {
                            leftImg.setX(0);
                        } else if (diffX + leftImgMoveX >= rightImg.getX() - space) {
                            leftImg.setX(rightImg.getX() - space);
                        } else {
                            leftImg.setX(diffX + leftImgMoveX);
                        }

                        if (onScrollXListener != null) {
                            onScrollXListener.onLeftScroll(leftImg.getX());
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        leftImgMoveX = leftImg.getX();
                        if (onScrollXListener != null) {
                            onScrollXListener.onLeftUp();
                        }
                        break;
                }

                return true;
            }
        });

        rightImg.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getRawX();
                        if (onScrollXListener != null) {
                            onScrollXListener.onDown();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x2 = event.getRawX();
                        float diffX = x2 - x;

                        if (diffX + rightImgMoveX <= leftImg.getX() + space) {
                            rightImg.setX(leftImg.getX() + space);
                        } else if (diffX + rightImgMoveX >= viewWidth - rightImgWidth) {
                            rightImg.setX(viewWidth - rightImgWidth);
                        } else {
                            rightImg.setX(diffX + rightImgMoveX);
                        }

                        if (onScrollXListener != null) {
                            onScrollXListener.onRightScroll(rightImg.getX() + rightImgWidth);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        L.d("ACTION_UP");
                        rightImgMoveX = rightImg.getX();
                        if (onScrollXListener != null) {
                            onScrollXListener.onRightUp();
                        }
                        break;
                }

                return true;
            }
        });

        initPaint();
    }

    public void initPaint() {
        progressPaint = new Paint();
        progressPaint.setColor(getResources().getColor(R.color.white));
    }

    public void initView() {

    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public void setTime_length(float time_length) {
        this.time_length = time_length;
    }

    public float getMaskLeft() {
        return leftImg.getX();
    }

    public float getMaskRight() {
        return rightImg.getX() + rightImgWidth;
    }

    public void updateProgress(int x) {
        progressX1 = (float) x  / time_length ;
        progressX2 = (float) x / time_length ;
//        L.d("progressX1 = " + progressX1);
        invalidate();

        if (progressX1 >= rightImgMoveX + rightImgWidth) {
            if (onScrollXListener != null) {
                onScrollXListener.onStop();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = this.getMeasuredWidth();
        leftImgWidth = leftImg.getMeasuredWidth();
        rightImgWidth = rightImg.getMeasuredWidth();

        viewHeight = this.getMeasuredHeight();

        rightImg.setX(viewWidth - rightImgWidth);

        rightImgMoveX = rightImg.getX();

        leftImgL = 0;
        space = unit - rightImgWidth < 0 ? rightImgWidth : unit - rightImgWidth;

        L.d("viewHeight = " + viewHeight);

        progressX1 = 0;
        progressY1 = 0;
        progressX2 = 0;
        progressY2 = rightImg.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        L.d("画线");
        canvas.drawLine(progressX1, progressY1, progressX2, progressY2, progressPaint);
    }
}
