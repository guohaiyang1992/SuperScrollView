package com.simon.horscrollview;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

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
        return view;
    }

    @Override
    public int getDefaultPosition() {
        return 0; //默认显示的位置
    }
}
