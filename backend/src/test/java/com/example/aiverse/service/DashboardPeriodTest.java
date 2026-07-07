package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.CommonErrorCode;

class DashboardPeriodTest {

    private final LocalDate today = LocalDate.of(2026, 7, 7);

    @Test
    void 문자열을_기간으로_변환한다() {
        assertThat(DashboardPeriod.parse("7D")).isEqualTo(DashboardPeriod.SEVEN_DAYS);
        assertThat(DashboardPeriod.parse("30D")).isEqualTo(DashboardPeriod.THIRTY_DAYS);
        assertThat(DashboardPeriod.parse("ALL")).isEqualTo(DashboardPeriod.ALL);
        assertThat(DashboardPeriod.parse(null)).isEqualTo(DashboardPeriod.THIRTY_DAYS);
    }

    @Test
    void 알수없는_기간_문자열은_예외를_던진다() {
        assertThatThrownBy(() -> DashboardPeriod.parse("60D"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(CommonErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 최근_7일은_오늘_포함_7일이다() {
        assertThat(DashboardPeriod.SEVEN_DAYS.startDate(today)).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(DashboardPeriod.SEVEN_DAYS.from(today)).isEqualTo(LocalDate.of(2026, 7, 1).atStartOfDay());
    }

    @Test
    void 최근_30일은_오늘_포함_30일이다() {
        assertThat(DashboardPeriod.THIRTY_DAYS.startDate(today)).isEqualTo(LocalDate.of(2026, 6, 8));
    }

    @Test
    void 전체_기간은_시작일과_from이_없다() {
        assertThat(DashboardPeriod.ALL.startDate(today)).isNull();
        assertThat(DashboardPeriod.ALL.from(today)).isNull();
    }
}
