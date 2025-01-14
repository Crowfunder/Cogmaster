package com.crowfunder.cogmaster.Index;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void resolveConfigByPath() throws Exception {

        // Real config 1
        String path = "Accessory/Armor/Aura/Snipe Aura, Cocoa";
        ResultActions result = mockMvc.perform(get("/api/v1/config/item?path={path}", path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path.path").value(path));

        // Real config 2
        // Deep parameters test
        path = "Block/Barbed Hedgehog";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path.path").value(path));
//                .andExpect(jsonPath("$.actor.name").value(""));

        // Fake config
        path = "Block/Fake Entry That Does Not Exist";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isNotFound());

        // Fake config without forward slashes
        path = "Fake Entry That Does Not Exist";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isNotFound());

        // Path without forward slashes
        path = "Action";
        result = mockMvc.perform(get("/api/v1/config/effect?path={path}", path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path.path").value(path));

        // Bad path with backslashes
        path = "Block\\Barbed Hedgehog";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isNotFound());
    }
}