package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.models.entities.TenantSupport;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import liquibase.repackaged.org.apache.commons.lang3.ArrayUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TenantInterceptor {
    @Bean
    public EmptyInterceptor getEmptyInterceptor() {
        return new EmptyInterceptor() {
            private Long getCurrentTenant() {
                return SecurityUtils.getCurrentTenantId();
            }

            @Override
            public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
                if (entity instanceof TenantSupport) {
                    ((TenantSupport) entity).setTenantId(getCurrentTenant());
                }
            }

            @Override
            public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
                if (entity instanceof TenantSupport) {
                    ((TenantSupport) entity).setTenantId(getCurrentTenant());
                    int index = ArrayUtils.indexOf(propertyNames, "tenantId");
                    if (index != -1) {
                        currentState[index] = getCurrentTenant();
                    }
                    return false;
                }
                return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
            }

            @Override
            public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
                if (entity instanceof TenantSupport) {
                    ((TenantSupport) entity).setTenantId(getCurrentTenant());
                    int index = ArrayUtils.indexOf(propertyNames, "tenantId");
                    if (index != -1) {
                        state[index] = getCurrentTenant();
                    }
                    return false;
                }
                return super.onSave(entity, id, state, propertyNames, types);
            }

            @Override
            public String onPrepareStatement(String sql) {
                return super.onPrepareStatement(sql);
            }
        };
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factory, DataSource dataSource, JpaProperties properties) {
        Map<String, Object> jpaPropertiesMap = new HashMap<>(properties.getProperties());
        jpaPropertiesMap.put("hibernate.ejb.interceptor", getEmptyInterceptor());
        return factory.dataSource(dataSource).packages("com.zsoft.gloody.gloodypayroll").properties(jpaPropertiesMap).build();
    }
}
