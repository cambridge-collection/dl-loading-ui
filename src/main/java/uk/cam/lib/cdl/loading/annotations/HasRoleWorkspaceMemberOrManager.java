package uk.cam.lib.cdl.loading.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO fix hardcoded roles

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@PreAuthorize("@roleService.hasRoleRegex('ROLE_WORKSPACE_MEMBER\\d+', authentication) or " +
    "@roleService.hasRoleRegex('ROLE_WORKSPACE_MANAGER\\d+', authentication)")
public @interface HasRoleWorkspaceMemberOrManager {
}
