package com.kuliginstepan.mongration;

import java.lang.reflect.Method;

@FunctionalInterface
public interface MethodParametersResolver {

    Object[] resolve(Method method);
}
