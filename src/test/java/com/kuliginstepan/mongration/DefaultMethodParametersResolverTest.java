package com.kuliginstepan.mongration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

class DefaultMethodParametersResolverTest {

    private DefaultListableBeanFactory beanFactory;
    private DefaultMethodParametersResolver resolver;

    @BeforeEach
    void setUp() {
        beanFactory = new DefaultListableBeanFactory();
        resolver = new DefaultMethodParametersResolver(beanFactory);
    }

    @Test
    void shouldResolveBeanByQualifier() throws NoSuchMethodException {
        GenericBeanDefinition bd1 = new GenericBeanDefinition();
        bd1.setBeanClass(TestBean.class);
        bd1.addQualifier(new AutowireCandidateQualifier(Qualifier.class, "myBean"));
        beanFactory.registerBeanDefinition("bean", bd1);

        GenericBeanDefinition bd2 = new GenericBeanDefinition();
        bd2.setBeanClass(TestBean.class);
        bd2.addQualifier(new AutowireCandidateQualifier(Qualifier.class, "myBean1"));
        beanFactory.registerBeanDefinition("bean1", bd2);

        Object[] objects = resolver.resolve(TestClass.class.getMethod("testQualifier", TestBean.class));

        assertThat(objects)
            .hasSize(1)
            .hasOnlyElementsOfType(TestBean.class);
    }

    @Test
    void shouldResolveBeanByType() throws NoSuchMethodException {
        GenericBeanDefinition bd1 = new GenericBeanDefinition();
        bd1.setBeanClass(TestBean.class);
        beanFactory.registerBeanDefinition("bean", bd1);

        Object[] objects = resolver.resolve(TestClass.class.getMethod("testWithoutQualifier", TestBean.class));

        assertThat(objects)
            .hasSize(1)
            .hasOnlyElementsOfType(TestBean.class);
    }

    @Data
    public static class TestBean {

        private String test = "test";
    }

    public static class TestClass {

        public void testQualifier(@Qualifier("myBean") TestBean bean) {

        }

        public void testWithoutQualifier(TestBean bean) {

        }
    }
}