package com.huan.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
//@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class PageResponse<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPage;
    private List<T> data;
    private String message;

    public PageResponse() {
    }

    public static <T> PageResponse<T> pagedData(int pageNum, int pageSize, int totalPage, List<T> data) {
        PageResponse<T> result = new PageResponse<>();
        result.setPageNum(pageNum);
        result.setData(data);
        result.setTotalPage(totalPage);
        result.setPageSize(pageSize);
        result.setMessage(null);
        return result;
    }

    public static <T> PageResponse<T> of(String message) {
        PageResponse<T> result = new PageResponse<>();
        result.setMessage(message);
        return result;
    }
}
