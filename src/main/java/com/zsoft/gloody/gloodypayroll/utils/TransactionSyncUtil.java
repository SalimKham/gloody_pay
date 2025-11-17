package com.zsoft.gloody.gloodypayroll.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionSyncUtil {

    public static void afterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
