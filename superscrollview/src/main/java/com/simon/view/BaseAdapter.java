package com.simon.view;


import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 基础的adapter
 * author: Simon
 * created at 2017/9/22 下午4:50
 */
public abstract class BaseAdapter {

    //--点击事件的监听回调¬--
    private OnItemClickLisener lisener;

    public abstract int getCount(); //view的数量

    public abstract View getView(int position); //获取具体的view

    public abstract int getDefaultPosition(); //默认的选中位置

    public interface OnItemClickLisener {
        void onItemClick(View view, int position);
    }

    /**
     * 设置item点击事件
     *
     * @param lisener
     */
    public void setOnItemClickLisener(OnItemClickLisener lisener) {
        this.lisener = lisener;
    }

    /**
     *  检查是否具有lisener
     * @return
     */
    private boolean checkLisener() {
        return lisener != null;
    }

    /**
     * 获取childViews 并遍历设置点击事件
     *
     * @return
     */
    public List<View> getChildViews() {
        List<View> result = new ArrayList<>();
        if (getCount() != 0) {
            for (int i = 0; i < getCount(); i++) {
                View view = getView(i);
                result.add(view);
                view.setOnClickListener(new SuperViewClickListener(view, i));
            }
        }
        return result;
    }

    /**
     * 用于重写点击回调，用于当view点击时回调
     */
    public class SuperViewClickListener implements View.OnClickListener {

        private View view;
        private int position;

        public SuperViewClickListener(View view, int position) {
            this.view = view;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (checkLisener()) {
                lisener.onItemClick(view, position);
            }
        }
    }
}


