package com.kuliginstepan.mongration.configuration;

import com.kuliginstepan.mongration.Mongration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongrationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private BeanFactory factory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAssignableFrom(MongoTemplate.class)) {
            factory.getBean(Mongration.class);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }
}
