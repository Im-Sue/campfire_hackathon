package cn.iocoder.yudao.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.AppFileUploadReqVO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 文件存储")
@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class AppFileController {

    @Resource
    private FileService fileService;

    /**
     * 上传文件大小限制：3MB
     */
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "需要登录认证，每用户每分钟限15次，单文件最大3MB")
    @Parameter(name = "file", description = "文件附件", required = true, schema = @Schema(type = "string", format = "binary"))
    @RateLimiter(time = 60, count = 15, timeUnit = TimeUnit.SECONDS, keyResolver = UserRateLimiterKeyResolver.class, message = "上传过于频繁，请稍后再试")
    public CommonResult<String> uploadFile(AppFileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        // 校验文件大小，限制为 3MB
        if (file.getSize() > MAX_FILE_SIZE) {
            return CommonResult.error(400, "上传文件大小不能超过 3MB");
        }
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return success(fileService.createFile(content, file.getOriginalFilename(),
                uploadReqVO.getDirectory(), file.getContentType()));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "获取文件预签名地址（上传）", description = "前端直传OSS，每用户每分钟限15次")
    @Parameters({
            @Parameter(name = "name", description = "文件名称", required = true),
            @Parameter(name = "directory", description = "文件目录")
    })
    @RateLimiter(time = 60, count = 15, timeUnit = TimeUnit.SECONDS, keyResolver = UserRateLimiterKeyResolver.class, message = "请求过于频繁，请稍后再试")
    public CommonResult<FilePresignedUrlRespVO> getFilePresignedUrl(
            @RequestParam("name") String name,
            @RequestParam(value = "directory", required = false) String directory) {
        return success(fileService.presignPutUrl(name, directory));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件", description = "配合presigned-url，每用户每分钟限15次")
    @RateLimiter(time = 60, count = 15, timeUnit = TimeUnit.SECONDS, keyResolver = UserRateLimiterKeyResolver.class, message = "请求过于频繁，请稍后再试")
    public CommonResult<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return success(fileService.createFile(createReqVO));
    }

}
