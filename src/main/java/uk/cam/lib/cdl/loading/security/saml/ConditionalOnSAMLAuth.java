package uk.cam.lib.cdl.loading.security.saml;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a Spring component as being active only when SAML Auth is enabled.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(prefix = "auth.saml", name="enabled", havingValue="true")
public @interface ConditionalOnSAMLAuth { }
