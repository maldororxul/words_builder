//package com.example.wordsbuilder.ui.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.wordsbuilder.R

//@Composable
//fun getGameFontFamily(): FontFamily {
//    // Получаем текущую локаль приложения через AppCompatDelegate
//    val locales = AppCompatDelegate.getApplicationLocales()
//    val currentLocale = if (!locales.isEmpty) locales[0]?.language ?: "en" else "en"
//
//    return if (currentLocale == "ru") {
//        // Если язык русский, отдаем мультяшный шрифт с поддержкой кириллицы
//        FontFamily(Font(R.font.cartoon_ru))
//    } else {
//        // Для английского и испанского оставляем ваш оригинальный шрифт
//        FontFamily(Font(R.font.cartoon))
//    }
//}
