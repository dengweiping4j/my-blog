package com.pyy.blog.controller;

import com.pyy.blog.domain.BlogPaper;
import com.pyy.blog.domain.Pagination;
import com.pyy.blog.domain.Result;
import com.pyy.blog.domain.dto.BlogPaperDTO;
import com.pyy.blog.service.MdBlogService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * md文档控制器类
 *
 * @author dengweiping
 * @date 2021/1/19 14:40
 */
@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Autowired
    private MdBlogService mdBlogService;

    /**
     * 获取文档
     *
     * @return
     */
    @ApiOperation(value = "获取文档", notes = "获取文档", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功"),
            @ApiResponse(code = 204, message = "没有内容")})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> get(@PathVariable("id") String id) {
        return new ResponseEntity<>(mdBlogService.getBlogById(id), HttpStatus.OK);
    }

    /**
     * 分页查询
     *
     * @param blogPaper
     * @param pagination
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "查询成功")})
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResponseEntity<Result> query(@RequestBody BlogPaper blogPaper, @Valid Pagination pagination) {
        return ResponseEntity.ok(mdBlogService.query(blogPaper, pagination));
    }

    /**
     * 创建文档
     *
     * @return
     */
    @ApiOperation(value = "创建文档", notes = "创建文档", produces = "application/json")
    @ApiResponses({@ApiResponse(code = 200, message = "新增成功"),
            @ApiResponse(code = 204, message = "没有内容")})
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody BlogPaperDTO blogPaper) {
        return new ResponseEntity<>(mdBlogService.createMdBlog(blogPaper), HttpStatus.OK);
    }

}
