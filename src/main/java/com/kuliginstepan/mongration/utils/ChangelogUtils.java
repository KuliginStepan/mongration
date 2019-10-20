package com.kuliginstepan.mongration.utils;

import com.kuliginstepan.mongration.annotation.Changelog;
import com.kuliginstepan.mongration.annotation.Changeset;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

@UtilityClass
public class ChangelogUtils {

    public static Changeset extractChangeset(Method changesetMethod) {
        return AnnotationUtils.getAnnotation(changesetMethod, Changeset.class);
    }

    public static List<Method> findChangeSetMethods(Class<?> changelogClass) {
        return Arrays.stream(changelogClass.getMethods())
            .filter(method -> method.isAnnotationPresent(Changeset.class))
            .collect(Collectors.toList());
    }

    public static String generateChangeLogId(Class<?> changelogClass) {
        return Optional.of(changelogClass)
            .map(changelog -> AnnotationUtils.getAnnotation(changelog, Changelog.class))
            .filter(changelog -> StringUtils.hasLength(changelog.id()))
            .map(Changelog::id)
            .orElseGet(changelogClass::getSimpleName);
    }

    public static String generateChangeSetId(Method changesetMethod) {
        return Optional.of(changesetMethod)
            .map(changeset -> AnnotationUtils.getAnnotation(changeset, Changeset.class))
            .filter(changeset -> StringUtils.hasLength(changeset.id()))
            .map(Changeset::id)
            .orElseGet(changesetMethod::getName);
    }

    public static Class<?> getChangelogClass(Object changelog) {
        return AopProxyUtils.ultimateTargetClass(changelog);
    }
}
