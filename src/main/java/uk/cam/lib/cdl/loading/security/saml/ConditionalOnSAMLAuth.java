package uk.cam.lib.cdl.loading.security.saml;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a Spring component as being active only when SAML Auth is enabled.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnExpression("T(org.springframework.util.StringUtils).commaDelimitedListToSet(@environment.getProperty('auth.methods')).contains('saml')")
public @interface ConditionalOnSAMLAuth { }
