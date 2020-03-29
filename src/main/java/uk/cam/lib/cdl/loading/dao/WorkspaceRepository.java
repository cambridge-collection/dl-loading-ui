package uk.cam.lib.cdl.loading.dao;

import org.springframework.data.repository.CrudRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;

import java.util.List;


public interface WorkspaceRepository extends CrudRepository<Workspace, Long> {

    Workspace findWorkspaceById(long workspaceId);
    List<Workspace> findWorkspaceByCollectionIds(String collectionId);
}
