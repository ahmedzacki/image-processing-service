package com.ahmed.image_processing_service.image.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageResponse {

    private final UUID imageId;
    private final String fileName;
    private final String contentType;
    private final long size;
    private final String status;
}
