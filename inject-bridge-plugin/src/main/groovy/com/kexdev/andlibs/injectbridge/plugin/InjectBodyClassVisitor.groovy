package com.kexdev.andlibs.injectbridge.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class InjectBodyClassVisitor extends ClassVisitor {

    private final static String INJECT_BODY_ANNOTATION_BYTECODE = "Lcom/kexdev/andlibs/injectbridge/core/InjectBody;"

    private String classname
    private InjectClassInfo injectClassInfo

    InjectBodyClassVisitor() {
        super(Opcodes.ASM6)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        classname = name
        injectClassInfo = null
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (INJECT_BODY_ANNOTATION_BYTECODE == desc) {
            OnAnnotationValueListener valueListener = new OnAnnotationValueListener() {
                @Override
                void onValue(String name, Object value) {
                    if (injectClassInfo == null) {
                        injectClassInfo = new InjectClassInfo()
                    }

                    switch (name) {
                        case InjectConfig.ANNOTATION_TARGET:
                            injectClassInfo.target = value
                            break
                        case InjectConfig.ANNOTATION_PRIORITY:
                            injectClassInfo.priority = value
                            break
                    }
                }
            }

            AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible)
            return new InjectAnnotationVisitor(annotationVisitor, valueListener)
        }

        return super.visitAnnotation(desc, visible)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        if (injectClassInfo != null && injectClassInfo.target != null) {
            injectClassInfo.className = classname
            InjectManager.addInjectClassInfo(injectClassInfo)
        }
    }
}
