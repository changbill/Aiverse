package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AssetErrorCode;
import com.example.aiverse.common.error.GlobalExceptionHandler;
import com.example.aiverse.common.response.PageInfo;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.AssetDetailResponse;
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
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AssetController(assetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
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

    @Test
    void 콘텐츠_상세를_반환한다() throws Exception {
        given(assetService.getDetail(1L)).willReturn(new AssetDetailResponse(
                1L, "제목", "설명", AssetType.IMAGE, 4L, "preview/key.jpg", 120,
                "Midjourney", LicenseType.COMMERCIAL, 38L, 5L, "홍길동",
                List.of("cyberpunk", "city"), LocalDateTime.now(), null
        ));

        mockMvc.perform(get("/api/contents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.tags.length()").value(2))
                .andExpect(jsonPath("$.data.viewCount").value(38));
    }

    @Test
    void 존재하지_않는_콘텐츠는_404를_반환한다() throws Exception {
        given(assetService.getDetail(999L)).willThrow(new ApplicationException(AssetErrorCode.ASSET_NOT_FOUND));

        mockMvc.perform(get("/api/contents/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 콘텐츠_등록에_성공하면_201과_상세_정보를_반환한다() throws Exception {
        given(assetService.create(eq(5L), any())).willReturn(new AssetDetailResponse(
                1L, "제목", "설명", AssetType.IMAGE, 4L, "preview/key.jpg", 120,
                "Midjourney", LicenseType.COMMERCIAL, 0L, 5L, "홍길동",
                List.of("cyberpunk"), LocalDateTime.now(), null
        ));
        authenticateAs(5L);

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "제목",
                                  "description": "설명",
                                  "assetType": "IMAGE",
                                  "categoryId": 4,
                                  "originalObjectKey": "tmp/user-5/uuid/original.png",
                                  "originalFilename": "sunset.png",
                                  "contentType": "image/png",
                                  "fileSize": 1000000,
                                  "priceCredit": 120,
                                  "aiTool": "Midjourney",
                                  "licenseType": "COMMERCIAL",
                                  "tags": ["cyberpunk"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("제목"));
    }

    @Test
    void 콘텐츠_수정에_성공하면_수정된_정보를_반환한다() throws Exception {
        given(assetService.update(eq(5L), eq(1L), any())).willReturn(new AssetDetailResponse(
                1L, "수정된 제목", "설명", AssetType.IMAGE, 4L, "preview/key.jpg", 200,
                "Midjourney", LicenseType.COMMERCIAL, 0L, 5L, "홍길동",
                List.of("night"), LocalDateTime.now(), LocalDateTime.now()
        ));
        authenticateAs(5L);

        mockMvc.perform(put("/api/contents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "수정된 제목",
                                  "priceCredit": 200,
                                  "tags": ["night"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.priceCredit").value(200));
    }

    @Test
    void 소유자가_아닌_수정_요청은_403을_반환한다() throws Exception {
        willThrow(new ApplicationException(AssetErrorCode.FORBIDDEN))
                .given(assetService).update(eq(5L), eq(1L), any());
        authenticateAs(5L);

        mockMvc.perform(put("/api/contents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"수정\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 콘텐츠_삭제에_성공하면_204를_반환한다() throws Exception {
        authenticateAs(5L);

        mockMvc.perform(delete("/api/contents/1"))
                .andExpect(status().isNoContent());

        verify(assetService).delete(5L, 1L);
    }
}
