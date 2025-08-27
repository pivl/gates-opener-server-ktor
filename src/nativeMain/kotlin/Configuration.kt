data class Configuration(
    val apiToken: String,  // Токен для API авторизации (используется внешними клиентами)
    val initialToken: String,  // Первичный токен для получения токенов системы
    val deviceUuid: String = "fe9883696cbcffff",  // UUID устройства
)