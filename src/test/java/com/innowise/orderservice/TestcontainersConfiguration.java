package com.innowise.orderservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration(proxyBeanMethods = false)
@TestPropertySource(locations = "classpath:application-test.yaml")
@Testcontainers
public class TestcontainersConfiguration {


        @Bean
        Network network() {
            return Network.newNetwork();
        }

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgresOrderServiceContainer(Network network) {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("order_service_db")
                    .withUsername("myuser")
                    .withPassword("secret")
                    .withNetwork(network)
                    .withNetworkAliases("orderservice_postgres");
        }

        @Bean
        PostgreSQLContainer<?> postgresAuthServiceContainer(Network network) {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("auth_service_db")
                    .withUsername("myuser")
                    .withPassword("secret")
                    .withNetwork(network)
                    .withNetworkAliases("authservice_postgres");
        }

        @Bean
        PostgreSQLContainer<?> postgresUserServiceContainer(Network network) {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("user_service_db")
                    .withUsername("myuser")
                    .withPassword("secret")
                    .withNetwork(network)
                    .withNetworkAliases("userservice_postgres");
        }

        @Bean
        GenericContainer<?> redisContainer(Network network) {
            return new GenericContainer<>(DockerImageName.parse("redis:latest"))
                    .withExposedPorts(6379)
                    .withNetwork(network)
                    .withNetworkAliases("redis");
        }

        @Bean
        GenericContainer<?> authServiceContainer(Network network, PostgreSQLContainer<?> postgresAuthServiceContainer) {
            return new GenericContainer<>(DockerImageName.parse("ghcr.io/absolute-in-doubt/authsvc:latest"))
                    .withExposedPorts(8081)
                    .withNetwork(network)
                    .withNetworkAliases("authservice")
                    .withEnv("POSTGRES_URL", "jdbc:postgresql://authservice_postgres:5432/auth_service_db")
                    .withEnv("POSTGRES_PASSWORD", "secret")
                    .withEnv("POSTGRES_USER", "myuser")
                    .dependsOn(postgresAuthServiceContainer);
        }

        @Bean
        GenericContainer<?> userServiceContainer(
                Network network,
                PostgreSQLContainer<?> postgresUserServiceContainer,
                GenericContainer<?> redisContainer,
                GenericContainer<?> authServiceContainer) {
            return new GenericContainer<>(DockerImageName.parse("ghcr.io/absolute-in-doubt/usrsvc:latest"))
                    .withExposedPorts(8080)
                    .withNetwork(network)
                    .withNetworkAliases("userservice")
                    .withEnv("POSTGRES_URL", "jdbc:postgresql://userservice_postgres:5432/user_service_db")
                    .withEnv("POSTGRES_PASSWORD", "secret")
                    .withEnv("POSTGRES_USER", "myuser")
                    .withEnv("REDIS_HOST", "redis")
                    .withEnv("REDIS_PORT", "6379")
                    .withEnv("JWKS_URL", "http://authservice:8081/.well-known/jwks.json")
                    .dependsOn(postgresUserServiceContainer, redisContainer, authServiceContainer);
        }
    }
