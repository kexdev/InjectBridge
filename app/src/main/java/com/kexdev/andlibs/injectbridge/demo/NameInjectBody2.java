package com.kexdev.andlibs.injectbridge.demo;

import android.util.Log;

import com.kexdev.andlibs.injectbridge.core.BaseInjectBody;
import com.kexdev.andlibs.injectbridge.core.InjectBody;

/**
 * @author zixuan
 * Created by zixuan on 2019/8/28
 */
@InjectBody(target = "injectName")
public class NameInjectBody2 implements BaseInjectBody {
    @Override
    public Object execute(Object[] params) {
        String name = null;
        int age = 0;
        String result = null;

        try {
            if (params != null && params.length > 0) {
                name = (String) params[0];
                age = (int) params[1];
                result = "name2 = " + name + ", age2 = " + age;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("test", "NameInjectBody2 result: " + result);
        return result;
    }
}
