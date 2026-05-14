data class CampaignLevel(
    val id: Int,
    val reward: Int,    // НАГРАДА
    val hintCost: Int,  // СТОИМОСТЬ
    val words: Map<String, String>
)