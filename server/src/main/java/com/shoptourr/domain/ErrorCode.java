package com.shoptourr.domain;

public enum ErrorCode {
    VALIDATION_ERROR,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    IDEMPOTENCY_CONFLICT,
    BUDGET_RULE,
    TAXFREE_RULE,
    RATE_LIMITED,
    MEDIA_NOT_READY,
    INTERNAL
}
