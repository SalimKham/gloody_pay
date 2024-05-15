package com.zsoft.gloody.gloodypayroll.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {
  /*  @Before("execution(* com.zsoft.gloody.gloodypayroll.services..*(..)) && target(object) ")
    public void aroundExecution(JoinPoint pjp, Object object) throws Throwable {
        if (object instanceof TenantSupportService) {
            Session session = ((TenantSupportService) object).getSession();
            if (session != null && getCurrentTenant() != null) {
                org.hibernate.Filter filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", getCurrentTenant());
                filter.validate();
            }
        }
    }

    private Long getCurrentTenant() {
        return SecurityUtils.getCurrentTenantId();
    }*/
}
