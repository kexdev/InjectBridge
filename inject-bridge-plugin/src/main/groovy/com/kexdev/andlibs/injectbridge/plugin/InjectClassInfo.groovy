package com.kexdev.andlibs.injectbridge.plugin

class InjectClassInfo {

    public String className

    public String target
    public int priority

    @Override
    String toString() {
        return 'InjectClassInfo [target: ' + target + ', className: ' + className + ', priority: ' + priority + ']'
    }

}
