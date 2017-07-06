package com.example.hw.hwprogressbutton;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by HaodaHw on 2017/7/4.
 * <p>
 * 带动画效果的按钮
 */

public class ProgressButton extends View {

    /**
     * view的宽度
     */
    private int width;
    /**
     * view的高度
     */
    private int height;
    /**
     * 圆角矩形画笔
     */
    private Paint mPaint;
    /**
     * 文字画笔
     */
    private Paint textPaint;
    /**
     * 对勾（√）画笔
     */
    private Paint okPaint;
    /**
     * 路径--用来获取对勾的路径
     */
    private Path path = new Path();
    /**
     * 圆角矩形背景颜色
     */
    private int bg_color = 0x40ffdc;
    /**
     * 根据view的大小设置成矩形
     */
    private RectF rectf = new RectF();
    /**
     * 文字绘制所在矩形
     */
    private Rect textRect = new Rect();
    /**
     * 默认两圆圆心之间的距离=需要移动的距离
     */
    private int default_two_circle_distance;
    /**
     * 两圆圆心之间的距离
     */
    private int two_circle_distance;
    /**
     * 圆角半径
     */
    private int circleAngle;
    /**
     * 取路径的长度
     */
    private PathMeasure pathMeasure;
    /**
     * 按钮文字字符串
     */
    private String buttonString = "下载";
    /**
     * 动画集
     */
    private AnimatorSet animatorSet = new AnimatorSet();
    /**
     * 1-矩形到圆角矩形过度的动画
     */
    private ValueAnimator animator_rect_to_angle;
    /**
     * 2-圆角矩形到圆过度的动画
     */
    private ValueAnimator animator_rect_to_circle;
    /**
     * 3-view上移的动画
     */
    private ObjectAnimator animator_move_to_up;
    /**
     * 4-绘制对勾（√）的动画
     */
    private ValueAnimator animator_draw_ok;
    /**
     * 动画执行时间
     */
    private int duration = 1000;
    /**
     * view向上移动距离
     */
    private int move_distance = 300;
    /**
     * 是否开始绘制对勾
     */
    private boolean startDrawOk = false;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPaint();//初始化画笔

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickListener();//设置点击监听
                }
            }
        });

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mListener.animationFinish();//设置动画结束监听
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);//边框宽度
        mPaint.setStyle(Paint.Style.FILL);//填充模式：实心
        mPaint.setColor(Color.parseColor("#ffffff"));//将十六进制颜色代码转换为int类型数值方法

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(40);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        okPaint = new Paint();
        okPaint.setStrokeWidth(5);
        okPaint.setStyle(Paint.Style.STROKE);//填充模式：描边
        okPaint.setAntiAlias(true);
        okPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        //圆角矩形过度到圆的动画时移动的距离
        default_two_circle_distance = (w - h) / 2;

        //设置对勾图路径坐标
        initOk();

        //初始化所有动画
        initAnimation();
    }

    private void initOk() {
        path.moveTo((float) (default_two_circle_distance * 1.3), height / 2);//先移动到预设的点的位置
        path.lineTo(default_two_circle_distance + height / 2, height * 3 / 4);//再画线
        path.lineTo((float) (width / 2 + height * 0.3), 0);

        pathMeasure = new PathMeasure(path, true);//关联已创建好的Path
    }

    private void initAnimation() {
        set_rect_to_angle_animation();
        set_rect_to_circle_animation();
        set_move_to_up_animation();
        set_draw_ok_animation();

        //借助AnimatorSet类完成组合动画，并开启
        animatorSet.play(animator_move_to_up).
                before(animator_draw_ok).
                after(animator_rect_to_angle).
                after(animator_rect_to_circle);

//        animatorSet.start();  该方法提供给调用者
    }

    //4-设置绘制对勾（√）的动画
    private void set_draw_ok_animation() {
        animator_draw_ok = ValueAnimator.ofFloat(1, 0);
        animator_draw_ok.setDuration(duration);
        animator_draw_ok.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startDrawOk = true;
                float value = (float) animation.getAnimatedValue();
                //利用属性动画来改变偏移量，然后根据phase动态的构造一个这样的DashPathEffect，会让路径产生对勾动画的效果
                PathEffect effect = new DashPathEffect(new float[]{pathMeasure.getLength(), pathMeasure.getLength()}, value * pathMeasure.getLength());
                okPaint.setPathEffect(effect);//给画笔对象设置绘制路径时的特效
                invalidate();
            }
        });

    }

    //3-设置view上移的动画   ObjectAnimator:可以直接对任意对象的任意属性进行动画操作
    private void set_move_to_up_animation() {
        float currentTranslationY = this.getTranslationY();
        //向上平滑移动
        animator_move_to_up = ObjectAnimator.ofFloat(this, "translationY", currentTranslationY, currentTranslationY - move_distance);
        animator_move_to_up.setDuration(duration);
        animator_move_to_up.setInterpolator(new AccelerateDecelerateInterpolator());//系统默认也是一个先加速后减速的Interpolator

    }

    // 2-设置圆角矩形到圆过度的动画
    private void set_rect_to_circle_animation() {
        animator_rect_to_circle = ValueAnimator.ofInt(0, default_two_circle_distance);
        animator_rect_to_circle.setDuration(duration);
        animator_rect_to_circle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                two_circle_distance = (int) animation.getAnimatedValue();//该变化值用于变化成圆 变化域就是方法ofInt里的参数

                int alpha = 255 - (two_circle_distance * 255) / default_two_circle_distance;//在靠拢的过程中设置文字的透明度,使文字逐渐消失的效果
                textPaint.setAlpha(alpha);//设置透明度 0~255

                invalidate();//重绘
            }
        });

