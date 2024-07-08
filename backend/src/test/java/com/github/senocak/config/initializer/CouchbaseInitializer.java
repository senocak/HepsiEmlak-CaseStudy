package com.github.senocak.config.initializer;

import com.github.senocak.TestConstants;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import java.util.List;

@TestConfiguration
public class CouchbaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String SCOPE = "integration-scope";
    private static final BucketDefinition bucketDefinition = new BucketDefinition("mybucket")
            .withPrimaryIndex(true);

    private static final CouchbaseContainer couchbaseContainer = new CouchbaseContainer("couchbase/server:7.6.1")
            .withExposedPorts(8091, 8092, 8093, 8094, 8095, 8096, 11210)
            .withCredentials("Administrator", "password")
            .withBucket(bucketDefinition)
            .withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT);

    static {
        couchbaseContainer.start();
    }

    private final HttpHeaders headers = new HttpHeaders();
    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    public CouchbaseInitializer() {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + couchbaseContainer.getConnectionString(),
                "spring.datasource.username=" + couchbaseContainer.getUsername(),
                "spring.datasource.password=" + couchbaseContainer.getPassword(),
                "spring.datasource.bucketName=" + bucketDefinition.getName(),
                "spring.datasource.scope=" + SCOPE,
                "spring.datasource.ddl=create"
        ).applyTo(configurableApplicationContext.getEnvironment());
        createScope();
        createCollection("user-collection");
        createCollection("todo-collection");
    }

    private void createScope() {
        testRestTemplate.withBasicAuth(couchbaseContainer.getUsername(), couchbaseContainer.getPassword())
                .postForEntity(
                        "http://" + couchbaseContainer.getHost() + ":" + couchbaseContainer.getBootstrapHttpDirectPort() + "/pools/default/buckets/" + bucketDefinition.getName() + "/scopes",
                        new HttpEntity<>("name=" + SCOPE, headers),
                        String.class
                );
    }

    private void createCollection(String name) {
        testRestTemplate.withBasicAuth(couchbaseContainer.getUsername(), couchbaseContainer.getPassword())
                .postForEntity(
                        "http://" + couchbaseContainer.getHost() + ":" + couchbaseContainer.getBootstrapHttpDirectPort() + "/pools/default/buckets/" + bucketDefinition.getName() + "/scopes/" + SCOPE + "/collections",
                        new HttpEntity<>("name=" + name, headers),
                        String.class
                );
    }
}