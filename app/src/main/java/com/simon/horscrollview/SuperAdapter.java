package com.simon.horscrollview;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.simon.view.BaseAdapter;

/**
 * description: 测试adapter
 * author: Simon
 * created at 2017/9/22 下午4:51
 */
public class SuperAdapter extends BaseAdapter {
    private Context context;

    public SuperAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public View getView(int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_test, null);
        changeViewBg(view, position);//--此处代码只是为了演示，并无其他作用，不需要的时候可以去掉，不会影响使用--
        return view;
    }

    @Override
    public int getDefaultPosition() {
        return 0; //默认显示的位置
    }


    /**
     * 用于演示时设置不同的背景--【只是用于演示，无实际用途】
     *
     * @param view
     * @param position
     */
    private void changeViewBg(View view, int position) {
        int index = position % 4;
        View peopleTv=  view.findViewById(R.id.people_bg);
        switch (index) {
            case 0:
                peopleTv.setBackgroundResource(R.drawable.people);
                break;
            case 1:
                peopleTv.setBackgroundResource(R.drawable.people1);
                break;
            case 2:
                peopleTv.setBackgroundResource(R.drawable.people2);
                break;
            case 3:
                peopleTv.setBackgroundResource(R.drawable.people3);
                break;
        }
    }
}
