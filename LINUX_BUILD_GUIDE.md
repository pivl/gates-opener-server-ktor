# 🐧 Руководство по сборке Linux executables

## 📋 Доступные режимы сборки

### 1. 🍎 **Обычная сборка (все платформы)**
```bash
./gradlew linkLinuxBinaries
```
**Результат:**
- ✅ `gatesopener/bin/gates-opener-server-ktor-amd64` (Linux x86_64)
- ✅ `gatesopener/bin/gates-opener-server-ktor-aarch64` (Linux ARM64)

### 2. 🐳 **Docker сборка x86_64**
```bash
./build-linux.sh
```
**Особенности:**
- Использует `Dockerfile.build`
- Отключает ARM64 target (`-Ddocker.build=true`)
- Результат: только x86_64 executable

### 3. 🦾 **Docker сборка ARM64**
```bash
./build-linux-arm64.sh
```
**Особенности:**
- Использует `Dockerfile.build-arm64`
- Отключает x86_64 target (`-Ddocker.build.arm64=true`)
- Базовый образ: `arm64v8/openjdk:17-jdk-slim`
- Результат: только ARM64 executable

## 🔧 Системные свойства

### `docker.build=true`
**Эффект:**
- ❌ Отключает `linuxArm64` target
- ✅ Включает `linuxX64` target
- ✅ Включает `macosArm64` target

### `docker.build.arm64=true`
**Эффект:**
- ✅ Включает `linuxArm64` target
- ❌ Отключает `linuxX64` target
- ✅ Включает `macosArm64` target

## 📁 Структура файлов

### Docker x86_64 сборка
- **Dockerfile:** `Dockerfile.build`
- **Скрипт:** `build-linux.sh`
- **Задача:** `linkLinuxX64Only`
- **Образ:** `gates-opener-builder`

### Docker ARM64 сборка
- **Dockerfile:** `Dockerfile.build-arm64`
- **Скрипт:** `build-linux-arm64.sh`
- **Задача:** `linkLinuxArm64Only`
- **Образ:** `gates-opener-builder-arm64`

## 🚀 Примеры использования

### Локальная разработка (macOS)
```bash
# Быстрая сборка для macOS
./gradlew linkReleaseExecutableMacosArm64

# Сборка всех Linux binaries
./gradlew linkLinuxBinaries
```

### CI/CD или продакшн
```bash
# Для x86_64 серверов
./build-linux.sh

# Для ARM64 серверов (например, AWS Graviton)
./build-linux-arm64.sh
```

### Проверка конфигурации
```bash
# Проверка x86_64 режима
./gradlew tasks --group build -Ddocker.build=true | grep link

# Проверка ARM64 режима  
./gradlew tasks --group build -Ddocker.build.arm64=true | grep link

# Обычный режим
./gradlew tasks --group build | grep link
```

## ⚠️ Важные замечания

1. **ARM64 Docker сборка** требует Docker с поддержкой multi-arch
2. **Платформа хоста** влияет на производительность кросс-компиляции
3. **Зависимости libcurl** автоматически устанавливаются в Docker контейнерах
4. **Результаты сборки** всегда копируются в `gatesopener/bin/`

## 🐛 Устранение неполадок

### Проблема: "Could not find kotlin-native-prebuilt-X-linux-aarch64.tar.gz"
**Решение:** Используйте правильный Docker файл и флаги:
- Для x86_64: `./build-linux.sh`
- Для ARM64: `./build-linux-arm64.sh`

### Проблема: "Task not found"
**Причина:** Неправильное системное свойство
**Решение:** Проверьте флаги в Dockerfile и задачах

### Проблема: Медленная сборка
**Решение:** 
- Используйте Docker layer caching
- Включите Gradle daemon: `--daemon`
- Сборка на нативной платформе быстрее

## 📈 Производительность

| Метод | Время | Платформа хоста | Результат |
|-------|-------|----------------|-----------|
| Локальная (macOS) | ~2 мин | macOS ARM64 | Все targets |
| Docker x86_64 | ~5 мин | macOS ARM64 | x86_64 только |
| Docker ARM64 | ~3 мин | macOS ARM64 | ARM64 только |
| Нативная ARM64 | ~1 мин | Linux ARM64 | ARM64 только |

Успешной сборки! 🎉
