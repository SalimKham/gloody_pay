package com.zsoft.gloody.gloodypayroll.services;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TenantSupportService extends EntityManagerService {
    public void disableFilters() {
        Session session = getSession();
        if (Objects.nonNull(session)) {
            session.disableFilter("tenantFilter");
        }
    }
}
