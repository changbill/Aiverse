package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.aiverse.dto.TagResponse;
import com.example.aiverse.service.TagService;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TagController(tagService)).build();
    }

    @Test
    void 태그_목록을_반환한다() throws Exception {
        given(tagService.search(any(), any())).willReturn(List.of(new TagResponse(1L, "city", 5L)));

        mockMvc.perform(get("/api/tags").param("query", "cit").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("city"))
                .andExpect(jsonPath("$.data[0].usageCount").value(5));

        verify(tagService).search(eq("cit"), eq(10));
    }

    @Test
    void 쿼리와_limit_없이_호출하면_null로_전달된다() throws Exception {
        given(tagService.search(isNull(), isNull())).willReturn(List.of());

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk());

        verify(tagService).search(isNull(), isNull());
    }
}
