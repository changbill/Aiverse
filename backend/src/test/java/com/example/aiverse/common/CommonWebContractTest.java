package com.example.aiverse.common;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.ErrorCode;
import com.example.aiverse.common.error.GlobalExceptionHandler;
import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.common.web.RequestIdFilter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class CommonWebContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ContractController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .addFilters(new RequestIdFilter())
                .build();
    }

    @Test
    void wrapsSingleResponseWithData() throws Exception {
        mockMvc.perform(get("/contract/single"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("sample"));
    }

    @Test
    void wrapsPageResponseWithPageMetadata() throws Exception {
        mockMvc.perform(get("/contract/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.page.hasNext").value(true));
    }

    @Test
    void returnsFieldErrorsForInvalidRequest() throws Exception {
        mockMvc.perform(post("/contract/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("이름은 필수입니다."))
                .andExpect(jsonPath("$.requestId", matchesPattern("[0-9a-f-]{36}")));
    }

    @Test
    void returnsMalformedRequestForUnreadableJson() throws Exception {
        mockMvc.perform(post("/contract/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(0)));
    }

    @Test
    void returnsMethodNotAllowedForUnsupportedMethod() throws Exception {
        mockMvc.perform(put("/contract/single"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void returnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/contract/validate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void mapsApplicationExceptionToItsErrorCode() throws Exception {
        mockMvc.perform(get("/contract/application-error"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TEST_CONFLICT"))
                .andExpect(jsonPath("$.message").value("테스트 충돌입니다."))
                .andExpect(jsonPath("$.fieldErrors", hasSize(0)));
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        mockMvc.perform(get("/contract/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive"))));
    }

    @Test
    void reusesClientRequestIdInHeaderAndErrorBody() throws Exception {
        mockMvc.perform(get("/contract/application-error")
                        .header(RequestIdFilter.HEADER_NAME, "client-request-123"))
                .andExpect(status().isConflict())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, "client-request-123"))
                .andExpect(jsonPath("$.requestId").value("client-request-123"));
    }

    @Test
    void generatesRequestIdWhenClientValueIsInvalid() throws Exception {
        mockMvc.perform(get("/contract/single")
                        .header(RequestIdFilter.HEADER_NAME, "x".repeat(129)))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        RequestIdFilter.HEADER_NAME,
                        matchesPattern("[0-9a-f-]{36}")
                ));
    }

    @RestController
    @RequestMapping("/contract")
    static class ContractController {

        @GetMapping("/single")
        ApiResponse<Sample> single() {
            return ApiResponse.of(new Sample(1L, "sample"));
        }

        @GetMapping("/page")
        PageResponse<Sample> page() {
            PageImpl<Sample> page = new PageImpl<>(
                    List.of(new Sample(1L, "first"), new Sample(2L, "second")),
                    PageRequest.of(0, 2),
                    3
            );
            return PageResponse.from(page);
        }

        @PostMapping("/validate")
        ApiResponse<String> validate(@Valid @RequestBody CreateRequest request) {
            return ApiResponse.of(request.name());
        }

        @GetMapping("/application-error")
        void applicationError() {
            throw new ApplicationException(TestErrorCode.TEST_CONFLICT);
        }

        @GetMapping("/unexpected-error")
        void unexpectedError() {
            throw new IllegalStateException("sensitive implementation detail");
        }
    }

    record Sample(long id, String name) {
    }

    record CreateRequest(@NotBlank(message = "이름은 필수입니다.") String name) {
    }

    enum TestErrorCode implements ErrorCode {
        TEST_CONFLICT;

        @Override
        public String code() {
            return name();
        }

        @Override
        public String message() {
            return "테스트 충돌입니다.";
        }

        @Override
        public HttpStatus status() {
            return HttpStatus.CONFLICT;
        }
    }
}
