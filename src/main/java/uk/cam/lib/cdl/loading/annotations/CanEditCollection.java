package uk.cam.lib.cdl.loading.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@PreAuthorize("@roleService.canEditCollection(#collectionId, authentication) or " +
    "(#collectionForm!=null and @roleService.canEditCollection(#collectionForm.collectionId, authentication)) or " +
    "@roleService.canEditWorkspace(#workspaceIds, authentication)")
public @interface CanEditCollection {

}

