package com.simon.horscrollview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.simon.view.BaseAdapter;
import com.simon.view.CustomScrollView;
import com.simon.view.SuperScrollView;

/**
 * description:  custom 测试类
 * author: Simon
 * created at 2017/9/25 下午1:19
 */

public class CustomActivity extends Activity {
    private CustomScrollView customScrollView;
    private BaseAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        customScrollView = (CustomScrollView) findViewById(R.id.custom_view);
        adapter = new SuperAdapter(this);
        customScrollView.setAdapter(adapter);
        adapter.setOnItemClickLisener(new BaseAdapter.OnItemClickLisener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(CustomActivity.this, view.toString() + ":" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
