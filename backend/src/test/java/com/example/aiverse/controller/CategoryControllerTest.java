package com.example.aiverse.controller;

import static org.mockito.BDDMockito.given;
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

import com.example.aiverse.dto.CategoryResponse;
import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService)).build();
    }

    @Test
    void 카테고리_목록을_반환한다() throws Exception {
        given(categoryService.getActiveCategories()).willReturn(List.of(
                new CategoryResponse(1L, CategoryName.NATURE, "nature", 1),
                new CategoryResponse(2L, CategoryName.PEOPLE, "people", 2)
        ));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("NATURE"))
                .andExpect(jsonPath("$.data[0].slug").value("nature"))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1));
    }
}
