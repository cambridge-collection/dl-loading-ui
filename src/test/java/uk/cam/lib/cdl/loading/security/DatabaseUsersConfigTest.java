package uk.cam.lib.cdl.loading.security;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootTest(properties = {
    "users.source=database"
})
public class DatabaseUsersConfigTest {

    @Autowired
    @Qualifier(UsersConfig.USER_DETAILS_SERVICE)
    private UserDetailsService userDetailsService;

    @Test
    public void userDetailsServiceIsDatabaseUserDetailsService() {
        Truth.assertThat(userDetailsService).isInstanceOf(DBUserDetailsService.class);
    }
}
