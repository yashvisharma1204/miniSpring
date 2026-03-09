package com.miniSpring.scanner;
import com.miniSpring.annotations.Component;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassPathScanner {
    public List<Class<?>> scan(String basePackage){
        List<Class<?>> componentClass = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            System.out.println("[miniSpring] Warning: No resource found for package: " + basePackage);
            return componentClass;
        }
        File directory = new File(resource.getFile());
        if (!directory.exists()) {
            System.out.println("[miniSpring] Warning: Directory does not exist: " + directory);
            return componentClass;
        }
        scanDirectory(directory, basePackage, componentClass);
        return componentClass;
    }
    private void scanDirectory(File directory, String packageName, List<Class<?>> result) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                // Recurse into sub-packages
                scanDirectory(file, packageName + "." + file.getName(), result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> c = Class.forName(className);
                    if (c.isAnnotationPresent(Component.class)) {
                        result.add(c);
                        System.out.println("[MiniSpring] Found component: " + c.getSimpleName());
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("[MiniSpring] Could not load class: " + className);
                }
            }
        }
    }
}

