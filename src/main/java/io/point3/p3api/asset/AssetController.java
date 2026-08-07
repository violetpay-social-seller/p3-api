package io.point3.p3api.asset;

import io.point3.p3api.asset.application.register.AssetRegisterUseCase;
import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetRegisterUseCase assetRegisterUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AssetRespone> registerAsset(
            @RequestPart("file") MultipartFile file,
            @Authenticated CurrentUser currentUser
            )
    throws IOException {
        RegistryAsset asset = assetRegisterUseCase.register(toCommand(currentUser, file));
    }

    private RegisterAssetCommand toCommand(
            CurrentUser currentUser, MultipartFile file)
            throws IOException {
        return new RegisterAssetCommand(
                currentUser.userId(),
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize());
    }
