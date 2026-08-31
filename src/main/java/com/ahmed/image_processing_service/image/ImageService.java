package com.ahmed.image_processing_service.image;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ahmed.image_processing_service.exception.InvalidFileException;
import com.ahmed.image_processing_service.image.dto.ImageResponse;

@Service
public class ImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final ImageRepository imageRepository;
    private final S3StorageService s3StorageService;

    public ImageService(
            ImageRepository imageRepository,
            S3StorageService s3StorageService) {

        this.imageRepository = imageRepository;
        this.s3StorageService = s3StorageService;
    }

    public ImageResponse upload(Long userId, MultipartFile file) {

        validate(file);

        UUID imageId = UUID.randomUUID();
        String extension = extensionFor(file.getContentType());
        String s3Key = "images/%s/%s/original.%s".formatted(userId, imageId, extension);

        s3StorageService.upload(s3Key, file);

        Image image = new Image();
        image.setId(imageId);
        image.setUserId(userId);
        image.setOriginalFilename(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setS3Key(s3Key);

        imageRepository.save(image);

        return new ImageResponse(
                imageId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                "UPLOADED");
    }

    private void validate(MultipartFile file) {

        if (file.isEmpty()) {
            throw new InvalidFileException("Uploaded file must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File exceeds maximum allowed size of 10MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException(
                    "Unsupported file type: " + file.getContentType()
                            + ". Allowed types: JPEG, PNG, WebP");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new InvalidFileException("Unsupported file type: " + contentType);
        };
    }
}
