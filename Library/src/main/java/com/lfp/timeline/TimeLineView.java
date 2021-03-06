package com.lfp.timeline;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * <pre>
 * desc:
 *       View中包含一条时间线,通过观察时间线上时间的流逝可以做一些事情
 *
 *
 * function:
 *      addTimeEventInDrawBefore()  :添加事件 在绘制之前
 *      addTimeEventInDrawAfter()   :添加事件 在绘制之后
 *      deleteTimeEvent()           :删除事件
 *      deleteTimeEvents()          :删除所有事件
 *
 *
 * Created by LiFuPing on 2018/8/22.
 * </pre>
 */
public abstract class TimeLineView extends View {


    /*时间线 - 绘制之前分发*/
    AnimationTimeLine mTimeLine = new AnimationTimeLine();
    /*动画时间线 - onDraw 方法执行之后才分发*/
    AnimationTimeLine mAnimationTimeLine = new AnimationTimeLine();

    public TimeLineView(Context context) {
        super(context);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        //如果View不可见或者时间线上没有观察者的时候停止之后的动作
        if (getVisibility() == View.VISIBLE && mTimeLine.getTimeObserverCount() > 0) {
            mTimeLine.setContext(this, canvas);
            mTimeLine.elapse(getDrawingTime());
        }
        super.draw(canvas);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (getVisibility() == View.VISIBLE && mAnimationTimeLine.getTimeObserverCount() > 0) {
            mAnimationTimeLine.setContext(this, canvas);
            mAnimationTimeLine.elapse(getDrawingTime());
        }


        //如果时间线上没有附着物的时候 检查是否需要持续接收信号
        if (getVisibility() == View.VISIBLE && (mTimeLine.getTimeObserverCount() > 0 || mAnimationTimeLine.getTimeObserverCount() > 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    /**
     * 添加事件 - 在绘制之前
     *
     * @param event 事件
     */
    public void addTimeEventInDrawBefore(TimeLineObserver event) {
        mTimeLine.addTimeObserver(event);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 添加事件 - 在绘制之后
     *
     * @param event 事件
     */
    public void addTimeEventInDrawAfter(TimeLineObserver event) {
        mAnimationTimeLine.addTimeObserver(event);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 删除事件
     *
     * @param event 事件
     */
    public void deleteTimeEvent(TimeLineObserver event) {
        mTimeLine.deleteTimeObserver(event);
        mAnimationTimeLine.deleteTimeObserver(event);
    }

    /**
     * 删除所有事件
     */
    public void deleteTimeEvents() {
        mTimeLine.deleteTimeObservers();
        mAnimationTimeLine.deleteTimeObservers();
    }


    /*  自定义动画时间线,提供Canvas回调 */
    private static final class AnimationTimeLine extends TimeLine {
        Canvas canvas;
        View mView;

        public void setContext(View v, Canvas canvas) {
            this.mView = v;
            this.canvas = canvas;
        }

        public Canvas getCanvas() {
            return canvas;
        }

        public View getView() {
            return mView;
        }
    }

    /**
     * <pre>
     *  动画效果绘制事件 , 在一段时间内绘制一段动画
     *
     * </pre>
     */
    public abstract static class AnimationDrawEvent extends TimeValueEvent {

        @Override
        public void onElapse(long actualtime, long runtime, long duration, float progress) {
            super.onElapse(actualtime, runtime, duration, progress);
            TimeLine timeLine = getTimeLine();
            if (timeLine instanceof AnimationTimeLine) {
                AnimationTimeLine timeline = (AnimationTimeLine) timeLine;
                onElapse(getValue(), timeline.getView(), timeline.getCanvas());
            }
        }

        /**
         * 时间流逝
         *
         * @param value  the value
         * @param view   the view
         * @param canvas the canvas
         */
        public abstract void onElapse(float value, View view, Canvas canvas);
    }

}
