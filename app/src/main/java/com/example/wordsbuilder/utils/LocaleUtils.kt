import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.content.edit

fun changeLocale(context: Context, langCode: String) {
    saveLanguage(context, langCode) // Сначала сохраняем
    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
    AppCompatDelegate.setApplicationLocales(appLocale) // Затем уведомляем систему (она перезапустит экран)
}

fun saveLanguage(context: Context, langCode: String) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    prefs.edit { putString("app_lang", langCode) }
}

fun getSavedLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    // По умолчанию берем системный или "en"
    return prefs.getString("app_lang", "en") ?: "en"
}