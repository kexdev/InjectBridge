package com.kexdev.andlibs.injectbridge.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class TargetMethodVisitor extends MethodVisitor {

    private final static String INJECT_TARGET_ANNOTATION_BYTECODE = "Lcom/kexdev/andlibs/injectbridge/core/InjectTarget;"
    private final static String INJECT_TARGET_DESC_BYTECODE = "([Ljava/lang/Object;)Ljava/lang/Object;"

    String className
    String methodName
    String methodDesc
    int methodAccess

    String targetName
    String targetModel

    private List<InjectClassInfo> classInfoList
    private boolean needInject
    private int currentLocals = 1

    private OnMethodInjectListener onInjectListener

    TargetMethodVisitor() {
        super(Opcodes.ASM6)
    }

    void set(MethodVisitor methodVisitor, String className, String methodName, String desc, int access) {
        this.mv = methodVisitor
        this.className = className
        this.methodName = methodName
        this.methodDesc = desc
        this.methodAccess = access
    }

    void setMethodInjectListener(OnMethodInjectListener listener) {
        this.onInjectListener = listener
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (INJECT_TARGET_ANNOTATION_BYTECODE == desc) {
            needInject = true
            targetName = methodName
            targetModel = InjectConfig.MODEL_REPLACE

            OnAnnotationValueListener valueListener = new OnAnnotationValueListener() {
                @Override
                void onValue(String name, Object value) {
                    switch (name) {
                        case InjectConfig.ANNOTATION_TARGET_NAME:
                            targetName = value
                            break
                        case InjectConfig.ANNOTATION_TARGET_MODEL:
                            targetModel = value
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
    void visitCode() {
        if (mv == null) {
            return
        }

        if (!needInject) {
            mv.visitCode()
            return
        }

        classInfoList = new ArrayList<>()
        classInfoList.addAll(InjectManager.getInjectClassInfoList(targetName))

        if (classInfoList.size() == 0) {
            needInject = false
            mv.visitCode()
            return
        }

        onInjectListener.onInject()
        Logger.i("[InjectBridge] start inject [inject class: " + className + ", method: " + methodName + ", desc: " + methodDesc
                + ", targetName: " + targetName + ", targetModel: " + targetModel + "]")

        switch (targetModel) {
            case InjectConfig.MODEL_REPLACE: //替换
                injectReplace(classInfoList)
                break
            case InjectConfig.MODEL_BEFORE: //加在前面
                //加在前面
                injectBefore(classInfoList)
                mv.visitCode()
                break
            case InjectConfig.MODEL_AFTER: //加在后面
                mv.visitCode()
                break
            default:
                needInject = false
                mv.visitCode()
        }
        //super.visitCode()
    }

    @Override
    void visitInsn(int opcode) {
        if (InjectConfig.MODEL_AFTER == targetModel && mv != null) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                //加在后面
                if (classInfoList != null && classInfoList.size() > 0) {
                    injectAfter(classInfoList)
                    return
                }
            }
        }
        super.visitInsn(opcode)
    }

    @Override
    void visitMaxs(int maxStack, int maxLocals) {
        if (needInject) {
            if (maxStack < 2) {
                maxStack = 2
            }

            if (maxLocals < currentLocals) {
                maxLocals = currentLocals
            }
        }
        super.visitMaxs(maxStack, maxLocals)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
        targetName = null
        targetModel = null
        classInfoList = null
        needInject = false
        currentLocals = 1
    }

    private static int getTypesSize(Type[] types) {
        int size = 0
        for (int i = 0; i < types.size(); i++) {
            size += types[i].getSize()
        }

        return size
    }

    private boolean isStaticMethod() {
        //如果是静态方法
        return ((methodAccess & Opcodes.ACC_STATIC) != 0)
    }

    private void injectBefore(List<InjectClassInfo> classInfoList) {
        int offset = getTypesSize(Type.getArgumentTypes(methodDesc))
        for (int i = 0; i < classInfoList.size(); i++) {
            InjectClassInfo classInfo = classInfoList.get(i)
            currentLocals += 1
            Logger.i("[InjectBridge] injectBefore " + classInfo.toString())

            //开始注入
            mv.visitTypeInsn(Opcodes.NEW, classInfo.className)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, classInfo.className, '<init>', '()V', false)
            mv.visitVarInsn(Opcodes.ASTORE, offset + i + 1)

            mv.visitVarInsn(Opcodes.ALOAD, offset + i + 1)
            mv.visitVarInsn(Opcodes.ALOAD, offset + i)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classInfo.className, 'execute', INJECT_TARGET_DESC_BYTECODE, false)

            //只做pop不做return，因为before之后还要调用原逻辑，原逻辑还执行返回方法
            mv.visitInsn(Opcodes.POP)
        }
    }

    private void injectAfter(List<InjectClassInfo> classInfoList) {
        String returnType = Type.getReturnType(methodDesc)
        int offset = getTypesSize(Type.getArgumentTypes(methodDesc))
        for (int i = 0; i < classInfoList.size(); i++) {
            InjectClassInfo classInfo = classInfoList.get(i)
            currentLocals += 1
            Logger.i("[InjectBridge] injectAfter " + classInfo.toString())

            //开始注入
            mv.visitTypeInsn(Opcodes.NEW, classInfo.className)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, classInfo.className, '<init>', '()V', false)
            mv.visitVarInsn(Opcodes.ASTORE, offset + i + 1)

            mv.visitVarInsn(Opcodes.ALOAD, offset + i + 1)
            mv.visitVarInsn(Opcodes.ALOAD, offset + i)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classInfo.className, 'execute', INJECT_TARGET_DESC_BYTECODE, false)

            //只做pop不做return，因为是在处理返回时处理的，最终还执行了返回方法
            if (i < classInfoList.size() - 1) {
                mv.visitInsn(Opcodes.POP)
            } else {
                if (returnType == "V") {
                    mv.visitInsn(Opcodes.RETURN)
                } else {
                    mv.visitInsn(Opcodes.ARETURN)
                }
            }
        }
    }

    private void injectReplace(List<InjectClassInfo> classInfoList) {
        String returnType = Type.getReturnType(methodDesc)
        int offset = getTypesSize(Type.getArgumentTypes(methodDesc))
        for (int i = 0; i < classInfoList.size(); i++) {
            InjectClassInfo classInfo = classInfoList.get(i)
            currentLocals += 1
            Logger.i("[InjectBridge] injectReplace " + classInfo.toString())

            //开始注入
            mv.visitTypeInsn(Opcodes.NEW, classInfo.className)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, classInfo.className, '<init>', '()V', false)
            mv.visitVarInsn(Opcodes.ASTORE, offset + i + 1)

            mv.visitVarInsn(Opcodes.ALOAD, offset + i + 1)
            mv.visitVarInsn(Opcodes.ALOAD, offset + i)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classInfo.className, 'execute', INJECT_TARGET_DESC_BYTECODE, false)

            if (returnType == "V") {
                mv.visitInsn(Opcodes.RETURN)
            } else {
                mv.visitInsn(Opcodes.ARETURN) //返回值永远是Object
            }

            //替换只执行第一个，优先级最高的
            break
        }
    }

}
