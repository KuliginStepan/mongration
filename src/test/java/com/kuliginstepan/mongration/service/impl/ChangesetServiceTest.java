package com.kuliginstepan.mongration.service.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.kuliginstepan.mongration.MongrationException;
import org.junit.jupiter.api.Test;

class ChangesetServiceTest {

    private ChangeSetService changesetService = new ChangeSetService(null, null);

    @Test
    void shouldAllowExecuteVoidMethod() {
        assertThatCode(() -> changesetService.validateChangesetMethodSignature(
            ChangesetServiceTest.class.getDeclaredMethod("shouldAllowExecuteVoidMethod")).block())
            .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForNonVoidMethod() {
        assertThatExceptionOfType(MongrationException.class)
            .isThrownBy(() -> changesetService.validateChangesetMethodSignature(
                ChangesetServiceTest.class.getDeclaredMethod("testChangeSetMethod")).block());
    }

    private Object testChangeSetMethod() {
        return new Object();
    }
}