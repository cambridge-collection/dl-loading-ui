package uk.cam.lib.cdl.loading.usermanagment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.security.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void userRepository_saveAndRetrieveUser() {

        User user = new User();
        user.setUsername("TestUsername");
        user.setFirstName("TestFirst");
        user.setLastName("TestLast");
        user.setEmail("Test@emailaddress.com");
        user.setPassword("testpass");
        user.setEnabled(true);

        userRepository.save(user);
        User foundEntity = userRepository.findByUsername(user.getUsername());

        assertNotNull(foundEntity);
        assertEquals(user.getUsername(), foundEntity.getUsername());
    }
}
