package com.demo.asm.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class AsmMethodVisitor extends AdviceAdapter {
    private static final String TAG = "AsmMethodVisitor";
    private static final String ANNOTATION_TRACK_METHOD = "Lcom/demo/asm/lib/TrackMethod;";
    private static final String METHOD_EVENT_MANAGER = "com/demo/asm/lib/MethodObservable";
    private final MethodVisitor methodVisitor;
    private final String methodName;

    private boolean needInject;
    private String mTag;

    public AsmMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc) {
        super(Opcodes.ASM6, methodVisitor, access, name, desc);
        this.methodVisitor = methodVisitor;
        this.methodName = name;
    }

    public void setTag(String tag) {
        needInject = true;
        this.mTag = tag;
    }

    /**
     * 访问类的注解
     *
     * @param desc    表示类注解类的描述；
     * @param visible 表示该注解是否运行时可见
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);
        if (desc.equals(ANNOTATION_TRACK_METHOD)) {
            needInject = true;
            return new AnnotationVisitor(Opcodes.ASM6, annotationVisitor) {
                /**
                 * @param name 注解key值
                 * @param value value值
                 */
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, value);
                    if (name.equals("tag") && value instanceof String) {
                        mTag = (String) value;
                        Logger.log(TAG, mTag, " methodName=" + methodName);
                    }
                }
            };
        }
        return annotationVisitor;
    }

    @Override
    protected void onMethodEnter() {
        if (needInject && mTag != null) {
            //visitMethodInsn:访问方法操作指令
            //opcode：为INVOKESPECIAL,INVOKESTATIC,INVOKEVIRTUAL,INVOKEINTERFACE;
            //owner:方法拥有者的名称;
            //name:方法名称;
            //descriptor:方法描述，参数和返回值;
            //isInterface；是否是接口;
//            methodVisitor.visitMethodInsn(INVOKESTATIC, METHOD_EVENT_MANAGER, "getInstance", "()L" + METHOD_EVENT_MANAGER + ";", false);
            methodVisitor.visitFieldInsn(GETSTATIC, METHOD_EVENT_MANAGER, "INSTANCE", "L" + METHOD_EVENT_MANAGER + ";");
            //visitLdcInsn:访问ldc指令，也就是访问常量池索引；
            //value:必须是非空的Integer,Float,Double,Long,String,或者对象的Type,Array的Type,Method Sort的Type，或者Method Handle常量中的Handle，或者ConstantDynamic;
            methodVisitor.visitLdcInsn(mTag);
            methodVisitor.visitLdcInsn(methodName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, METHOD_EVENT_MANAGER, "notifyMethodStart", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (needInject && mTag != null) {
//            methodVisitor.visitMethodInsn(INVOKESTATIC, METHOD_EVENT_MANAGER, "getInstance", "()L" + METHOD_EVENT_MANAGER + ";", false);
            methodVisitor.visitFieldInsn(GETSTATIC, METHOD_EVENT_MANAGER, "INSTANCE", "L" + METHOD_EVENT_MANAGER + ";");
            methodVisitor.visitLdcInsn(mTag);
            methodVisitor.visitLdcInsn(methodName);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, METHOD_EVENT_MANAGER, "notifyMethodEnd", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
    }

    /**
     * 访问方法操作指令
     *
     * @param opcode 为INVOKESPECIAL,INVOKESTATIC,INVOKEVIRTUAL,INVOKEINTERFACE;
     * @param owner  方法拥有者的名称;
     * @param name   方法名称;
     * @param desc   方法描述，参数和返回值;
     * @param itf    是否是接口;
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
