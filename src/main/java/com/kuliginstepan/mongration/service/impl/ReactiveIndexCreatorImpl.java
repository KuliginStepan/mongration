package com.kuliginstepan.mongration.service.impl;

import com.kuliginstepan.mongration.service.IndexCreator;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperationsProvider;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveIndexCreatorImpl implements IndexCreator {

    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
    private final ReactiveIndexOperationsProvider indexOperationsProvider;
    private final IndexResolver indexResolver;

    public ReactiveIndexCreatorImpl(
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext,
        ReactiveIndexOperationsProvider indexOperationsProvider) {
        this.mappingContext = mappingContext;
        this.indexOperationsProvider = indexOperationsProvider;
        indexResolver = IndexResolver.create(mappingContext);
    }

    @Override
    public Mono<Void> createIndexes(Class<?> type) {
        MongoPersistentEntity persistentEntity = mappingContext.getPersistentEntity(type);
        return Flux.fromIterable(indexResolver.resolveIndexFor(type))
            .flatMap(index -> indexOperationsProvider.indexOps(persistentEntity.getCollection()).ensureIndex(index))
            .then();
    }

    @Override
    public Mono<Void> createIndexes() {
        return Flux.fromIterable(mappingContext.getPersistentEntities())
            .filter(entity -> entity.isAnnotationPresent(Document.class))
            .map(PersistentEntity::getType)
            .flatMap(this::createIndexes)
            .then();
    }
}
