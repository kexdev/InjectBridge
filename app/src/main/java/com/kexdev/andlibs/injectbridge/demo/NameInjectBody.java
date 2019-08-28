package com.kexdev.andlibs.injectbridge.demo;

import android.util.Log;

import com.kexdev.andlibs.injectbridge.core.BaseInjectBody;
import com.kexdev.andlibs.injectbridge.core.InjectBody;

/**
 * @author zixuan
 * Created by zixuan on 2019/8/28
 */
@InjectBody(target = "injectName")
public class NameInjectBody implements BaseInjectBody {
    @Override
    public Object execute(Object[] params) {
        String name = null;
        int age = 0;
        String result = null;

        try {
            if (params != null && params.length > 0) {
                name = (String) params[0];
                age = (int) params[1];
                result = "name = " + name + ", age = " + age;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("test", "InjectBridge - NameInjectBody");
        Log.i("test", "result=" + result);
        return result;
    }
}
