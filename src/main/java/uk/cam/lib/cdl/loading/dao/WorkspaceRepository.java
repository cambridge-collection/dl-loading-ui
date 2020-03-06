package uk.cam.lib.cdl.loading.dao;

import org.springframework.data.repository.CrudRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;


public interface WorkspaceRepository extends CrudRepository<Workspace, Long> {

    Workspace findWorkspaceById(long workspaceId);
}
