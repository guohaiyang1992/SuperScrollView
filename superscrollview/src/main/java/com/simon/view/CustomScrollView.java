package com.simon.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 通过自己去处理触摸事件，解决触摸冲突(之前采用的重写设置位置的方式，现在采用的是scroll 方式，细节不太一样(区别是什么))
 * author: Simon
 * created at 2017/9/25 上午9:07
 */

public class CustomScrollView extends ViewGroup implements ViewTreeObserver.OnGlobalLayoutListener {

    //---------------------基础配置----------------------
    //--子view的数量--
    private int count;

    //--子view的缓存--
    private List<View> childList;

    //--中间点的坐标--
    private int midPostionX = 0;

    //--适配器--
    private BaseAdapter adapter;

    //--默认选中点--
    private int defaultPostion = 0;

    //-------------------滑动相关配置------------------------

    //--滑动工具类--
    private Scroller mScroller;

    //--最小滑动距离--
    private int mTouchSlop;

    //------------------触控相关-------------------------
    //--触控的相对位置--
    private int downX, downY;

    //--触控的绝对位置，相对屏幕--
    private float mXDown, mXMove, mXLastMove;

    //--触摸点id--
    private int pointId;

    //--是否滑动到默认值--
    private boolean isScrollToDefault;


    public CustomScrollView(Context context) {
        this(context, null);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initConfig();
    }

    private void initConfig() {
        //--创建scroller用于实现滑动--
        mScroller = new Scroller(getContext());
        //--获取config 用于获取最大滑动速度和最小滑动距离--
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    //------------------自定义 view group 重写方法-------------------------

    /**
     * 重写viewgroup 必须重写此处，用于对子view 布局
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cCount = getChildCount();
        int cWidth = 0;
        int cHeight = 0;
        LayoutParams cParams = null;
        MarginLayoutParams marginLayoutParams = null;

        int height = getHeight();
        int right = 0;
        //--遍历child layout--
        for (int i = 0; i < cCount; i++) {
            View childView = getChildAt(i);
            cWidth = childView.getMeasuredWidth();
            cHeight = childView.getMeasuredHeight();
            cParams = childView.getLayoutParams();
            int leftMargin, rightMargin, topMargin, bottomMargin;
            if (cParams == null || !(cParams instanceof MarginLayoutParams)) {
                leftMargin = rightMargin = topMargin = bottomMargin = 0;
            } else {
                marginLayoutParams = (MarginLayoutParams) cParams;
                leftMargin = marginLayoutParams.leftMargin;
                rightMargin = marginLayoutParams.rightMargin;
                topMargin = marginLayoutParams.topMargin;
                bottomMargin = marginLayoutParams.bottomMargin;
            }
            //--默认居中对齐--
            int cl = 0, ct = 0, cr = 0, cb = 0;
            cl = right + leftMargin;
            ct = (height - topMargin - bottomMargin) / 2 - cHeight / 2;
            cr = cl + cWidth;
            cb = ct + cHeight;
            right = cr + rightMargin;
            childView.layout(cl, ct, cr, cb);
        }
    }

    /**
     * 重写viewgroup 必须重写
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        /**
         * 记录如果是wrap_content是设置的宽和高
         */
        int width = 0;
        int height = 0;

        int count = getChildCount();

        int cWidth = 0;
        int cHeight = 0;
        LayoutParams cParams = null;
        MarginLayoutParams marginLayoutParams = null;

        for (int i = 0; i < count; i++) {
            View childView = getChildAt(i);
            cWidth = childView.getMeasuredWidth();
            cHeight = childView.getMeasuredHeight();
            cParams = childView.getLayoutParams();
            int leftMargin, rightMargin, topMargin, bottomMargin;
            if (cParams == null || !((cParams instanceof MarginLayoutParams))) {
                leftMargin = rightMargin = topMargin = bottomMargin = 0;
            } else {
                marginLayoutParams = (MarginLayoutParams) cParams;
                leftMargin = marginLayoutParams.leftMargin;
                rightMargin = marginLayoutParams.rightMargin;
                topMargin = marginLayoutParams.topMargin;
                bottomMargin = marginLayoutParams.bottomMargin;
            }
            width += cWidth + leftMargin + rightMargin;//叠加子view的宽度
            height = Math.max(height, cHeight + topMargin + bottomMargin);//获取最大高度
        }
        /**
         * 如果是wrap_content设置为我们计算的值
         * 否则：直接设置为父容器计算的值
         */
        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth
                : width, (heightMode == MeasureSpec.EXACTLY) ? sizeHeight
                : height);
    }
    //------------------重写 方法 end-------------------------


    //------------------触摸事件-------------------------


    /**
     * 用于控制父布局对此viewgroup的事件处理（用于外部拦截）
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                requestParentDisallowInterceptTouchEvent(true);
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //--横向滑动的时候，请求不拦截--
                if (Math.abs(ev.getX() - downX) > Math.abs(ev.getY() - downY)) {
                    requestParentDisallowInterceptTouchEvent(true);
                } else {
                    requestParentDisallowInterceptTouchEvent(false);
                }

                break;
        }
        return super.dispatchTouchEvent(ev);//--**此处用于将事件分发,不写此处则无法继续后续操作**--
    }

    /**
     * 请求父布局对当前viewgroup的时间是否拦截处理 true 表示不可以拦截  false 表示可以拦截
     *
     * @param disallow
     */
    private void requestParentDisallowInterceptTouchEvent(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    /**
     * 用于父布局决定是否将事件传递下去（用于内部拦截）
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointId = ev.getPointerId(0);
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件(判定为横向滑动的时候拦截事件)
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev); //--其他情况按照原有的触发--
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
        initChildView();
    }

    private void initChildView() {
        if (adapter == null) {
            return;
        }
        count = adapter.getCount();
        defaultPostion = adapter.getDefaultPosition();
        childList = getAndAddChildList();
    }

    /**
     * 获取childViews
     *
     * @return 获取所有的子view 返回list形式
     */
    private List<View> getAndAddChildList() {
        List<View> result = new ArrayList<>();
        //--获取子view并设置点击事件--
        if (adapter != null && adapter.getCount() != 0) {
            result = adapter.getChildViews();
        }
        //--删除之前的子view--
        removeAllViews();
        //--遍历添加新的view--
        for (int i = 0; i < result.size(); i++) {
            addView(result.get(i));
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 此处回调的时候，getWidth 和getMearsureWidth 一定有值
     */
    @Override
    public void onGlobalLayout() {
        midPostionX = getWidth() / 2;
        scrollToDefault();
    }

    private void scrollToDefault() {
        //--如果之前滑动过默认位置则此处不再滑动(增加判断是否等于0 有的时候即使layout 的时候仍然为0比如在视图隐藏的时候)--

        if (isScrollToDefault && midPostionX == 0) {
            return;
        }
        //--之前没滑动过--
//        scrollToChildPosition(defaultPostion);
        //--重置状态防止再次滑动到默认--
        isScrollToDefault = true;
//        print("滑动到默认位置");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getRawX();
                int scrolledX = (int) (mXLastMove - mXMove);//注意取的是负值，因为是整个布局在动，而不是控件在动
                scrollBy(scrolledX, 0);//手指move时，布局跟着滚动
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
//                必须调用invalidate()重绘
                invalidate();

                break;

            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return super.onTouchEvent(event);
    }


}
