package com.tgboyles.frugalfoxmcp.dto;

import org.springframework.lang.NonNull;

public record AuthRequest(@NonNull String username, @NonNull String password) {
}
