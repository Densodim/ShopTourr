package com.example.shoptourr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun epochMillis(): Long