//        animator_rect_to_circle.start();
    }

    // 1-设置矩形过度到圆角矩形的动画
    private void set_rect_to_angle_animation() {
        animator_rect_to_angle = ValueAnimator.ofInt(0, height / 2);//传入任意多个参数的,因此我们还可以构建出更加复杂的动画逻辑
        animator_rect_to_angle.setDuration(duration);
        animator_rect_to_angle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleAngle = (int) animation.getAnimatedValue();//该变化值用于圆角矩形
                invalidate();
            }
        });

//        animator_rect_to_angle.start();//开启动画
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        draw_roundRect(canvas);//先绘制圆角矩形
        drawText(canvas);//绘制文本

        if (startDrawOk) {
            canvas.drawPath(path, okPaint);//最后一步设置动态绘制文字
        }

    }

    private void drawText(Canvas canvas) {
        textRect.left = 0;
        textRect.top = 0;
        textRect.right = width;
        textRect.bottom = height;
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        int baseline = (textRect.bottom + textRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        //文字绘制到整个布局的中心位置
        canvas.drawText(buttonString, textRect.centerX(), baseline, textPaint);
    }

    //先绘制圆角矩形，当发生过度到圆角矩形的动画时，circleAngle参数的值发生时时变化，图也就变了
    private void draw_roundRect(Canvas canvas) {
        rectf.left = two_circle_distance;
        rectf.top = 0;
        rectf.right = width - two_circle_distance;
        rectf.bottom = height;

        canvas.drawRoundRect(rectf, circleAngle, circleAngle, mPaint);

    }

    /**
     * 向调用者提供：启动动画方法
     */
    public void start() {
        animatorSet.start();
    }


    //最后给控件定义接口回调
    public interface ProgressButtonListener {
        /**
         * 按钮点击事件
         */
        void onClickListener();

        /**
         * 动画完成回调
         */
        void animationFinish();
    }

    private ProgressButtonListener mListener;

    public void setProgressButtonListener(ProgressButtonListener progressButtonListener) {
        mListener = progressButtonListener;
    }
}
