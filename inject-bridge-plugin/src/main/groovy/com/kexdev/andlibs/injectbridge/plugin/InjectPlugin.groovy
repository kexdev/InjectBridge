package com.kexdev.andlibs.injectbridge.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        //只有application支持此插件
        if (!hasApp) {
            throw new IllegalStateException("'com.android.application' plugin required.")
        }

        //添加参数
        project.extensions.create(InjectConfig.INJECT_NAME, InjectPluginExtension)

        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new InjectTransform(project))

        project.afterEvaluate {
            Logger.project = project
            Logger.showLog = project.injectBridge.showLog
            InjectManager.isEnable = project.injectBridge.enabled
            InjectManager.ignorePackages = project.injectBridge.ignorePackages
        }
    }

}
