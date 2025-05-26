package org.finos.calm.security;

import org.finos.calm.domain.UserAccess;
import org.finos.calm.domain.UserAccess.Permission;
import org.finos.calm.domain.UserAccess.ResourceType;
import org.finos.calm.domain.exception.UserAccessNotFoundException;
import org.finos.calm.store.UserAccessStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestUserAccessValidatorShould {

    private UserAccessStore userAccessStore;
    private UserAccessValidator validator;

    @BeforeEach
    void setUp() {
        userAccessStore = mock(UserAccessStore.class);
        validator = new UserAccessValidator(userAccessStore);
    }

    @Test
    void return_true_when_user_has_sufficient_permissions() throws Exception {
        UserRequestAttributes requestAttributes = new UserRequestAttributes("GET", "testuser",
                "/calm/namespace/finos/patterns", "finos");
        UserAccess userAccess = new UserAccess("testuser", Permission.read, "finos", ResourceType.patterns);
        when(userAccessStore.getUserAccessForUsername("testuser"))
                .thenReturn(List.of(userAccess));

        boolean actual = validator.isUserAuthorized(requestAttributes);
        assertTrue(actual);
    }

    @Test
    void return_false_when_user_has_no_matching_permission() throws Exception {
        UserRequestAttributes requestAttributes = new UserRequestAttributes("GET", "testuser",
                "/calm/namespace/finos/patterns", "finos");
        UserAccess userAccess = new UserAccess("testuser", Permission.read, "workshop", ResourceType.patterns);
        when(userAccessStore.getUserAccessForUsername("testuser"))
                .thenReturn(List.of(userAccess));

        boolean actual = validator.isUserAuthorized(requestAttributes);
        assertFalse(actual);
    }

    @Test
    void return_true_when_user_has_write_permission() throws Exception {
        UserRequestAttributes requestAttributes = new UserRequestAttributes("GET", "testuser",
                "/calm/namespace/finos/patterns", "finos");
        UserAccess userAccess = new UserAccess("testuser", Permission.write, "finos", ResourceType.patterns);
        when(userAccessStore.getUserAccessForUsername("testuser"))
                .thenReturn(List.of(userAccess));

        boolean actual = validator.isUserAuthorized(requestAttributes);
        assertTrue(actual);
    }

    @Test
    void return_true_when_user_accessing_default_get_namespaces_endpoint() throws Exception {
        UserRequestAttributes requestAttributes = new UserRequestAttributes("GET", "testuser",
                "/calm/namespaces", null);

        boolean actual = validator.isUserAuthorized(requestAttributes);
        assertTrue(actual);
    }

    @Test
    void return_false_when_no_permissions_are_mapped_to_user() throws Exception {
        UserRequestAttributes requestAttributes = new UserRequestAttributes("GET", "testuser",
                "/calm/namespaces/test/finos", "finos");
        when(userAccessStore.getUserAccessForUsername("testuser"))
                .thenThrow(new UserAccessNotFoundException());

        boolean actual = validator.isUserAuthorized(requestAttributes);
        assertFalse(actual);
    }
}
