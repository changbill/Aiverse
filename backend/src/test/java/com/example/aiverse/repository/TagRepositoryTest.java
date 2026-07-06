package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.Tag;
import com.example.aiverse.support.IntegrationTestSupport;

class TagRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void 태그를_저장하고_이름으로_조회할_수_있다() {
        tagRepository.save(Tag.of("cyberpunk"));

        assertThat(tagRepository.findByName("cyberpunk"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("cyberpunk");
    }

    @Test
    void 존재하지_않는_이름_조회시_빈값을_반환한다() {
        assertThat(tagRepository.findByName("no-such-tag")).isEmpty();
    }

    @Test
    void 태그_존재_여부를_확인할_수_있다() {
        tagRepository.save(Tag.of("city"));

        assertThat(tagRepository.existsByName("city")).isTrue();
        assertThat(tagRepository.existsByName("no-such-tag")).isFalse();
    }
}
