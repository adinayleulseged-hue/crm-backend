package com.crm.gateway.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtBlacklistKeysTest {

    @Test
    void key_usesSharedPrefix() {
        assertThat(JwtBlacklistKeys.key("abc-123")).isEqualTo("auth:jwt:blacklist:abc-123");
    }
}
