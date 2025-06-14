package com.crowfunder.cogmaster.Translations;

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
class TranslationsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void getValue() throws Exception {
                // item.properties
                String path = "/api/v1/translations/key?q=m.oni_helm";
                ResultActions result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"Oni Helm"}"""));

                // design.properties
                path = "/api/v1/translations/key?q=e.location_already_placed";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"A room has already been installed at this location."}"""));
        }

        @Test
        void getValueComplex() throws Exception {
                // Needs recursive resolving
                String path = "/api/v1/translations/key?q=a.shd|a.sniped\\!m.buccaneer_bicorne";
                ResultActions result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"Shadow Sniped Buccaneer Bicorne"}"""));

                // Multiple arguments into 1 key, one of args tainted
                path = "/api/v1/translations/key?q=m.prize_box_seq|p.anniv|\\~2013";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"Anniversary Prize Box 2013"}"""));
        }

        @Test
        void getValueComplexFail() throws Exception {
                // Key does not exist
                String path = "/api/v1/translations/key?q=m.thisdoesnotexisthonestly|p.anniv|\\~2013";
                ResultActions result = mockMvc.perform(get(path));
                result.andExpect(status().isNotFound());

                // Provide less args than key holds
                path = "/api/v1/translations/key?q=m.prize_box_seq|p.anniv";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"Anniversary Prize Box {1}"}"""));

                // Provide more args than key holds
                path = "/api/v1/translations/key?q=m.prize_box_seq|p.anniv|\\~2013|\\~test";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"Anniversary Prize Box 2013"}"""));

                // Arg does not exist
                path = "/api/v1/translations/key?q=m.prize_box_seq|p.annivasd|\\~2013";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.valueOf("application/json")))
                                .andExpect(content().string("""
                                                {"value":"(Invalid Key) Prize Box 2013"}"""));
        }

        @Test
        void getValueFail() throws Exception {
                // Query by value instead of key
                String path = "/api/v1/translations/key?q=Oni Helm";
                ResultActions result = mockMvc.perform(get(path));
                result.andExpect(status().isNotFound());

                path = "/api/v1/translations/key?q=completelybollocksfakevalue";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isNotFound());
        }

        @Test
        void getKey() throws Exception {
                // Single entry
                String path = "/api/v1/translations/value?q=Brandish";
                ResultActions result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.[0].value").value("m.brandish"));

                // Two entries
                path = "/api/v1/translations/value?q=Sputterspark";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.[1].value").value("n.great_escape"));
        }

        @Test
        void getKeyFail() throws Exception {
                // Input key instead of value
                String path = "/api/v1/translations/value?q=m.brandish";
                ResultActions result = mockMvc.perform(get(path));
                result.andExpect(status().isNotFound());

                path = "/api/v1/translations/value?q=completelybollocksfakevalue";
                result = mockMvc.perform(get(path));
                result.andExpect(status().isNotFound());
        }
}