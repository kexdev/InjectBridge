package com.kexdev.andlibs.injectbridge.plugin

class InjectClassInfoComparator implements Comparator<InjectClassInfo> {

    @Override
    int compare(InjectClassInfo o1, InjectClassInfo o2) {
        if (o1.priority > o2.priority) {
            return -1
        } else if (o1.priority < o2.priority) {
            return 1
        } else {
            return 0
        }
    }

}