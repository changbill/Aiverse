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

import com.example.aiverse.dto.CreditProductResponse;
import com.example.aiverse.service.CreditProductService;

@ExtendWith(MockitoExtension.class)
class CreditProductControllerTest {

    @Mock
    private CreditProductService creditProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CreditProductController(creditProductService)).build();
    }

    @Test
    void 충전_상품_목록을_반환한다() throws Exception {
        given(creditProductService.getActiveProducts()).willReturn(List.of(
                new CreditProductResponse(1L, "BASIC", "Basic", 500, 0, 5000, 1),
                new CreditProductResponse(2L, "PLUS", "Plus", 1000, 100, 10000, 2)
        ));

        mockMvc.perform(get("/api/credit-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("BASIC"))
                .andExpect(jsonPath("$.data[1].bonusCredit").value(100));
    }
}
