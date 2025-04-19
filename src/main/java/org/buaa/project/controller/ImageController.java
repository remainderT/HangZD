package org.buaa.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.service.ImageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片管理控制层
 */
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * oss上传图片
     */
    @PostMapping("/oss/upload")
    public Result<String> ossUploadImage(@RequestParam("file") MultipartFile file) {
        return Results.success(imageService.ossUploadImage(file));
    }

    /**
     * 登录时候获取验证码， 并把结果存入session中.
     */
    @GetMapping("/api/answerly/v1/user/captcha")
    public void getCaptcha(HttpServletResponse response) {
        imageService.getCaptcha(response);
    }

}
