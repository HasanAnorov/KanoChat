package com.ierusalem.androchat.core.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Collections
import java.util.Stack

class RandomColors() {
    private val recycle: Stack<Int> = Stack()
    private val colors: Stack<Int> = Stack()

    init {
        recycle.addAll(
            listOf(
                // ARGB hex to int >> (0xFFEE5670.toInt(),...)
                Color(0xFF2196F3).toArgb(),
                Color(0xFF673AB7).toArgb(),
                Color(0xFF00BCD4).toArgb(),
                Color(0xFF8BC34A).toArgb(),
                Color(0xFFFF5722).toArgb(),
                Color(0xFF795548).toArgb(),
                Color(0xFF9E9E9E).toArgb(),
                Color(0xFF607D8B).toArgb(),
                Color(0xFF303F9F).toArgb(),
                Color(0xFF009688).toArgb(),
                Color(0xFF00BCD4).toArgb(),
                Color(0xFF4CAF50).toArgb(),
                Color(0xFFFF5722).toArgb(),
                Color(0xFF795548).toArgb(),
            )
        )
    }

    fun getColor(): Int {
        if (colors.size == 0)
            while (!recycle.isEmpty()) colors.push(recycle.pop())
        Collections.shuffle(colors)
        val c = colors.pop()
        recycle.push(c)
        return c
    }
}