package example;

/**
 * Created by davidsu on 17/03/2017.
 */
import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.HashMap;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
//http://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey
public class SessionHandler implements ContainerRequestFilter {
    //todo implement the whole thing - add active user on login, provide a logout, verify active user upon request...

    private static HashMap<String, ActiveUser> activeUsers = new HashMap<>();
    private static SecureRandom random = new SecureRandom();

    public static void init(){
        //todo kick a thread that will periodically remove expired users from activeUsers
        //I guess we will need some write lock for thread safety on this hashMap but need to ask google
        //about hashMaps and multithreading
        activeUsers.put("testuser", new ActiveUser("dummy@email", true));
    }

    public static void addActiveUser(String token, String email, String type){
        activeUsers.put(token, new ActiveUser(email, type.equals("admin")));
    }

    public static String nextSessionId() {
        //todo certify that return value isn't present in "activeUsers"
        return new BigInteger(130, random).toString(32);
    }

    private ActiveUser getActiveUserForToken(String token){
        ActiveUser user = activeUsers.get(token);
        if(user == null || user.isExpired()){
            return null;
        }
        return user;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Get the HTTP Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();
        ActiveUser user = getActiveUserForToken(token);

        if(user == null){
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }
        requestContext.setSecurityContext(new InternalSecurityContext(user));
    }
}