package com.tgboyles.frugalfoxmcp.dto;

import org.springframework.lang.NonNull;

public record AuthResponse(@NonNull String token, @NonNull String username, @NonNull String email) {
}
