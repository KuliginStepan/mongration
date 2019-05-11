package com.kuliginstepan.mongration;

import com.kuliginstepan.mongration.annotation.ChangeLog;
import com.kuliginstepan.mongration.annotation.ChangeSet;
import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.ChangeSetService;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ReflectionUtils;

/**
 * Class which executes particulars {@link ChangeSet} found in {@link ChangeLog}.
 */
@Slf4j
public class Mongration implements BeanFactoryAware, InitializingBean {

    private final MongoTemplate template;
    @Nullable
    private final TransactionTemplate txTemplate;
    private final ChangeSetService service;
    private ListableBeanFactory factory;

    public Mongration(@NonNull MongoTemplate template, @Nullable TransactionTemplate txTemplate,
        @NonNull MongrationProperties properties) {
        this.template = template;
        this.txTemplate = txTemplate;
        service = new ChangeSetService(template, properties);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = (ListableBeanFactory) beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("mongration started");
        TreeMap<Object, TreeSet<Method>> migrations = findMigrationsForExecution();
        if (hasNotExecutedMigrations(migrations)) {
            if (!service.acquireProcessLock()) {
                throw new IllegalStateException("mongration could not acquire process lock");
            }
            try {
                executeMigrations(migrations);
            } finally {
                service.releaseProcessLock();
            }
        }
        log.info("mongration finished his work");
    }

    private static boolean hasNotExecutedMigrations(TreeMap<Object, TreeSet<Method>> migrations) {
        return migrations.values().stream().anyMatch(methods -> !methods.isEmpty());
    }

    private TreeMap<Object, TreeSet<Method>> findMigrationsForExecution() {
        return factory.getBeansWithAnnotation(ChangeLog.class).values().stream()
            .collect(Collectors.toMap(
                Function.identity(),
                changelog -> Arrays.stream(ChangeSetService.getChangelogClass(changelog).getMethods())
                    .filter(method -> method.isAnnotationPresent(ChangeSet.class))
                    .filter(method -> service.needExecuteChangeSet(method, changelog))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparingInt(method -> ChangeSetService.getChangeSet(method).order())))),
                (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                },
                () -> new TreeMap<>(AnnotationAwareOrderComparator.INSTANCE)));
    }

    private void executeMigrations(TreeMap<Object, TreeSet<Method>> migrations) {
        log.info("started executing migrations");
        migrations.forEach(
            (changelog, changeSets) -> changeSets.forEach(changeSet -> executeMigration(changeSet, changelog)));
    }

    private void executeMigration(Method changeSetMethod, Object changelog) {
        service.validateChangeSetMethodSignature(changeSetMethod);
        Class<?>[] params = changeSetMethod.getParameterTypes();
        if (params.length == 1) {
            ReflectionUtils.invokeMethod(changeSetMethod, changelog, template);
        } else {
            if (params[0].isAssignableFrom(MongoTemplate.class)) {
                ReflectionUtils.invokeMethod(changeSetMethod, changelog, template, txTemplate);
            } else {
                ReflectionUtils.invokeMethod(changeSetMethod, changelog, txTemplate, template);
            }
        }
        service.saveChangeSet(changeSetMethod, changelog);
    }

}
