// BackgroundManager.kt
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit

class BackgroundManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    // Хранилище временного фона в оперативной памяти
    private var tempBackgroundModel: BackgroundModel? = null
    private var originalBackgroundId: String = "bg_default"

    var coins: Int
        get() = getSavedCoins(context)
        set(value) = saveCoins(context, value)

    // Идентификатор сохраненного в конфиге фона
    private var savedBackgroundId: String
        get() = prefs.getString("selected_bg", "bg_default") ?: "bg_default"
        set(value) = prefs.edit { putString("selected_bg", value) }

    // Главный реактивный стейт для Jetpack Compose
    var selectedBackgroundId by mutableStateOf(savedBackgroundId)
        public set

    fun loadBackgrounds(): List<BackgroundModel> {
        return try {
            val json = context.assets.open("backgrounds.json").bufferedReader().use { it.readText() }
            Gson().fromJson(json, object : TypeToken<List<BackgroundModel>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCurrentBackground(): BackgroundModel {
        // Если сейчас активен временный фон уровня — отдаем его сразу без чтения JSON
        if (selectedBackgroundId == "temp_level_bg" && tempBackgroundModel != null) {
            return tempBackgroundModel!!
        }
        val list = loadBackgrounds()
        return list.find { it.id == selectedBackgroundId } ?: list.firstOrNull() ?: BackgroundModel("bg_default", "bg_name_default", "image", "bg_default_draw", 0)
    }

    fun isPurchased(id: String): Boolean {
        if (id == "bg_default") return true
        val purchasedSet = prefs.getStringSet("purchased_backgrounds", emptySet()) ?: emptySet()
        return purchasedSet.contains(id)
    }

    fun buyBackground(bg: BackgroundModel): Boolean {
        if (coins >= bg.price && !isPurchased(bg.id)) {
            coins -= bg.price
            val purchasedSet = prefs.getStringSet("purchased_backgrounds", emptySet())?.toMutableSet() ?: mutableSetOf()
            purchasedSet.add(bg.id)
            prefs.edit { putStringSet("purchased_backgrounds", purchasedSet) }
            return true
        }
        return false
    }

    fun setTemporaryBackground(resourceName: String) {
        val detectedType = if (resourceName.startsWith("vid_") || resourceName.contains("video")) "video" else "image"

        tempBackgroundModel = BackgroundModel(
            id = "temp_level_bg",
            nameResKey = "temp_bg",
            type = detectedType,
            resourceName = resourceName,
            price = 0
        )

        if (selectedBackgroundId != "temp_level_bg") {
            originalBackgroundId = selectedBackgroundId
        }

        // Оповещаем Compose об изменении фона
        selectedBackgroundId = "temp_level_bg"
    }

    fun resetToMenuBackground() {
        tempBackgroundModel = null
        selectedBackgroundId = originalBackgroundId
    }

    fun selectBackground(id: String) {
        if (isPurchased(id)) {
            savedBackgroundId = id
            selectedBackgroundId = id
        }
    }

    @SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
    @Composable
    fun getLocalizedName(bg: BackgroundModel): String {
        val localContext = LocalContext.current
        val resId = localContext.resources.getIdentifier(bg.nameResKey, "string", localContext.packageName)
        return if (resId != 0) stringResource(resId) else bg.nameResKey
    }
}