package com.miniSpring.container;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BeanDefinition {
    private final String beanName;
    private final Class<?> beanClass;
    private final BeanScope scope;
    private final List<Field> injectedFields;
    private final Constructor<?> injectedConstructors;
    private final List<Method> postConMethods;

    public BeanDefinition(String beanName,Class<?> beanClass, BeanScope scope, List<Field> injectedFields,Constructor<?> injectedConstructor, List<Method> postMethods){
        this.beanName=beanName;
        this.beanClass=beanClass;
        this.scope=scope;
        this.injectedFields=injectedFields;
        this.injectedConstructors=injectedConstructor;
        this.postConMethods=postMethods;
    }
    public String getBeanName(){
        return beanName;
    }
    public Class<?> getBeanClass(){
        return beanClass;
    }
    public BeanScope getBeanScope(){
        return scope;

    }
    public List<Field> getFields(){
        return injectedFields;
    }
    public Constructor<?> getConstructor(){
        return injectedConstructors;
    }
    public List<Method> getMethods(){
        return postConMethods;
    }
    public boolean isSingleton(){
        return scope == BeanScope.SINGLETON;
    }
    @Override
    public String toString(){
        return "BeanDefination={name: '"+beanName+"', class: '"+beanClass+"', scope: '"+scope+"', injected fields: '"+injectedFields+"', injected Constructor: '"+injectedConstructors+"', post constructor methods: '"+postConMethods+"' }";
    }
}
