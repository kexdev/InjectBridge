package com.kexdev.andlibs.injectbridge.demo;

import com.kexdev.andlibs.injectbridge.core.InjectConfig;
import com.kexdev.andlibs.injectbridge.core.InjectTarget;

/**
 * @author zixuan
 * Created by zixuan on 2019/8/28
 */
public class SimpleInjectTarget {

    @InjectTarget
    public Object injectCount(Object[] params) {
        return 0;
    }

    //@InjectTarget(model = InjectConfig.MODEL_REPLACE)
    //@InjectTarget(model = InjectConfig.MODEL_BEFORE)
    @InjectTarget(model = InjectConfig.MODEL_AFTER)
    public Object injectName(Object[] params) {
        return "default";
    }

}
