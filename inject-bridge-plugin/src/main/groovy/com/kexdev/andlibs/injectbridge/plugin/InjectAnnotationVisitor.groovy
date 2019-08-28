package com.kexdev.andlibs.injectbridge.plugin

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes

class InjectAnnotationVisitor extends AnnotationVisitor {

    OnAnnotationValueListener valueListener

    InjectAnnotationVisitor(AnnotationVisitor av, OnAnnotationValueListener listener) {
        super(Opcodes.ASM6, av)
        valueListener = listener
    }

    @Override
    void visit(String name, Object value) {
        super.visit(name, value)
        valueListener.onValue(name, value)
    }

    @Override
    AnnotationVisitor visitArray(String name) {
        if (name == "name") {
            return new InjectAnnotationVisitor(super.visitArray(name), valueListener)
        } else {
            return super.visitArray(name)
        }
    }

}
