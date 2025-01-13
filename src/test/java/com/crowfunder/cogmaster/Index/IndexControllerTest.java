package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void resolveConfigByPath() throws Exception {

        String path = "Accessory/Armor/Aura/Snipe Aura, Cocoa";
        String expectedResult = "{\"implementationType\":\"com.threerings.projectx.item.config.ItemConfig$Derived\",\"derivedImplementationType\":\"com.threerings.projectx.item.config.ItemConfig$AccessoryTicket\",\"path\":{\"path\":\"Accessory/Armor/Aura/Snipe Aura, Cocoa\",\"nextPath\":\"Accessory\"},\"derivedPath\":{\"path\":\"Accessory/Parts/Base, Custom Colors\",\"nextPath\":\"Accessory\"},\"sourceConfig\":\"item\",\"parameters\":{\"empty\":false,\"hashMap\":{\"icon\":{\"value\":{\"empty\":false,\"hashMap\":{\"file\":{\"value\":\"ui/icon/inventory/icon_accessory-afront.png\",\"nested\":false}}},\"nested\":true},\"stackable\":{\"value\":\"true\",\"nested\":false},\"Accessory\":{\"value\":{\"implementationType\":\"com.threerings.config.ConfigReference\",\"sourceConfig\":\"item\",\"parameters\":{\"empty\":true,\"hashMap\":{}},\"path\":{\"path\":\"Armor/Aura/Snipe Aura, Cocoa\",\"nextPath\":\"Armor\"}},\"nested\":false},\"Icon\":{\"value\":\"ui/icon/inventory/icon_accessory-aaura_cm.png\",\"nested\":false},\"Colorizations\":{\"value\":{\"empty\":false,\"hashMap\":{\"entry\":{\"value\":[{\"value\":{\"empty\":false,\"hashMap\":{\"source\":{\"value\":{\"empty\":false,\"hashMap\":{\"colorization\":{\"value\":\"1295\",\"nested\":false}}},\"nested\":true},\"clazz\":{\"value\":\"11\",\"nested\":false}}},\"nested\":true},{\"value\":{\"empty\":false,\"hashMap\":{\"colorization\":{\"value\":\"3114\",\"nested\":false}}},\"nested\":true},{\"value\":{\"empty\":false,\"hashMap\":{\"colorization\":{\"value\":\"3370\",\"nested\":false}}},\"nested\":true}],\"nested\":false}}},\"nested\":true},\"rarity\":{\"value\":\"-1\",\"nested\":false}}},\"derivedParameters\":{\"empty\":false,\"hashMap\":{\"icon\":{\"value\":{\"empty\":false,\"hashMap\":{\"file\":{\"value\":\"ui/icon/inventory/icon_accessory-afront.png\",\"nested\":false}}},\"nested\":true},\"stackable\":{\"value\":\"true\",\"nested\":false},\"rarity\":{\"value\":\"-1\",\"nested\":false}}},\"derived\":true,\"ownParameters\":{\"empty\":false,\"hashMap\":{\"Accessory\":{\"value\":{\"implementationType\":\"com.threerings.config.ConfigReference\",\"sourceConfig\":\"item\",\"parameters\":{\"empty\":true,\"hashMap\":{}},\"path\":{\"path\":\"Armor/Aura/Snipe Aura, Cocoa\",\"nextPath\":\"Armor\"}},\"nested\":false},\"Icon\":{\"value\":\"ui/icon/inventory/icon_accessory-aaura_cm.png\",\"nested\":false},\"Colorizations\":{\"value\":{\"empty\":false,\"hashMap\":{\"entry\":{\"value\":[{\"value\":{\"empty\":false,\"hashMap\":{\"source\":{\"value\":{\"empty\":false,\"hashMap\":{\"colorization\":{\"value\":\"1295\",\"nested\":false}}},\"nested\":true},\"clazz\":{\"value\":\"11\",\"nested\":false}}},\"nested\":true},{\"value\":{\"empty\":false,\"hashMap\":{\"colorization\":{\"value\":\"3114\",\"nested\":false}}},\"nested\":true},{\"value\":{\"empty\":false,\"hashMap\":{\"colorization\":{\"value\":\"3370\",\"nested\":false}}},\"nested\":true}],\"nested\":false}}},\"nested\":true}}}}";
        ResultActions result = mockMvc.perform(get("/api/v1/config/item?path={path}", path));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedResult));

        path = "Block/Barbed Hedgehog";
        result = mockMvc.perform(get("/api/v1/config/actor?path={path}", path));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        MvcResult results = result.andReturn();
        ConfigEntry deserialized = objectMapper.readValue(results.getResponse().getContentAsString(), ConfigEntry.class);

        assert(deserialized.getOwnParameters().resolveParameterPath(new Path("Variant")).isNested());

    }
}