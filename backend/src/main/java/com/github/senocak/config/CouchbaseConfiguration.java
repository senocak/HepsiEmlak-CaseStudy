package com.github.senocak.config;

import com.couchbase.client.core.error.BucketNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.ClusterEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

@Configuration
@EnableCouchbaseRepositories
public class CouchbaseConfiguration extends AbstractCouchbaseConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CouchbaseConfiguration.class);
    private final DataSourceConfig dataSourceConfig;

    public CouchbaseConfiguration(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public String getConnectionString() {
        return dataSourceConfig.getUrl();
    }
    public String getUserName() {
        return dataSourceConfig.getUsername();
    }
    public String getPassword() {
        return dataSourceConfig.getPassword();
    }
    public String getBucketName() {
        return dataSourceConfig.getBucketName();
    }
//    public String typeKey(): String = "type"
    public String getScopeName() {
        return dataSourceConfig.getScope();
    }


    @Bean(destroyMethod = "disconnect")
    public Cluster couchbaseCluster(ClusterEnvironment couchbaseClusterEnvironment) {
        try {
            log.info("Connecting to Couchbase cluster at ");
            return Cluster.connect(getConnectionString(), getUserName(), getPassword());
        } catch (Exception e) {
            log.error("Error connecting to Couchbase cluster: {}", e.getMessage());
            throw e;
        }
    }

    @Bean
    public Bucket getCouchbaseBucket(Cluster cluster) {
        try {
            if (!cluster.buckets().getAllBuckets().containsKey(getBucketName())) {
                log.error("Bucket with name {} does not exist. Creating it now.", getBucketName());
                throw new BucketNotFoundException(getBucketName());
            }
            return cluster.bucket(getBucketName());
        } catch (Exception e) {
            log.error("Error getting bucket, {}", e.getMessage());
            throw e;
        }
    }
}