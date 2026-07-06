package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.aiverse.common.response.PageInfo;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.AssetListResponse;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.service.AssetService;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @Mock
    private AssetService assetService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AssetController(assetService)).build();
    }

    @Test
    void 콘텐츠_목록을_검색해_페이지로_반환한다() throws Exception {
        var item = new AssetListResponse(
                1L, "제목", "설명", AssetType.IMAGE, 1L, "preview/key",
                100, "tool", LicenseType.PERSONAL, 3, 2L, "creator", LocalDateTime.now()
        );
        given(assetService.search(any(), eq(0), eq(20)))
                .willReturn(new PageResponse<>(List.of(item), new PageInfo(0, 20, 1, 1, false)));

        mockMvc.perform(get("/api/contents")
                        .param("search", "제목")
                        .param("sort", "POPULAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("제목"))
                .andExpect(jsonPath("$.data[0].creatorNickname").value("creator"))
                .andExpect(jsonPath("$.data[0].tags").doesNotExist())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
