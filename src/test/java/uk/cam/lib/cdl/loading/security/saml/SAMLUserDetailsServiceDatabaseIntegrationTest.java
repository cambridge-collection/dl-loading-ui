package uk.cam.lib.cdl.loading.security.saml;

//        // TODO fix issue - fails when connecting to auth.saml.keycloak.auth-server-url
// after upgrading spring / h2 / flyway

/**
 * An integration test to verify that SAML authentiation is able to locate users
 * stored in the database when users.source is database.
// */
//@SpringBootTest(properties = {
//    "auth.methods=saml",
//    "users.source=database"
//})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class SAMLUserDetailsServiceDatabaseIntegrationTest {
//    @Autowired
//    private SAMLUserDetailsService samlUserDetailsService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User user;
//
//    @BeforeEach
//    private void generateUsers() {
//        // Generate a fake user to be retrieved
//        user = new User();
//        user.setUsername("example");
//        user.setPassword("");
//        user.setFirstName("Foo");
//        user.setLastName("");
//        user.setEmail("");
//        user.setAuthorities(ImmutableList.of("ROLE_USER"));
//
//        userRepository.save(user);
//    }
//
//    @Test
//    public void samlUserDetailsServiceKnowsAboutUsersInDatabase() {
//        var result = samlUserDetailsService.loadUserBySAML(samlCredentialWithID("example"));
//
//        assertThat(result).isInstanceOf(MyUserDetails.class);
//        var userDetails = (MyUserDetails)result;
//        assertThat(userDetails.getUser().getUsername()).isEqualTo("example");
//        assertThat(userDetails.getUser().getFirstName()).isEqualTo("Foo");
//    }
//}
