data class BackgroundModel(
    val id: String,
    val nameResKey: String, // Ключ перевода
    val type: String,       // "image" или "video"
    val resourceName: String, // Имя файла в drawable или raw
    val price: Int
)