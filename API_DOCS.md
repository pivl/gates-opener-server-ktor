# Gates Opener Server API

Прокси-сервер для открывания дверей/калиток/ворот через систему EVO73.

## Запуск приложения

```bash
./gates-opener-server-ktor --api-token YOUR_API_TOKEN --initial-token YOUR_INITIAL_TOKEN
```

### Аргументы командной строки

- `--api-token` - токен для авторизации внешних клиентов (обязательный)
- `--initial-token` - первичный токен для получения токенов системы EVO73 (обязательный) 
- `--device-uuid` - UUID устройства (опциональный, по умолчанию: fe9883696cbc9018)

### Пример запуска

```bash
./gates-opener-server-ktor \
  --api-token "my-secure-api-token" \
  --initial-token "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..." \
  --device-uuid "fe9883696cbc9018"
```

## API Endpoints

### Открытие двери

**POST** `/open-door`

**Headers:**
```
Authorization: Bearer YOUR_API_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "doorphone_id": "3770",
  "door_number": "2"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Door opened successfully"
}
```

**Error Response:**
```json
{
  "status": "error", 
  "message": "Error description"
}
```

### Проверка состояния

**GET** `/health`

**Response:**
```json
{
  "status": "ok"
}
```

## Примеры использования

### Открыть подъезд №1 (doorphone_id: 3770, door: 2)

```bash
curl -X POST http://localhost:8080/open-door \
  -H "Authorization: Bearer YOUR_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doorphone_id": "3770", "door_number": "2"}'
```

### Открыть ворота (doorphone_id: 3836, door: 2)

```bash
curl -X POST http://localhost:8080/open-door \
  -H "Authorization: Bearer YOUR_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doorphone_id": "3836", "door_number": "2"}'
```

## Работа с токенами

Приложение автоматически:
1. Получает токены на основе `initial-token`
2. Обновляет токены при истечении срока действия
3. Повторяет запросы при получении 401 ошибки

Флоу работы с токенами:
1. `initial-token` → `main-token` (через `/api/v2/single-auth/main-token`)
2. `main-token` → `intercom-token` (через `/api/v1/authIntercom`)
3. `intercom-token` используется для открывания дверей

При получении 401 ошибки:
1. Обновляется `initial-token` (через `/auth/refresh`)
2. Получаются новые `main-token` и `intercom-token`
3. Повторяется исходный запрос

## Безопасность

- API использует Bearer токен авторизацию
- Все внутренние токены автоматически обновляются
- Логирование всех действий в консоль

## Порт

Сервер запускается на порту **8080** и доступен по адресу `http://0.0.0.0:8080`
