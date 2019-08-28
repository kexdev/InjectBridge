package com.kexdev.andlibs.injectbridge.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project

class InjectTransform extends Transform {

    private Project project

    public InjectTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return InjectConfig.INJECT_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS //内容：class
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT //范围：整个工程
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        //clear
        InjectManager.clearInjectClassInfoList()

        //find
        transformInvocation.getInputs().each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                InjectManager.findInjectInfo(directoryInput.file)
            }
            input.jarInputs.each { JarInput jarInput ->
                InjectManager.findInjectInfo(jarInput.file)
            }
        }

        //inject
        transformInvocation.getInputs().each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
                InjectManager.findTargetAndInject(dest)
            }
            input.jarInputs.each { JarInput jarInput ->
                def dest = transformInvocation.outputProvider.getContentLocation(jarInput.name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
                InjectManager.findTargetAndInject(dest)
            }
        }
    }
}
