package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.aiverse.common.response.PageInfo;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.LibraryAssetResponse;
import com.example.aiverse.dto.LibraryItemResponse;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.service.LibraryService;

@ExtendWith(MockitoExtension.class)
class LibraryControllerTest {

    @Mock
    private LibraryService libraryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LibraryController(libraryService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test
    void 보관함을_페이지로_반환한다() throws Exception {
        var item = new LibraryItemResponse(
                55L, new LibraryAssetResponse(1L, "제목", "preview/key.jpg", AssetType.IMAGE, false),
                120, LicenseType.COMMERCIAL, LocalDateTime.now()
        );
        given(libraryService.getLibrary(eq(5L), eq(0), eq(20)))
                .willReturn(new PageResponse<>(List.of(item), new PageInfo(0, 20, 1, 1, false)));
        authenticateAs(5L);

        mockMvc.perform(get("/api/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].purchaseId").value(55))
                .andExpect(jsonPath("$.data[0].asset.title").value("제목"))
                .andExpect(jsonPath("$.data[0].asset.deleted").value(false));
    }
}
