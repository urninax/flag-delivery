package me.urninax.flagdelivery.it.auth;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class AbstractAuthIT extends AbstractIntegrationTest {
    @Autowired
    protected TestRestTemplate template;

    protected HttpHeaders defaultHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    protected HttpHeaders authHeaders(String bearer){
        HttpHeaders headers = defaultHeaders();
        headers.set("Authorization", bearer);
        return headers;
    }

    protected ResponseEntity<?> sendCreateUserRequest(SignupRequest request, HttpHeaders headers){
        HttpEntity<SignupRequest> entity = new HttpEntity<>(request, headers);
        return template.postForEntity("/api/v1/auth/signup", entity, Object.class);
    }

    protected ResponseEntity<?> sendSigninUserRequest(SigninRequest request, HttpHeaders headers){
        HttpEntity<SigninRequest> entity = new HttpEntity<>(request, headers);
        return template.postForEntity("/api/v1/auth/signin", entity, Object.class);
    }
}
