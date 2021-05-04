package com.kuliginstepan.mongration.service.impl;

import com.kuliginstepan.mongration.service.IndexCreator;
import java.util.List;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperationsProvider;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class IndexCreatorImpl implements IndexCreator {

    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
    private final IndexOperationsProvider indexOperationsProvider;
    private final IndexResolver indexResolver;

    public IndexCreatorImpl(MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext,
                            IndexOperationsProvider indexOperationsProvider) {
        this.mappingContext = mappingContext;
        this.indexOperationsProvider = indexOperationsProvider;
        indexResolver = IndexResolver.create(mappingContext);
    }

    @Override
    public Mono<Void> createIndexes(Class<?> type) {
        MongoPersistentEntity persistentEntity = mappingContext.getPersistentEntity(type);
        return createIndexes(type, persistentEntity.getCollection());
    }

    @Override
    public Mono<Void> createIndexes(Class<?> type, String collection) {
        return Flux.fromIterable(indexResolver.resolveIndexFor(type))
            .map(index -> indexOperationsProvider.indexOps(collection).ensureIndex(index))
            .then();
    }

    @Override
    public Mono<Void> createIndexes(List<IndexDefinition> definitions, String collection) {
        return Flux.fromIterable(definitions)
            .map(index -> indexOperationsProvider.indexOps(collection).ensureIndex(index))
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
