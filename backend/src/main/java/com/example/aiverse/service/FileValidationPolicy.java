package com.example.aiverse.service;

import java.util.Set;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.FileErrorCode;
import com.example.aiverse.dto.UploadPurpose;
import com.example.aiverse.entity.AssetType;

final class FileValidationPolicy {

    private static final long MB = 1024L * 1024L;
    private static final long GB = 1024L * MB;

    private static final Set<String> COVER_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long COVER_MAX_SIZE = 10 * MB;

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long IMAGE_MAX_SIZE = 50 * MB;

    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of("video/mp4", "video/webm");
    private static final long VIDEO_MAX_SIZE = GB;

    private static final Set<String> MUSIC_CONTENT_TYPES = Set.of("audio/mpeg", "audio/wav", "audio/flac");
    private static final long MUSIC_MAX_SIZE = 200 * MB;

    private FileValidationPolicy() {
    }

    static void validate(UploadPurpose purpose, AssetType assetType, String contentType, long fileSize) {
        Set<String> allowedContentTypes;
        long maxSize;

        if (purpose == UploadPurpose.COVER) {
            allowedContentTypes = COVER_CONTENT_TYPES;
            maxSize = COVER_MAX_SIZE;
        } else {
            if (assetType == null) {
                throw new ApplicationException(FileErrorCode.INVALID_FILE_FORMAT);
            }
            allowedContentTypes = switch (assetType) {
                case IMAGE -> IMAGE_CONTENT_TYPES;
                case VIDEO -> VIDEO_CONTENT_TYPES;
                case MUSIC -> MUSIC_CONTENT_TYPES;
            };
            maxSize = switch (assetType) {
                case IMAGE -> IMAGE_MAX_SIZE;
                case VIDEO -> VIDEO_MAX_SIZE;
                case MUSIC -> MUSIC_MAX_SIZE;
            };
        }

        if (!allowedContentTypes.contains(contentType)) {
            throw new ApplicationException(FileErrorCode.INVALID_FILE_FORMAT);
        }
        if (fileSize > maxSize) {
            throw new ApplicationException(FileErrorCode.FILE_TOO_LARGE);
        }
    }
}
