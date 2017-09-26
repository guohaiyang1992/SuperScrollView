package com.simon.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.BuildConfig;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 超级滑动类
 * author: Simon
 * created at 2017/9/22 下午2:45
 * <p>
 * 用于实现效果： 多个view 可以添加到内部，可以设置滚动到的position
 * 滑动的时候也可以切换不同的item 对外提供接口，可以改变显示的大小宽高亮度，从而有动画效果还有滑动后的回弹效果当不足切换时回弹，足够时自动补足
 * <p>
 * 另一种实现方式，使用recycleview 的方式自定义 manager 自定义过度动画  功能和上方的一样
 * <p>
 * item 可以点击，可以自定义显示效果
 * <p>
 * 当前效果：
 * 1.支持横向滑动
 * 2.支持中间缩小放大
 * 3.支持滑动回弹，默认回弹到当前的中间view，如果没有，选择临近中间位置的view
 * <p>
 * todo:
 * 1.开放滑动时动画的回调接口，由外部指定
 * 2.加入view加载缓存，而不是一次加载全部
 * 3.增加其他view 的动画
 */

public class SuperScrollView extends ViewGroup {
    //--TAG--
    private static final String TAG = "SuperScrollView";
    //--处理滑动的辅助工具--
    private ViewDragHelper mDragger;

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

    //--是否滑动过default--
    private boolean isScrollToDefault = false;

    //-------------------构造函数------------------------
    public SuperScrollView(Context context) {
        this(context, null);
    }

    public SuperScrollView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initConfig();
        initChildView();
    }

    /**
     * 初始化子视图
     */
    private void initChildView() {
        if (adapter == null) {
            return;
        }
        count = adapter.getCount();
        defaultPostion = adapter.getDefaultPosition();
        childList = getAndAddChildList();
    }

    /**
     * 初始化基础数据
     */
    private void initConfig() {
        //--创建辅助助手--
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override //--用于获取可以捕捉的view--
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override //--用于设置横向边界（只计算最左侧边界）--
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override //--设置垂直边界--
            public int clampViewPositionVertical(View child, int top, int dy) {
                return child.getTop();
            }

            //--当设置了click监听必须写--
            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

//            @Override
//            public int getViewVerticalDragRange(View child) {
//                return getMeasuredHeight() - child.getMeasuredHeight();
//            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                dispatchOtherViewPositionChanged(changedView, dx);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                //--判读当前中的view是否存在（如果不存在看上一次的midView在当前的什么位置，如果上次的就是最左侧或者最右侧特殊处理，反之 根据位置方向 计算上一个或者下一个）--
                dispatchViewReleased();

            }
        });

    }

    /**
     * 分发手指松开时的动作
     */
    private void dispatchViewReleased() {
        View midView = getMidView();
        if (midView == null) {
            midView = getNearbyView();
        }
        //不是null的时候使其居中
        mDragger.smoothSlideViewTo(midView, midPostionX - midView.getWidth() / 2, midView.getTop());
        invalidate();
    }


    /**
     * 当 中间view 没有时 获取临近的view
     *
     * @return view 临近view
     */
    private View getNearbyView() {
        View nearbyView = null;
        int distance = Integer.MAX_VALUE;
        for (View view : childList) {
            int leftD = Math.abs(view.getLeft() - midPostionX);
            int rightD = Math.abs(view.getRight() - midPostionX);
            int min = Math.min(leftD, rightD);
            if (min < distance) {
                distance = min;
                nearbyView = view;
            }
        }
        return nearbyView;
    }

    /**
     * 用于移动除了当前的view以外的其他view
     *
     * @param changedView 当前变化的view
     * @param dx          x方向变化数据
     */
    private void dispatchOtherViewPositionChanged(View changedView, int dx) {
        //--动画处理--
        View midView = getMidView();
        //--小优化 用于标志是否设置过中，防止重复设置--
        boolean isSetMid = false;
        //--percent--
        float percent = 1f + (0.4f * (1 - getMidViewPercent()));


        for (View view : childList) {

            //--如果是null,设置下一个--
            if (view == null) {
                continue;
            }

            //--移动处理--
            if (!view.equals(changedView)) {
                view.offsetLeftAndRight(dx);
            }

            //--针对不同view设置不同值--
            if (!isSetMid && view.equals(midView)) {
                midView.setScaleX(percent);
                midView.setScaleY(percent);
                midView.setAlpha(percent);
                isSetMid = true;
                print("当前缩放比例为： " + percent);
            } else { //用于恢复其他view的状态
                view.setScaleX(1f);
                view.setScaleY(1f);
                //--other view 设置透明度为0.5f--
                view.setAlpha(0.5f);
            }

        }


    }


    /**
     * 获取中间view的缩放比例
     *
     * @return 返回缩放比例 （0.0-1.0）
     */
    private float getMidViewPercent() {
        View midView = getMidView();
        if (midView == null) {
            return 1;
        } else {
            int viewMid = midView.getLeft() + midView.getWidth() / 2;
            return Math.abs(viewMid - midPostionX) / (midView.getWidth() / 2.0f);
        }
    }

    /**
     * 获取中间的view
     *
     * @return
     */
    private View getMidView() {
        for (View view : childList) {
            if (view.getLeft() < midPostionX && view.getRight() > midPostionX) {
                return view;
            }
        }
        return null;
    }


    //--viewdraghelper 需要重写此处--
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragger.shouldInterceptTouchEvent(ev);
    }

    /**
     * 重写此方法（自定义viewgroup 必须重写） 目前布局限定为横向布局，且可以无限横向
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
        print("-----onLayout----");
        //--此处滚动到默认位置--
        scrollToDefault();
    }

    //--viewdraghelper 需要重写此处--
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
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
        //--遍历添加--
        for (int i = 0; i < result.size(); i++) {
            addView(result.get(i));
        }
        return result;
    }


    /**
     * 平滑移动需要
     */
    @Override
    public void computeScroll() {
        //--平滑移动--
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    /**
     * 用于设置适配器
     *
     * @param adapter BaseAdpater
     */
    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
        initChildView();
    }

    /**
     * 滚动子view到某个位置
     *
     * @param position
     */
    public void scrollToChildPosition(int position) {
        //--check position--
        if (position <= count - 1 && mDragger != null) {
            View midView = childList.get(position);
            mDragger.smoothSlideViewTo(midView, midPostionX - midView.getWidth() / 2, midView.getTop());
            invalidate();//注意此处，必须写，否则无效果
        }
    }

    /**
     * 用于获取margin参数
     *
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 测量自身的宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
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


    /**
     * 仅仅在debug模式下打印log
     *
     * @param info
     */
    private void print(String info) {
        Log.v(TAG, info);
    }

    /**
     * 滑动到默认位置
     */
    private void scrollToDefault() {
        midPostionX = getWidth() / 2;
        //--如果之前滑动过默认位置则此处不再滑动(增加判断是否等于0 有的时候即使layout 的时候仍然为0比如在视图隐藏的时候)--
        if (isScrollToDefault && midPostionX == 0) {
            return;
        }
        //--之前没滑动过--
        scrollToChildPosition(defaultPostion);
        //--重置状态防止再次滑动到默认--
        isScrollToDefault = true;
        print("滑动到默认位置");
    }

}
