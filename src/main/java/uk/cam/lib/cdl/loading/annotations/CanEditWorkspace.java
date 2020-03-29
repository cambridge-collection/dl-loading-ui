package uk.cam.lib.cdl.loading.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@PreAuthorize("@roleService.canEditWorkspace(#workspaceIds, authentication)")
public @interface CanEditWorkspace {

}

