package ee.hm.dop.rest.filter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import ee.hm.dop.model.AuthenticatedUser;
import ee.hm.dop.model.User;
import ee.hm.dop.service.useractions.AuthenticatedUserService;
import ee.hm.dop.service.login.LogoutService;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class SecurityFilterTest {

    private static final int HTTP_AUTHENTICATION_TIMEOUT = 419;

    private SecurityFilter filter;
    private UriInfo uriInfo;
    private HttpServletRequest request;
    private HttpSession session;
    private ContainerRequestContext context;
    private Capture<SecurityContext> capturedSecurityContext;
    private Capture<Response> capturedResponse;
    private AuthenticatedUserService authenticatedUserService;

    @Before
    public void setup() throws NoSuchMethodException {
        request = createMock(HttpServletRequest.class);
        session = createMock(HttpSession.class);
        authenticatedUserService = createMock(AuthenticatedUserService.class);

        context = createMock(ContainerRequestContext.class);
        capturedSecurityContext = newCapture();
        capturedResponse = newCapture();

        uriInfo = createMock(UriInfo.class);
        filter = new SecurityFilterMock();
    }

    @Test
    public void filterNoTokenInRequest() throws IOException {
        expect(context.getHeaderString("Authentication")).andReturn(null);

        replay(uriInfo, request, session, context);
        filter.filter(context);
        verify(uriInfo, request, session, context);

    }

    @Test
    public void filterNoUserWithRecievedToken() throws IOException {
        String token = "token";

        expect(context.getHeaderString("Authentication")).andReturn(token);
        expect(authenticatedUserService.getAuthenticatedUserByToken(token)).andReturn(null);
        context.abortWith(EasyMock.capture(capturedResponse));

        replay(uriInfo, request, session, context, authenticatedUserService);
        filter.filter(context);
        verify(uriInfo, request, session, context, authenticatedUserService);

        assertEquals(HTTP_AUTHENTICATION_TIMEOUT, capturedResponse.getValue().getStatus());
    }

    @Test
    public void filterWrongUsernameInHeader() throws IOException {
        String token = "token";
        AuthenticatedUser authenticatedUser = createMock(AuthenticatedUser.class);
        User user = createMock(User.class);
        context.abortWith(EasyMock.capture(capturedResponse));

        setExpects(token, authenticatedUser, user, "wrongUsername", false);

        replay(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

        filter.filter(context);

        verify(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

        assertEquals(HTTP_AUTHENTICATION_TIMEOUT, capturedResponse.getValue().getStatus());
    }

    @Test
    public void filter() throws IOException {
        String token = "token";
        AuthenticatedUser authenticatedUser = createMock(AuthenticatedUser.class);
        User user = createMock(User.class);
        context.setSecurityContext(EasyMock.capture(capturedSecurityContext));

        setExpects(token, authenticatedUser, user, "realUsername", true);

        replay(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

        filter.filter(context);

        verify(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

    }

    @Test
    public void filterIsNotSecure() throws IOException, URISyntaxException {
        String token = "token";
        AuthenticatedUser authenticatedUser = createMock(AuthenticatedUser.class);
        User user = createMock(User.class);
        context.setSecurityContext(EasyMock.capture(capturedSecurityContext));

        setExpects(token, authenticatedUser, user, "realUsername", true);

        expect(uriInfo.getRequestUri()).andReturn(new URI("http://www.boo.com/foo/duuu"));

        replay(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);
        filter.filter(context);
        SecurityContext securityContext = capturedSecurityContext.getValue();
        assertFalse(securityContext.isSecure());

        verify(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

    }

    @Test
    public void filterIsSecure() throws IOException, URISyntaxException {
        String token = "token";
        AuthenticatedUser authenticatedUser = createMock(AuthenticatedUser.class);
        User user = createMock(User.class);
        context.setSecurityContext(EasyMock.capture(capturedSecurityContext));

        setExpects(token, authenticatedUser, user, "realUsername", true);

        expect(uriInfo.getRequestUri()).andReturn(new URI("https://www.boo.com/foo/duuu"));

        replay(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);
        filter.filter(context);
        SecurityContext securityContext = capturedSecurityContext.getValue();
        assertTrue(securityContext.isSecure());

        verify(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);
    }

    @Test
    public void filterAuthenticationScheme() throws IOException, URISyntaxException {
        String token = "token";
        AuthenticatedUser authenticatedUser = createMock(AuthenticatedUser.class);
        User user = createMock(User.class);
        context.setSecurityContext(EasyMock.capture(capturedSecurityContext));

        setExpects(token, authenticatedUser, user, "realUsername", true);

        replay(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);
        filter.filter(context);
        SecurityContext securityContext = capturedSecurityContext.getValue();
        assertEquals(SecurityContext.CLIENT_CERT_AUTH, securityContext.getAuthenticationScheme());

        verify(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

    }

    @Test
    public void filterSessionTimeout() throws IOException, URISyntaxException {
        String token = "token";
        AuthenticatedUser authenticatedUser = createMock(AuthenticatedUser.class);
        User user = createMock(User.class);
        context.abortWith(EasyMock.capture(capturedResponse));

        expect(context.getHeaderString("Authentication")).andReturn(token);
        expect(authenticatedUserService.getAuthenticatedUserByToken(token)).andReturn(authenticatedUser);
        expect(authenticatedUser.getUser()).andReturn(user);
        expect(authenticatedUser.getLoginDate()).andReturn(DateTime.now().minusDays(1).minusSeconds(1));
        expect(context.getHeaderString("Username")).andReturn("realUsername");
        expect(user.getUsername()).andReturn("realUsername");

        replay(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);
        filter.filter(context);
        assertEquals(HTTP_AUTHENTICATION_TIMEOUT, capturedResponse.getValue().getStatus());

        verify(uriInfo, request, session, context, authenticatedUserService, authenticatedUser, user);

    }

    private void setExpects(String token, AuthenticatedUser authenticatedUser, User user, String returnedUser, boolean success) {
        expect(context.getHeaderString("Authentication")).andReturn(token);
        expect(authenticatedUserService.getAuthenticatedUserByToken(token)).andReturn(authenticatedUser);
        expect(authenticatedUser.getUser()).andReturn(user);
        expect(authenticatedUser.getLoginDate()).andReturn(LocalDateTime.now().toDateTime(DateTimeZone.UTC)).times(0, 1);
        expect(context.getHeaderString("Username")).andReturn(returnedUser);
        expect(user.getUsername()).andReturn("realUsername");
        if (success)
            expect(context.getUriInfo()).andReturn(uriInfo);
    }

    class SecurityFilterMock extends SecurityFilter {

        @Override
        protected AuthenticatedUserService authenticatedUserService() {
            return authenticatedUserService;
        }

    }
}
