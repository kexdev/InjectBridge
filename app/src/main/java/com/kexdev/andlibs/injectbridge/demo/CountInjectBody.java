package com.kexdev.andlibs.injectbridge.demo;

import android.util.Log;

import com.kexdev.andlibs.injectbridge.core.BaseInjectBody;
import com.kexdev.andlibs.injectbridge.core.InjectBody;

/**
 * @author zixuan
 * Created by zixuan on 2019/8/28
 */
@InjectBody(target = "injectCount")
public class CountInjectBody implements BaseInjectBody {
    @Override
    public Object execute(Object[] params) {
        int count = 0;

        try {
            if (params != null) {
                for (Object param : params) {
                    count =  count + (int) param;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("test", "CountInjectBody count: " + count);
        return count;
    }
}
