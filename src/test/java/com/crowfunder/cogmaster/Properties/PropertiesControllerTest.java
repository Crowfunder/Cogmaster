package com.crowfunder.cogmaster.Properties;

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
class PropertiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getValue() throws Exception {
        // item.properties
        String path = "/api/v1/properties/key?q=m.oni_helm";
        ResultActions result = mockMvc.perform(get(path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("Oni Helm"));

        // design.properties
        path = "/api/v1/properties/key?q=e.location_already_placed";
        result = mockMvc.perform(get(path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("A room has already been installed at this location."));
    }

    @Test
    void getValueFail() throws Exception {
        // Query by value instead of key
        String path = "/api/v1/properties/key?q=Oni Helm";
        ResultActions result = mockMvc.perform(get(path));
        result.andExpect(status().isNotFound());

        path = "/api/v1/properties/key?q=completelybollocksfakevalue";
        result = mockMvc.perform(get(path));
        result.andExpect(status().isNotFound());
    }

    @Test
    void getKey() throws Exception {
        // Single entry
        String path = "/api/v1/properties/value?q=Brandish";
        ResultActions result = mockMvc.perform(get(path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0]").value("m.brandish"));

        // Two entries
        path = "/api/v1/properties/value?q=Sputterspark";
        result = mockMvc.perform(get(path));
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[1]").value("n.great_escape"));

    }

    @Test
    void getKeyFail() throws Exception {
        // Input key instead of value
        String path = "/api/v1/properties/value?q=m.brandish";
        ResultActions result = mockMvc.perform(get(path));
        result.andExpect(status().isNotFound());

        path = "/api/v1/properties/value?q=completelybollocksfakevalue";
        result = mockMvc.perform(get(path));
        result.andExpect(status().isNotFound());
    }
}