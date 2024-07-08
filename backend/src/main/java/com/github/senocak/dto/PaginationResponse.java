package com.github.senocak.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.domain.Page;
import java.util.List;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"page", "pages", "total", "sort", "sortBy", "items"})
public class PaginationResponse<T, P> extends BaseDto {
    @Schema(example = "1", description = "Current page", requiredMode = Schema.RequiredMode.REQUIRED, name = "page", type = "String")
    private final int page;

    @Schema(example = "3", description = "Total pages", requiredMode = Schema.RequiredMode.REQUIRED, name = "pages", type = "String")
    private final int pages;

    @Schema(example = "10", description = "Total elements", requiredMode = Schema.RequiredMode.REQUIRED, name = "total", type = "String")
    private long total;

    @Schema(example = "id", description = "Sort by", requiredMode = Schema.RequiredMode.REQUIRED, name = "sortBy", type = "String")
    private final String sortBy;

    @Schema(example = "asc", description = "Sort", requiredMode = Schema.RequiredMode.REQUIRED, name = "sort", type = "String")
    private final String sort;

    @ArraySchema(schema = @Schema(description = "items", requiredMode = Schema.RequiredMode.REQUIRED, type = "ListDto"))
    private final List<P> items;

    public PaginationResponse(Page<T> page, List<P> items, String sortBy, String sort) {
        this.page = page.getNumber() + 1;
        this.pages = page.getTotalPages();
        this.total = page.getTotalElements();
        this.sortBy = sortBy;
        this.sort = sort;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSort() {
        return sort;
    }

    public List<P> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "PaginationResponse(page: " + page + ", pages: " + pages + ", total: " + total + ", items: " + items + ")";
    }
}
