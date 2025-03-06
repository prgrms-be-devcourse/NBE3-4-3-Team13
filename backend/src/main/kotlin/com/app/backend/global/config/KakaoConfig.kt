package com.app.backend.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakao")
data class KakaoConfig(
    var id: String = "",
    var secret: String = "",
    var redirectUri: String = ""
)