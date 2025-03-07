package com.app.backend.global.security;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.annotation.CustomWithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomWithSecurityContextFactory implements WithSecurityContextFactory<CustomWithMockUser> {

    @Override
    public SecurityContext createSecurityContext(CustomWithMockUser annotation) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        MemberDetails memberDetails = new MemberDetails(new Member(annotation.id(),
                                                                   annotation.username(),
                                                                   annotation.password(),
                                                                   annotation.nickname(),
                                                                   Provider.LOCAL,
                                                                   null,
                                                                   annotation.role(),
                                                                   false));
        securityContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities())
        );

        return securityContext;
    }

}
