package com.kuliginstepan.mongration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.kuliginstepan.mongration.configuration.MongrationProperties;
import com.kuliginstepan.mongration.service.AbstractChangeSetService;
import com.kuliginstepan.mongration.service.IndexCreator;
import com.kuliginstepan.mongration.service.LockService;
import java.lang.reflect.Method;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AbstractMongrationTest {

    @Spy
    private MongrationProperties properties;

    @Mock
    private LockService lockService;

    @InjectMocks
    private FakeMongration mongration;

    @Test
    void shouldSuppressLockReleasingExceptionsIfMigrationFailed() {
        // given
        String actionErrorMessage = "Sample error!";
        MongrationException releaseLockException = new MongrationException("Failed releasing lock!");

        // given mocked
        doReturn(Mono.empty()).when(lockService).acquireLock();
        doReturn(Mono.error(releaseLockException))
            .when(lockService).releaseLock();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            mongration.withLockAcquired(Mono.error(new RuntimeException(actionErrorMessage))).subscribe()
        );

        // then
        Assertions.assertThat(exception.getCause())
            .hasMessage(actionErrorMessage)
            .hasSuppressedException(releaseLockException);
    }

    @Test
    void shouldRetryLockAcquiringOnMongrationExceptions() {
        // given
        properties.setRetryCount(1);

        // given mocked
        doReturn(
            Mono.error(new MongrationException("Let's fail first try!")),
            Mono.empty()
        ).when(lockService).acquireLock();

        // when
        mongration.acquireLock().subscribe();

        // then no exception thrown
    }

    @Test
    void shouldNotRetryLockAcquiringOnOtherExceptions() {
        // given
        String errorMessage = "Let's fail first try!";
        properties.setRetryCount(1);

        // given mocked
        doReturn(
            Mono.error(new RuntimeException(errorMessage)),
            Mono.empty()
        ).when(lockService).acquireLock();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            mongration.acquireLock().subscribe()
        );

        // then
        Assertions.assertThat(exception.getCause())
            .hasMessage(errorMessage);
    }

    private static class FakeMongration extends AbstractMongration {

        public FakeMongration(AbstractChangeSetService changesetService,
            IndexCreator indexCreator,
            LockService lockService,
            MongrationProperties properties
        ) {
            super(changesetService, indexCreator, lockService, properties);
        }

        @Override
        protected Mono<Object> executeChangeSetMethod(Object changelog, Method changesetMethod) {
            return null;
        }
    }
}
