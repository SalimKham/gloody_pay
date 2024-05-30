package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CompanySupportService extends EntityManagerService {
    public void disableFilters() {
        Session session = getSession();
        if (Objects.nonNull(session)) {
            session.disableFilter("companyFilter");
        }
    }
    public void applyFilter() {
        Session session = getSession();
        if (session != null && SecurityUtils.getCurrentCompanyId() != null) {
            org.hibernate.Filter filter = session.enableFilter("companyFilter");
            filter.setParameter("companyId", SecurityUtils.getCurrentCompanyId());
            filter.validate();
        }
    }
}
