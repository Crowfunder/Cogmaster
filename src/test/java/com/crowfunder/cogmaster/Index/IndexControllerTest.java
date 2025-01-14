package com.crowfunder.cogmaster.Index;

import org.junit.jupiter.api.Nested;
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
    void RealConfigDeepParameter() throws Exception {
        String path = "Accessory/Armor/Aura/Snipe Aura, Cocoa";
        ResultActions result = mockMvc.perform(get("/api/v1/config/item?path={path}", path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path.path").value(path))
                .andExpect(jsonPath("$.parameters.hashMap.Colorizations.value.hashMap.entry.value.[0].value.hashMap.source.value.hashMap.colorization.value").value("1295"));
    }

    @Test
    void RealConfig() throws Exception {
        String path = "Block/Barbed Hedgehog";
        ResultActions result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path.path").value(path));

        // Path without forward slashes
        path = "Action";
        result = mockMvc.perform(get("/api/v1/config/effect?path={path}", path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.path.path").value(path));
    }

    @Test
    void FakeConfig() throws Exception {

        // Fake config
        String path = "Block/Fake Entry That Does Not Exist";
        ResultActions result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isNotFound());

        // Fake config without forward slashes
        path = "Fake Entry That Does Not Exist";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isNotFound());

        // Bad path with backslashes
        path = "Block\\Barbed Hedgehog";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));
        result.andExpect(status().isNotFound());
    }
}