package com.maleyk.flow_manager;

import com.maleyk.flow_manager.controller.FileController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.context.ApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlowManagerApplicationTests extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @LocalManagementPort
    private int managementPort;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
        assertThat(context.getBean(FileController.class)).isNotNull();
    }

    @Test
    void checkHealth() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + managementPort + "/actuator/health"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isBetween(200, 299);
    }
}
