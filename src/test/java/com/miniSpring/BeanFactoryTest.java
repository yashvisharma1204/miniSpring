package com.miniSpring;

import com.miniSpring.annotations.Component;
import com.miniSpring.annotations.Inject;
import com.miniSpring.annotations.PostConstruct;
import com.miniSpring.container.BeanFactory;
import com.miniSpring.container.BeanDefinition;
import com.miniSpring.container.BeanScope;
import com.miniSpring.exceptions.BeanNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class BeanFactoryTest {

    private BeanFactory factory;

    @BeforeEach
    void setUp() {
        factory = new BeanFactory();
    }

    // ── Simple bean creation ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should create a simple bean with no dependencies")
    void shouldCreateSimpleBean() {
        registerBean(SimpleBean.class);
        Object bean = factory.getBean("simpleBean");
        assertNotNull(bean);
        assertInstanceOf(SimpleBean.class, bean);
    }

    @Test
    @DisplayName("Should return same instance for singleton scope")
    void shouldReturnSingletonInstance() {
        registerBean(SimpleBean.class);
        Object a = factory.getBean("simpleBean");
        Object b = factory.getBean("simpleBean");
        assertSame(a, b, "Singleton beans should be the same instance");
    }

    // ── Field injection ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should inject a field dependency")
    void shouldInjectField() {
        registerBean(SimpleBean.class);
        registerBean(DependentBean.class);

        DependentBean bean = factory.getBean(DependentBean.class);
        assertNotNull(bean.simpleBean, "Injected field should not be null");
    }

    // ── @PostConstruct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should call @PostConstruct after injection")
    void shouldCallPostConstruct() {
        registerBean(LifecycleBean.class);
        LifecycleBean bean = factory.getBean(LifecycleBean.class);
        assertTrue(bean.initialized, "@PostConstruct should have been called");
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw BeanNotFoundException for unknown bean")
    void shouldThrowForUnknownBean() {
        assertThrows(BeanNotFoundException.class, () -> factory.getBean("doesNotExist"));
    }

    @Test
    @DisplayName("Should throw BeanNotFoundException for unknown type")
    void shouldThrowForUnknownType() {
        assertThrows(BeanNotFoundException.class, () -> factory.getBean(String.class));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void registerBean(Class<?> cls) {
        String name = BeanFactory.resolveBeanName(cls);

        var injectableFields = new java.util.ArrayList<java.lang.reflect.Field>();
        for (var field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) injectableFields.add(field);
        }

        var postConstructMethods = new java.util.ArrayList<java.lang.reflect.Method>();
        for (var method : cls.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) postConstructMethods.add(method);
        }

        factory.registerDefinition(new BeanDefinition(
            name, cls, BeanScope.SINGLETON, injectableFields, null, postConstructMethods
        ));
    }

    // ── Inner test classes ────────────────────────────────────────────────────

    @Component
    static class SimpleBean {}

    @Component
    static class DependentBean {
        @Inject
        SimpleBean simpleBean;
    }

    @Component
    static class LifecycleBean {
        boolean initialized = false;

        @PostConstruct
        void init() { initialized = true; }
    }
}