package com.example.aiverse.util;

import java.util.Locale;

public final class TagNameNormalizer {

    private TagNameNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
