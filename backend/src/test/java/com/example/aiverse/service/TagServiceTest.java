package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.dto.TagResponse;
import com.example.aiverse.repository.TagRepository;
import com.example.aiverse.repository.TagUsage;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService tagService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository);
    }

    @Test
    void 검색어와_limit을_그대로_전달해_사용량순_태그를_반환한다() {
        given(tagRepository.searchOrderByUsage("city", 20))
                .willReturn(List.of(new TagUsage(1L, "city", 5L)));

        List<TagResponse> result = tagService.search("city", 20);

        assertThat(result).extracting(TagResponse::name).containsExactly("city");
        assertThat(result.get(0).usageCount()).isEqualTo(5L);
    }

    @Test
    void limit이_50을_넘으면_50으로_제한한다() {
        given(tagRepository.searchOrderByUsage(null, 50)).willReturn(List.of());

        tagService.search(null, 100);

        org.mockito.Mockito.verify(tagRepository).searchOrderByUsage(null, 50);
    }

    @Test
    void limit이_지정되지_않으면_기본값_20을_사용한다() {
        given(tagRepository.searchOrderByUsage(null, 20)).willReturn(List.of());

        tagService.search(null, null);

        org.mockito.Mockito.verify(tagRepository).searchOrderByUsage(null, 20);
    }

    @Test
    void limit이_1보다_작으면_1로_보정한다() {
        given(tagRepository.searchOrderByUsage(null, 1)).willReturn(List.of());

        tagService.search(null, 0);

        org.mockito.Mockito.verify(tagRepository).searchOrderByUsage(null, 1);
    }
}
