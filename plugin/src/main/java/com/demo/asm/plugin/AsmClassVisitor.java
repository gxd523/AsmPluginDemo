package com.demo.asm.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;

/**
 * 修改字节码文件
 */
public class AsmClassVisitor extends ClassVisitor {
    /**
     * 例：com/demo/asm/plugin/sample/MainActivity
     */
    private String className;

    public AsmClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor);
    }

    /**
     * 访问类的头部
     *
     * @param version    version指的是类的版本
     * @param access     指的是类的修饰符
     * @param name       类的名称
     * @param signature  类的签名，如果类不是泛型或者没有继承泛型类，那么signature为空
     * @param superName  类的父类名称
     * @param interfaces 类继承的接口
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    /**
     * 访问类的方法，如果需要修改类方法信息，则可以重写此方法;
     *
     * @param access    表示该域的访问方式，public，private或者static,final等等；
     * @param name      指的是方法的名称；
     * @param desc      表示方法的参数类型和返回值类型；
     * @param signature 指的是域的签名，一般是泛型域才会有签名;
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        AsmMethodVisitor asmMethodVisitor = new AsmMethodVisitor(methodVisitor, access, name, desc);
        AsmConfig asmConfig = AsmTransform.getAsmConfig();
        if (Util.isEmpty(asmConfig.injectMethodPairList)) {
            return asmMethodVisitor;
        }
        for (String injectMethodString : asmConfig.injectMethodPairList) {
            if (injectMethodString == null || injectMethodString.trim().length() < 3 || !injectMethodString.trim().contains("#")) {
                continue;
            }
            String[] split = injectMethodString.split("#");
            if (split.length != 2) {
                continue;
            }
            String className = split[0].replace('.', File.separatorChar);
            if (name.equals(split[1]) && className.equals(this.className)) {
                System.out.println("injectMethodPairList-->" + className + "..." + split[1]);
                asmMethodVisitor.setTag("addFromListDefaultTag");
                break;
            }
        }
        return asmMethodVisitor;
    }
}
