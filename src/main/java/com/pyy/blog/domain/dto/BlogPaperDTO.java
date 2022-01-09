package com.pyy.blog.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 博客文档实体
 *
 * @author dengweiping
 * @date 2021/1/19 14:30
 */
@Data
public class BlogPaperDTO {
    private String id;

    private String author;

    private String title;

    private List<String> labels;

    private String description;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;
}
