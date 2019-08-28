package com.kexdev.andlibs.injectbridge.plugin


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class TargetClassVisitor extends ClassVisitor {

    TargetMethodVisitor targetMethodVisitor = new TargetMethodVisitor()

    String className

    TargetClassVisitor() {
        super(Opcodes.ASM6)
    }

    void set(ClassVisitor classWriter, OnMethodInjectListener onMethodInjectListener) {
        this.cv = classWriter
        targetMethodVisitor.setMethodInjectListener(onMethodInjectListener)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        //如果是接口，不处理
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            return methodVisitor
        }

        targetMethodVisitor.set(methodVisitor, className, name, desc, access)
        return targetMethodVisitor
    }

}
