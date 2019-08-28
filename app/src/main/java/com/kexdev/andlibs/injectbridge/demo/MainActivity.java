package com.kexdev.andlibs.injectbridge.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        testInjectBridge();
    }

    private void testInjectBridge() {
        SimpleInjectTarget injectTarget = new SimpleInjectTarget();

        try {
            Object[] countParam = {1, 2, 3};
            int count = (int) injectTarget.injectCount(countParam);
            Log.i("test", "injectCount: " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String name = "zixuan";
            int age = 20;
            Object[] nameParam = {name, age};
            String result = (String) injectTarget.injectName(nameParam);
            Log.i("test", "injectName: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
