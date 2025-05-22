package com.caplyptasapps.qrcodegeneratorkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform