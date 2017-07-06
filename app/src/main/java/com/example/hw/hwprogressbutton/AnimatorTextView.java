package com.example.hw.hwprogressbutton;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * Created by HaodaHw on 2017/7/5.
 * <p>
 * 自定义控件练习：
 * 1.动态改变DashPathEffect第二个参数：偏移量，达到动态画线的效果
 * 2.触摸滑动Scroller     抬起手指后快速滚动fling
 *
 */

public class AnimatorTextView extends View {
    private Paint mPaint;
    private boolean startAnimaotr;
    private Path mPath = new Path();
    private PathMeasure mPathMeasure;//先关联已创建好的Path
    private ValueAnimator mValueAnimator;//属性动画

    private float mLastPointX, mLastPointY;
    private Scroller mScroller;
    private int mSlop;

    private int mMinVelocity;//最小滑动启动速度
    private VelocityTracker mVelocityTracker;//速度跟踪器

    public AnimatorTextView(Context context) {
        this(context, null);
    }

    public AnimatorTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMinVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();//初始化获取系统最小滑动启动速度：50

        initScroller(context);//初始化 滑动相关内容

        initPaint();
    }

    private void initScroller(Context context) {
        mScroller = new Scroller(context);
//        mSlop = ViewConfiguration.getTouchSlop(); //获取最小能够识别的滑动距离 但该方法已过时
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();//获取最小能够识别的滑动距离
        Log.d("AnimatorTextView", "可识别的滑动距离：" + mSlop);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (startAnimaotr) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initMyView();
        initAnimation();//添加动画
    }

    private void initMyView() {
        mPath.moveTo(10, 10);
        mPath.lineTo(200, 200);
        mPath.lineTo(400, 10);
        mPath.lineTo(500, 40);

        mPathMeasure = new PathMeasure(mPath, true);
    }

    private void initAnimation() {
        mValueAnimator = ValueAnimator.ofFloat(1, 0);
        mValueAnimator.setDuration(1000);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAnimaotr = true;
                float value = (float) animation.getAnimatedValue();
                PathEffect effect = new DashPathEffect(new float[]{mPathMeasure.getLength(), mPathMeasure.getLength()}, value *
                        mPathMeasure.getLength());
                mPaint.setPathEffect(effect);//设置路径效果
                invalidate();

            }
        });

        mValueAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //创建
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                restoreTouchPoint(event);
                break;

            case MotionEvent.ACTION_MOVE://追随手指滑动操作  Scroll。
                int dx = (int) (event.getX() - mLastPointX);
                int dy = (int) (event.getY() - mLastPointY);
                if (Math.abs(dx) > mSlop || Math.abs(dy) > mSlop) {
                    //取值的正负与手势的方向相反,源码分析得到
                    scrollBy(-dx, -dy);
                    restoreTouchPoint(event);
                }
                break;

            case MotionEvent.ACTION_UP://手指离开屏幕时，如果速度够大，就实现一个 fling 动作
                mVelocityTracker.computeCurrentVelocity(1000, 2000.0f);// 自定义：1s内最大滚动像素2000
                int xVelocity = (int) mVelocityTracker.getXVelocity();
                int yVelocity = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(xVelocity) > mMinVelocity || Math.abs(yVelocity) > mMinVelocity) {//当前速度的绝对值都大于阈值就触发
                    mScroller.fling(getScrollX(), getScrollY(), xVelocity, yVelocity, -1000, 1000, -1000, 2000);
                    invalidate();
                }
                break;
        }

        return true;
    }


    private void restoreTouchPoint(MotionEvent event) {
        mLastPointX = event.getX();
        mLastPointY = event.getY();
    }
}
