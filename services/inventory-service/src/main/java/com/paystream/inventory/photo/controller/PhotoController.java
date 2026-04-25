package com.paystream.inventory.photo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Photo", description = "이미지 처리 관련 API")
public interface PhotoController {

    @Operation(
            summary = "상품 이미지 조회",
            description = "이미지 이름을 입력받아 해당 이미지 파일을 서버에서 스트리밍합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "이미지 조회 성공",
                        content = @Content(mediaType = "image/png") // 혹은 image/jpeg
                        ),
                @ApiResponse(
                        responseCode = "404",
                        description = "이미지를 찾을 수 없음",
                        content = @Content // 에러 시에는 이미지 데이터가 없으므로 비워둠
                        )
            })
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) throws IOException;
}
