package com.innowise.orderservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.innowise.orderservice.domain.model.Item;
import com.innowise.orderservice.domain.port.out.ItemRepository;
import com.innowise.orderservice.domain.port.out.OrderRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@SpringBootTest(
        properties = {
                "application.security.jwksUrl=http://localhost:8090/.well-known/jwks.json",
                "application.userservice.getUserUriUnformatted=http://localhost:8090/api/v1/users/",
                "application.authService.getServiceAccessTokenUri=http://localhost:8090/api/v1/auth/service/authenticate"
        }
)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(classes = TestcontainersConfiguration.class)
class OrderControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static WireMockServer wireMockServer;
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static String testJwt;

    static {
        try {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8090));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8090);

        KeyPairGenerator keyGen = null;
            keyGen = KeyPairGenerator.getInstance("RSA");

        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        String jwksJson = new JWKSet(rsaKey).toString();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("1")
                .claim("login", "testuser")
                .claim("roles", List.of("USER"))
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims);

            signedJWT.sign(new RSASSASigner(privateKey));

        testJwt = signedJWT.serialize();

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/.well-known/jwks.json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jwksJson)));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/api/v1/users/\\d+"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":1,"firstName":"John","lastName":"Doe","email":"john@example.com","active":true}
                                """)));
        } catch (JOSEException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        Item item = new Item();
        item.setName("Test Item");
        item.setPrice(BigDecimal.valueOf(25.50));
        item = itemRepository.save(item);

        String requestJson = """
                {"items":[{"itemId":%d,"quantity":3}]}
                """.formatted(item.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userInfo.id").value(1))
                .andExpect(jsonPath("$.userInfo.firstName").value("John"))
                .andExpect(jsonPath("$.order.userId").value(1))
                .andExpect(jsonPath("$.order.status").value("CREATED"))
                .andExpect(jsonPath("$.order.items").isArray())
                .andExpect(jsonPath("$.order.items[0].itemId").value(item.getId()))
                .andExpect(jsonPath("$.order.items[0].quantity").value(3));
    }

    @Test
    void createOrder_shouldReturn400_whenItemsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    void createOrder_shouldReturn400_whenQuantityNull() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"itemId":1}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"itemId":1,"quantity":1}]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrderById_shouldReturnOrder() throws Exception {
        Item item = new Item();
        item.setName("Widget");
        item.setPrice(BigDecimal.valueOf(10.00));
        item = itemRepository.save(item);

        String createJson = """
                {"items":[{"itemId":%d,"quantity":2}]}
                """.formatted(item.getId());

        String responseContent = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode orderId = objectMapper.readTree(responseContent).path("order").path("id");

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId.asLong())
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId.asLong()))
                .andExpect(jsonPath("$.order.userId").value(1))
                .andExpect(jsonPath("$.userInfo.email").value("john@example.com"));
    }

    @Test
    void getOrderById_shouldReturn404_whenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{orderId}", 99999L)
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByUserId_shouldReturnOrders() throws Exception {
        Item item = new Item();
        item.setName("Item A");
        item.setPrice(BigDecimal.valueOf(5.00));
        item = itemRepository.save(item);

        String createJson = """
                {"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(item.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/{userId}/orders", 1L)
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].order.userId").value(1));
    }

    @Test
    void getOrdersByUserId_shouldReturn403_whenAccessingOtherUsersOrders() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/orders", 2L)
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderById_shouldUpdateOrder() throws Exception {
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setPrice(BigDecimal.valueOf(10.00));
        item1 = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setPrice(BigDecimal.valueOf(20.00));
        item2 = itemRepository.save(item2);

        String createJson = """
                {"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(item1.getId());

        String createResponse = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode orderId = objectMapper.readTree(createResponse).path("order").path("id");

        String updateJson = """
                {"items":[{"itemId":%d,"quantity":5}]}
                """.formatted(item2.getId());

        mockMvc.perform(put("/api/v1/orders/{orderId}", orderId.asLong())
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId.asLong()))
                .andExpect(jsonPath("$.order.items[0].itemId").value(item2.getId()))
                .andExpect(jsonPath("$.order.items[0].quantity").value(5));
    }

    @Test
    void updateOrderById_shouldReturn404_whenOrderNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/orders/{orderId}", 99999L)
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"itemId":1,"quantity":1}]}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrderById_shouldDeleteOrder() throws Exception {
        Item item = new Item();
        item.setName("Deletable Item");
        item.setPrice(BigDecimal.valueOf(15.00));
        item = itemRepository.save(item);

        String createJson = """
                {"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(item.getId());

        String createResponse = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode orderId = objectMapper.readTree(createResponse).path("order").path("id");

        mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId.asLong())
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId.asLong())
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrderById_shouldReturn404_whenOrderNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/{orderId}", 99999L)
                        .header("Authorization", "Bearer " + testJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_whenAccessingOtherUsersOrder() throws Exception {
        Item item = new Item();
        item.setName("Private Item");
        item.setPrice(BigDecimal.valueOf(100.00));
        item = itemRepository.save(item);

        String createJson = """
                {"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(item.getId());

        String createResponse = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode orderId = objectMapper.readTree(createResponse).path("order").path("id");

        String otherUserJwt = buildJwtForUser(999L, "otheruser");

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId.asLong())
                        .header("Authorization", "Bearer " + otherUserJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400_whenInvalidJsonBody() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + testJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not valid json"))
                .andExpect(status().isBadRequest());
    }

    private static String buildJwtForUser(Long userId, String login) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim("login", login)
                .claim("roles", List.of("USER"))
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        RSAKey rsaKey = new RSAKey.Builder(publicKey).build();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims);
        signedJWT.sign(new RSASSASigner(privateKey));
        return signedJWT.serialize();
    }
}
