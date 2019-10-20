package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.kuliginstepan.mongration.MongrationException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class ReactiveChangesetServiceTest {

    private ReactiveChangeSetService changesetService = new ReactiveChangeSetService(null, null);

    @Test
    void shouldAllowExecuteMonoMethod() {
        assertThatCode(() -> changesetService.validateChangesetMethodSignature(
            ReactiveChangesetServiceTest.class.getDeclaredMethod("testChangeSetMethod")).block())
            .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForNonMonoMethod() {
        assertThatExceptionOfType(MongrationException.class)
            .isThrownBy(() -> changesetService.validateChangesetMethodSignature(
                ReactiveChangesetServiceTest.class.getDeclaredMethod("shouldAllowExecuteMonoMethod")).block());
    }

    private Mono<Object> testChangeSetMethod() {
        return Mono.empty();
    }
}