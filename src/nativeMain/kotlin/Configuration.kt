data class Configuration(
    val apiToken: String,  // Токен для API авторизации (используется внешними клиентами)
    val initialToken: String,  // Первичный токен (бывший internalKey) для получения токенов системы
    val deviceUuid: String = "fe9883696cbc9018",  // UUID устройства
)