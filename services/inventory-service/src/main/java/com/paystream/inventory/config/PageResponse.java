package com.paystream.inventory.config;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private long totalCount;
    private int currentPage;
    private int totalPages;

    /**
     * @param page 원본 Page 객체 (StoreResponse 타입 등)
     */
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.totalCount = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
    }
}
