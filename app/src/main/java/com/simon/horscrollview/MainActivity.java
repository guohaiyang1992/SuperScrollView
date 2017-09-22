package com.simon.horscrollview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.simon.view.BaseAdapter;
import com.simon.view.SuperScrollView;

public class MainActivity extends AppCompatActivity {
    SuperScrollView superScrollView;
    SuperAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        superScrollView = (SuperScrollView) findViewById(R.id.super_view);
        adapter = new SuperAdapter(this);
        superScrollView.setAdapter(adapter);
        adapter.setOnItemClickLisener(new BaseAdapter.OnItemClickLisener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, view.toString() + ":" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
