package com.maleyk.flow_manager;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
 class FileControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Test
    void status_shouldReturn404_whenRecordDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/files/{id}/status", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_shouldReturn409_whenFileNotYetConverted() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pending.txt", "text/plain", "not ready yet".getBytes());

        String response = mockMvc.perform(multipart("/api/files").file(file))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/files/{id}/file", id))
                .andExpect(status().isConflict());
    }
}
