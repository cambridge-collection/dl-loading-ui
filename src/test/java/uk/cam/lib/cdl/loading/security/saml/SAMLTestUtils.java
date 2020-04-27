package uk.cam.lib.cdl.loading.security.saml;

import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SAMLTestUtils {
    private SAMLTestUtils() {}

    /**
     * Create a credential which returns the given ID value from {@code getNameID().getValue()}.
     */
    public static SAMLCredential samlCredentialWithID(String id) {
        var nameID = mock(NameID.class);
        when(nameID.getValue()).thenReturn("example");
        var credential = mock(SAMLCredential.class);
        when(credential.getNameID()).thenReturn(nameID);
        return credential;
    }
}
