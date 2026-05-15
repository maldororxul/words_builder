import com.google.gson.annotations.SerializedName

data class CampaignLevel(
    val id: Int,
    val reward: Int,
    val hintCost: Int,
    @SerializedName("bg_res") val bgRes: String,
    @SerializedName("music_res") val musicRes: String,
    @SerializedName("splash_res") val splashRes: String,
    val description: String,
    val words: Map<String, String>
)