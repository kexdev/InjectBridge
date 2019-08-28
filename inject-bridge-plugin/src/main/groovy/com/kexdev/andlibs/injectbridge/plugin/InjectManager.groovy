package com.kexdev.andlibs.injectbridge.plugin

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class InjectManager {

    public static boolean isEnable = true
    public static String[] ignorePackages

    public static List<InjectClassInfo> injectClassList = new ArrayList<>()

    private static InjectBodyClassVisitor injectInfoClassVisitor = new InjectBodyClassVisitor()
    private static TargetClassVisitor targetClassVisitor = new TargetClassVisitor()

    public static void clearInjectClassInfoList() {
        injectClassList.clear()
    }

    public static void addInjectClassInfo(InjectClassInfo injectClassInfo) {
        Logger.i("[InjectBridge] findInjectBody: " + injectClassInfo.toString())
        injectClassList.add(injectClassInfo)
    }

    public static List<InjectClassInfo> getInjectClassInfoList(String targetName) {
        List<InjectClassInfo> injectClassInfoList = new ArrayList<>()
        for (int i = 0; i < injectClassList.size(); i++) {
            InjectClassInfo injectClassInfo = injectClassList.get(i)
            if (injectClassInfo.target == targetName) {
                injectClassInfoList.add(injectClassInfo)
            }
        }

        //按优先级降序排列
        InjectClassInfoComparator comparator = new InjectClassInfoComparator()
        Collections.sort(injectClassInfoList, comparator)

        return injectClassInfoList
    }


    public static void findInjectInfo(File source) {
        if (source.isDirectory()) {
            source.eachFileRecurse { File file ->
                String filename = file.getName()
                if (filterClass(filename)) {
                    return
                }
                ClassReader classReader = new ClassReader(file.readBytes())
                classReader.accept(injectInfoClassVisitor, 0)
            }
        } else {
            JarFile jarFile = new JarFile(source)
            Enumeration<JarEntry> entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement()
                String filename = entry.getName()
                if (filterPackage(filename)) {
                    break
                }

                if (filterClass(filename)) {
                    continue
                }

                InputStream stream = jarFile.getInputStream(entry)
                if (stream != null) {
                    ClassReader classReader = new ClassReader(stream.bytes)
                    classReader.accept(injectInfoClassVisitor, 0)
                    stream.close()
                }
            }
            jarFile.close()
        }
    }

    public static void findTargetAndInject(File source) {
        if (source.isDirectory()) {
            source.eachFileRecurse { File file ->
                String filename = file.getName()
                if (filterClass(filename)) {
                    return
                }
                byte[] bytes = doInject(source, file.readBytes())
                if (bytes != null) {
                    Logger.i('[InjectBridge] replace class: [' + file.absolutePath + ']')
                    FileOutputStream outputStream = new FileOutputStream(file)
                    outputStream.write(bytes)
                    outputStream.close()
                }
            }
        } else {
            Map<String, byte[]> tempModifiedClassByteMap = new HashMap()

            JarFile jarFile = new JarFile(source)
            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries()
            while (jarEntryEnumeration.hasMoreElements()) {
                JarEntry jarEntry = jarEntryEnumeration.nextElement()
                String filename = jarEntry.getName()
                if (filterPackage(filename)) {
                    break
                }

                if (filterClass(filename)) {
                    continue
                }

                InputStream inputStream = jarFile.getInputStream(jarEntry)
                if (inputStream != null) {
                    byte[] bytes = doInject(source, inputStream.bytes)
                    if (bytes != null) {
                        tempModifiedClassByteMap.put(filename, bytes)
                    }
                }
                inputStream.close()
            }

            if (tempModifiedClassByteMap.size() != 0) {
                File tempJar = new File(source.absolutePath.replace('.jar', 'temp.jar'))
                if (tempJar.exists()) {
                    tempJar.delete()
                }

                jarEntryEnumeration = jarFile.entries()
                JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempJar))
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry jarEntry = jarEntryEnumeration.nextElement()
                    String filename = jarEntry.getName()
                    ZipEntry zipEntry = new ZipEntry(filename)
                    jarOutputStream.putNextEntry(zipEntry)
                    if (tempModifiedClassByteMap.containsKey(filename)) {
                        jarOutputStream.write(tempModifiedClassByteMap.get(filename))
                    } else {
                        InputStream inputStream = jarFile.getInputStream(jarEntry)
                        jarOutputStream.write(inputStream.bytes)
                        inputStream.close()
                    }
                    jarOutputStream.closeEntry()
                }
                jarOutputStream.close()

                Logger.i('[InjectBridge] replace jar: [' + source.absolutePath + ']')
                FileOutputStream outputStream = new FileOutputStream(source)
                outputStream.write(tempJar.bytes)
                outputStream.close()
                tempJar.delete()
            }
            jarFile.close()
        }
    }

    protected static byte[] doInject(File source, byte[] bytes) {
        ClassWriter classWriter = new ClassWriter(0)
        boolean methodInject
        OnMethodInjectListener onMethodInjectListener = new OnMethodInjectListener() {
            @Override
            void onInject() {
                methodInject = true
            }
        }
        targetClassVisitor.set(classWriter, onMethodInjectListener)
        try {
            ClassReader classReader = new ClassReader(bytes)
            classReader.accept(targetClassVisitor, 0)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
        if (methodInject) {
            return classWriter.toByteArray()
        } else {
            return null
        }
    }

    private static boolean filterPackage(String filename) {
        if (ignorePackages == null) return false

        for (int i = 0; i < ignorePackages.size(); i++) {
            if (filename.startsWith(ignorePackages[i])) {
                return true
            }
        }
        return false
    }

    private static boolean filterClass(String filename) {
        //如果未开启，则不处理
        if (!isEnable) {
            return true
        }

        if (!filename.endsWith(".class")
                || filename.contains('R$')
                || filename.contains('R.class')
                || filename.contains("BuildConfig.class")) {
            return true
        }

        return false
    }

}
