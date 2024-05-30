package com.zsoft.gloody.gloodypayroll.aspects;

import com.zsoft.gloody.gloodypayroll.services.CompanySupportService;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CompanyAspect {
   @Before("execution(* com.zsoft.gloody.gloodypayroll.services..*(..)) && target(object) ")
    public void aroundExecution(JoinPoint pjp, Object object) throws Throwable {
        if (object instanceof CompanySupportService) {
            ((CompanySupportService) object).applyFilter();
        }
    }
}
