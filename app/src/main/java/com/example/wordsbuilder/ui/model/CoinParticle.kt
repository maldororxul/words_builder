package com.example.wordsbuilder.ui.model

data class CoinParticle(
    val id: Int,
    var x: Float,          // Текущая координата X (в пикселях)
    var y: Float,          // Текущая координата Y (в пикселях)
    val velocityX: Float,  // Скорость полета по горизонтали
    val velocityY: Float,  // Скорость полета по вертикали (отрицательная, чтобы лететь вверх)
    var alpha: Float = 1f  // Прозрачность (от 1.0 до 0.0)
)