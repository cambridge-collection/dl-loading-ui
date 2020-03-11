package uk.cam.lib.cdl.loading.dao;

import org.springframework.data.repository.CrudRepository;
import uk.cam.lib.cdl.loading.model.security.User;


public interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String username);
    User findById(long id);
}
