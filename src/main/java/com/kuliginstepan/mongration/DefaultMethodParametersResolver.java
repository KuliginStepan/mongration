package com.kuliginstepan.mongration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;

@RequiredArgsConstructor
class DefaultMethodParametersResolver implements MethodParametersResolver {

    private final ListableBeanFactory beanFactory;

    @Override
    public Object[] resolve(Method method) {
        Object[] parameterBeans = new Object[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Annotation[] paramAnnotations = method.getParameterAnnotations()[i];
            Class<?> parameterType = method.getParameterTypes()[i];
            Object bean = Arrays.stream(paramAnnotations)
                .filter(Qualifier.class::isInstance)
                .map(Qualifier.class::cast)
                .findAny()
                .<Object>map(annotation ->
                    BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, parameterType, annotation.value())
                )
                .orElseGet(() -> beanFactory.getBean(parameterType));
            parameterBeans[i] = bean;
        }
        return parameterBeans;
    }
}
