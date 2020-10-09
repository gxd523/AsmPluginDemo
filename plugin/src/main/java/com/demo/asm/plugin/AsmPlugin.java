package com.demo.asm.plugin;


import com.android.annotations.NonNull;
import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AsmPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        if (appExtension == null) {
            return;
        }
        project.getExtensions().create("AsmConfig", AsmConfig.class);
        appExtension.registerTransform(new AsmTransform(project));// 注册优先于task任务的添加
    }
}
