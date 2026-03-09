package com.miniSpring.container;

import com.miniSpring.annotations.Component;
import com.miniSpring.exceptions.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class BeanFactory {
    private final Map<String, BeanDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, Object> singletonRegistry = new HashMap<>();
    private final Set<String> beansInCreation = new HashSet<>();

    public void registerDefinition(BeanDefinition definition) {
        definitions.put(definition.getBeanName(), definition);
    }

    public Object getBean(String name) {
        BeanDefinition definition = definitions.get(name);
        if (definition == null) {
            throw new BeanNotFoundException("No bean registered with name: '" + name + "'");
        }
        return resolveBean(definition);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        List<BeanDefinition> matches = new ArrayList<>();

        for (BeanDefinition def : definitions.values()) {
            if (type.isAssignableFrom(def.getBeanClass())) {
                matches.add(def);
            }
        }

        if (matches.isEmpty()) {
            throw new BeanNotFoundException("No bean found of type: " + type.getName());
        }
        if (matches.size() > 1) {
            throw new BeanNotFoundException(
                "Multiple beans found of type " + type.getSimpleName() +
                ". Use getBean(name, type) to specify which one."
            );
        }

        return (T) resolveBean(matches.get(0));
    }
    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(definitions.keySet());
    }
    private Object resolveBean(BeanDefinition definition) {
        // For singletons, return cached instance if available
        if (definition.isSingleton()) {
            Object cached = singletonRegistry.get(definition.getBeanName());
            if (cached != null) {
                return cached;
            }
        }
        // Detect circular dependencies
        if (beansInCreation.contains(definition.getBeanName())) {
            throw new CircularDependencyException(
                "Circular dependency detected while creating bean: '" + definition.getBeanName() + "'. " +
                "Creation chain: " + beansInCreation
            );
        }
        beansInCreation.add(definition.getBeanName());
        try {
            Object instance = createInstance(definition);
            injectFields(instance, definition);
            runPostConstruct(instance, definition);
            if (definition.isSingleton()) {
                singletonRegistry.put(definition.getBeanName(), instance);
            }
            return instance;
        } catch (Exception e) {
            throw new BeanCreationException(
                "Failed to create bean '" + definition.getBeanName() + "': " + e.getMessage(), e
            );
        } finally {
            beansInCreation.remove(definition.getBeanName());
        }
    }

    private Object createInstance(BeanDefinition definition) throws Exception {
        Constructor<?> ctor = definition.getConstructor();
        if (ctor != null) {
            // Constructor injection — resolve each parameter
            Class<?>[] paramTypes = ctor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                args[i] = getBean(paramTypes[i]);
            }
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } else {
            // Default no-arg constructor
            Constructor<?> noArg = definition.getBeanClass().getDeclaredConstructor();
            noArg.setAccessible(true);
            return noArg.newInstance();
        }
    }

    private void injectFields(Object instance, BeanDefinition definition) throws Exception {
        for (Field field : definition.getFields()) {
            Object dependency = getBean(field.getType());
            field.setAccessible(true);
            field.set(instance, dependency);
        }
    }

    private void runPostConstruct(Object instance, BeanDefinition definition) throws Exception {
        for (Method method : definition.getMethods()) {
            method.setAccessible(true);
            method.invoke(instance);
        }
    }
    public static String resolveBeanName(Class<?> cls) {
        Component annotation = cls.getAnnotation(Component.class);
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        String simpleName = cls.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}