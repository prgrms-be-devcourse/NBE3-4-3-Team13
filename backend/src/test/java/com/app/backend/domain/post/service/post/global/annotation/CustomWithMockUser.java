package com.app.backend.domain.post.service.post.global.annotation;

import com.app.backend.domain.post.service.post.global.security.CustomWithSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomWithSecurityContextFactory.class)
public @interface CustomWithMockUser {

    long id() default 1L;

    String username() default "testUsername";

    String password() default "testPassword";

    String nickname() default "testNickname";

    String role() default "ROLE_USER";

}
