package com.demo.asm.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * 核心类
 */
public class AsmTransform extends Transform {
    private static final String TAG = "AsmTransform";
    private static AsmConfig mAsmConfig;
    private Map<String, File> modifyMap = new HashMap<>();
    private Project mProject;

    public AsmTransform(Project project) {
        this.mProject = project;
    }

    public static AsmConfig getAsmConfig() {
        return mAsmConfig;
    }

    @Override
    public String getName() {
        return AsmTransform.class.getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 是否开启增量编译
     */
    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        // transform方法中才能获取到注册的对象
        mAsmConfig = (AsmConfig) this.mProject.getExtensions().getByName(AsmConfig.class.getSimpleName());
        Logger.isDebug = mAsmConfig.isDebug;
        String projectDri = this.mProject.getProjectDir().getAbsolutePath();
        initFilterClassFile(projectDri);
        if (!isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        // 获取输入（消费型输入，需要传递给下一个Transform）
        Collection<TransformInput> inputList = transformInvocation.getInputs();
        for (TransformInput input : inputList) {
            // 遍历输入，分别遍历其中的jar以及directory
            for (JarInput jarInput : input.getJarInputs()) {
                // 对jar文件进行处理
                Logger.log(TAG, "Find jar input-->" + jarInput.getName());
                transformJar(transformInvocation, jarInput);
            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                // 对directory进行处理
                Logger.log(TAG, "Find dir input-->" + directoryInput.getFile().getName());
                transformDirectory(transformInvocation, directoryInput);
            }
        }
    }

    private void initFilterClassFile(String projectDri) {
        if (mAsmConfig.filterClassNameListFile != null && mAsmConfig.filterClassNameListFile.length() != 0) {
            File filterClassFile = new File(projectDri, mAsmConfig.filterClassNameListFile);
            try {
                FileReader fileReader = new FileReader(filterClassFile.getAbsolutePath());
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Logger.log(TAG, "filterClassName-->" + line);
                    if (mAsmConfig.filterClassNameList == null) {
                        mAsmConfig.filterClassNameList = new ArrayList<>();
                    }
                    if (line.length() > 0) {
                        mAsmConfig.filterClassNameList.add(line);
                    }
                }
                bufferedReader.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void transformJar(TransformInvocation invocation, JarInput input) throws IOException {
        File tempDir = invocation.getContext().getTemporaryDir();
        String destName = input.getFile().getName();
        String hexName = DigestUtils.md5Hex(input.getFile().getAbsolutePath()).substring(0, 8);
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4);
        }
        // 获取输出路径
        File dest = invocation.getOutputProvider().getContentLocation(destName + "_" + hexName, input.getContentTypes(), input.getScopes(), Format.JAR);
        JarFile originJar = new JarFile(input.getFile());
        //input:/build/intermediates/runtime_library_classes/release/classes.jar
        File outputJar = new File(tempDir, "temp_" + input.getFile().getName());
        //out:/build/tmp/transformClassesWithAsmTransformForRelease/temp_classes.jar
        //dest:/build/intermediates/transforms/AsmTransform/release/26.jar
        JarOutputStream output = new JarOutputStream(new FileOutputStream(outputJar));

        // 遍历原jar文件寻找class文件
        Enumeration<JarEntry> enumeration = originJar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry originEntry = enumeration.nextElement();
            InputStream inputStream = originJar.getInputStream(originEntry);
            String entryName = originEntry.getName();
            if (entryName.endsWith(".class")) {
                JarEntry destEntry = new JarEntry(entryName);
                output.putNextEntry(destEntry);
                byte[] sourceBytes = IOUtils.toByteArray(inputStream);
                // 修改class文件内容
                byte[] modifiedBytes = null;
                if (filterModifyClass(entryName)) {
                    Logger.log(TAG, "Modify jar-->", entryName);
                    modifiedBytes = modifyClass(sourceBytes);
                }
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                output.write(modifiedBytes);
            }
            output.closeEntry();
        }
        output.close();
        originJar.close();
        // 复制修改后jar到输出路径
        FileUtils.copyFile(outputJar, dest);
    }

    private void transformDirectory(TransformInvocation invocation, DirectoryInput input) throws IOException {
        File tempDir = invocation.getContext().getTemporaryDir();
        // 获取输出路径
        File dest = invocation.getOutputProvider().getContentLocation(input.getName(), input.getContentTypes(), input.getScopes(), Format.DIRECTORY);
        File dir = input.getFile();
        if (dir != null && dir.exists()) {
            //tempDir=build/tmp/transformClassesWithAsmTransformForDebug
            //dir=build/intermediates/javac/debug/compileDebugJavaWithJavac/classes

            traverseDirectory(dir.getAbsolutePath(), tempDir, dir);
            //Map<String, File> modifiedMap = new HashMap<>();
            //traverseDirectory(tempDir, dir, modifiedMap, dir.getAbsolutePath() + File.separator);

            //input.getFile=build/intermediates/javac/debug/compileDebugJavaWithJavac/classes
            //dest=build/intermediates/transforms/AsmTransform/debug/52

            FileUtils.copyDirectory(input.getFile(), dest);

            for (Map.Entry<String, File> entry : modifyMap.entrySet()) {
                File target = new File(dest.getAbsolutePath() + File.separatorChar + entry.getKey().replace('.', File.separatorChar) + ".class");
                if (target.exists()) {
                    target.delete();
                }
                FileUtils.copyFile(entry.getValue(), target);
                entry.getValue().delete();
            }
        }
    }

    /**
     * 遍历目录下面的class文件
     *
     * @param basedir 基准目录，和dir对比需要找到包路径
     * @param tempDir 需要写入的临时目录
     * @param dir     class文件目录
     */
    private void traverseDirectory(String basedir, File tempDir, File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                traverseDirectory(basedir, tempDir, file);
            } else if (file.getAbsolutePath().endsWith(".class")) {
                String pathName = file.getAbsolutePath().replace(basedir + File.separator, "");
                String className = pathName.replace(File.separator, ".").replace(".class", "");
                byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(file));
                byte[] modifiedBytes = null;
                if (filterModifyClass(className + ".class")) {
                    Logger.log(TAG, "Modify dir-->", className + ".class");
                    modifiedBytes = modifyClass(sourceBytes);
                }
                if (modifiedBytes == null) {
                    modifiedBytes = sourceBytes;
                }
                File modified = new File(tempDir, className + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                new FileOutputStream(modified).write(modifiedBytes);
                modifyMap.put(className, modified);
            }
        }
    }

    private boolean filterModifyClass(String className) {
        if (className == null || className.length() == 0) {
            return false;
        }
        String s = className.replace(File.separator, ".");
        if (mAsmConfig.filterClassNameList != null && mAsmConfig.filterClassNameList.size() > 0) {
            for (String str : mAsmConfig.filterClassNameList) {
                if (s.equals(str)) {
                    return false;
                }
            }
        }
        if (mAsmConfig.filterContainsClass != null && mAsmConfig.filterContainsClass.length > 0) {
            for (String str : mAsmConfig.filterContainsClass) {
                if (s.contains(str)) {
                    return false;
                }
            }
        }
        if (mAsmConfig.filterStartWithClass != null && mAsmConfig.filterStartWithClass.length > 0) {
            for (String str : mAsmConfig.filterStartWithClass) {
                if (s.startsWith(str)) {
                    return false;
                }
            }
        }
        return true;
    }

    private byte[] modifyClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new AsmClassVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }
}
