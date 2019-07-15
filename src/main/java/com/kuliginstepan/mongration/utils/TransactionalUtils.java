package com.kuliginstepan.mongration.utils;

import lombok.experimental.UtilityClass;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@UtilityClass
public class TransactionalUtils {

    /**
     * @param txTemplate {@link TransactionTemplate} to execute {@link Runnable}
     * @param runnable {@link Runnable} to execute with {@link TransactionTemplate}
     */
    public static void executeTransactional(TransactionTemplate txTemplate, Runnable runnable) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                runnable.run();
            }
        });
    }

}
