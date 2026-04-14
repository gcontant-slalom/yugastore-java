package com.yugabyte.app.yugastore.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.yugabyte.app.yugastore.domain.ProductMetadata;

@ExtendWith(MockitoExtension.class)
class PagerTest {

    @Mock
    private Page<ProductMetadata> page;

    @Test
    void getPageIndex_returnsPageNumberPlusOne() {
        when(page.getNumber()).thenReturn(2);
        Pager pager = new Pager(page);

        assertThat(pager.getPageIndex()).isEqualTo(3);
    }

    @Test
    void getPageIndex_onFirstPage_returnsOne() {
        when(page.getNumber()).thenReturn(0);
        Pager pager = new Pager(page);

        assertThat(pager.getPageIndex()).isEqualTo(1);
    }

    @Test
    void getPageSize_delegatesToPage() {
        when(page.getSize()).thenReturn(20);
        Pager pager = new Pager(page);

        assertThat(pager.getPageSize()).isEqualTo(20);
    }

    @Test
    void hasNext_whenNextPageExists_returnsTrue() {
        when(page.hasNext()).thenReturn(true);
        Pager pager = new Pager(page);

        assertThat(pager.hasNext()).isTrue();
    }

    @Test
    void hasNext_whenOnLastPage_returnsFalse() {
        when(page.hasNext()).thenReturn(false);
        Pager pager = new Pager(page);

        assertThat(pager.hasNext()).isFalse();
    }

    @Test
    void hasPrevious_whenNotFirstPage_returnsTrue() {
        when(page.hasPrevious()).thenReturn(true);
        Pager pager = new Pager(page);

        assertThat(pager.hasPrevious()).isTrue();
    }

    @Test
    void hasPrevious_whenOnFirstPage_returnsFalse() {
        when(page.hasPrevious()).thenReturn(false);
        Pager pager = new Pager(page);

        assertThat(pager.hasPrevious()).isFalse();
    }

    @Test
    void getTotalPages_delegatesToPage() {
        when(page.getTotalPages()).thenReturn(5);
        Pager pager = new Pager(page);

        assertThat(pager.getTotalPages()).isEqualTo(5);
    }

    @Test
    void getTotalElements_delegatesToPage() {
        when(page.getTotalElements()).thenReturn(100L);
        Pager pager = new Pager(page);

        assertThat(pager.getTotalElements()).isEqualTo(100L);
    }

    @Test
    void indexOutOfBounds_whenPageIndexNegative_returnsTrue() {
        // page.getNumber() = -2 → pageIndex = -1 → < 0, out of bounds (short-circuit)
        when(page.getNumber()).thenReturn(-2);
        Pager pager = new Pager(page);

        assertThat(pager.indexOutOfBounds()).isTrue();
    }

    @Test
    void indexOutOfBounds_whenPageIndexExceedsTotal_returnsTrue() {
        // page.getNumber() = 10 → pageIndex = 11, totalElements = 5 → 11 > 5, out of bounds
        when(page.getNumber()).thenReturn(10);
        when(page.getTotalElements()).thenReturn(5L);
        Pager pager = new Pager(page);

        assertThat(pager.indexOutOfBounds()).isTrue();
    }

    @Test
    void indexOutOfBounds_whenPageIndexWithinRange_returnsFalse() {
        // page.getNumber() = 1 → pageIndex = 2, totalElements = 50 → 2 > 0 and 2 <= 50
        when(page.getNumber()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(50L);
        Pager pager = new Pager(page);

        assertThat(pager.indexOutOfBounds()).isFalse();
    }
}
