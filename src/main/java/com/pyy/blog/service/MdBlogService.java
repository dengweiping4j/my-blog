package com.pyy.blog.service;

import com.pyy.blog.domain.BlogPaper;
import com.pyy.blog.domain.Pagination;
import com.pyy.blog.domain.Result;
import com.pyy.blog.domain.dto.BlogPaperDTO;
import com.pyy.blog.domain.mapper.BlogPaperMapper;
import com.pyy.blog.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * md文档业务逻辑类
 *
 * @author dengweiping
 * @date 2021/1/19 14:36
 */
@Service
public class MdBlogService {

    @Autowired
    private DocService docService;

    public Result createMdBlog(BlogPaperDTO dto) {
        BlogPaper blog = BlogPaperMapper.toEntity(dto);
        blog.setId(UUIDUtil.creatUUID());
        blog.setAuthor("邓卫平");
        blog.setCreateDate(System.currentTimeMillis());
        return docService.addDocument(blog);
    }

    public Result getBlogById(String id) {
        BlogPaperDTO blogPaper = docService.getDocById(id);
        if (blogPaper == null) {
            return Result.error("查询失败");
        }

        return Result.success(blogPaper);
    }

    public Result query(BlogPaper queryDTO, Pagination pagination) {
        return docService.queryDoc(queryDTO, pagination);
    }
}
