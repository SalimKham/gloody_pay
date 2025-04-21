package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;

@Service
public class EntityManagerService {
    @PersistenceContext
    public EntityManager entityManager;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Session getSession() {
        return getEntityManagerSession();
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public <T, K extends Serializable> T getProxyEntity(Class<T> entityClass, K entityId) {
        Session session = getSession();
        if (session != null && entityId != null) {
            try {
                return session.get(entityClass, entityId);
            } catch (EntityNotFoundException ignored) {
                return null;
            }
        }
        return null;
    }

    public void afterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }

    private Session getEntityManagerSession() {
        if (entityManager != null) {
            try {
                return entityManager.unwrap(Session.class);
            } catch (IllegalStateException ex) {
                throw new ApiRuntimeException("error.entityManager.session.failed", ex.getCause());
            }
        }
        return null;
    }

    public void applyFilter(Long companyId) {
        Session session = getSession();
        if (session != null && companyId != null) {
            org.hibernate.Filter filter = session.enableFilter("companyFilter");
            filter.setParameter("companyId", companyId);
            filter.validate();
        }
    }
}
