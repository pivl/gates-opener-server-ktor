# 🚀 Инструкции по сборке Gates Opener Server

## macOS (ARM64)

✅ **Готово к использованию**

```bash
# Сборка
./gradlew linkReleaseExecutableMacosArm64

# Запуск
./build/bin/macosArm64/releaseExecutable/gates-opener-server-ktor.kexe \
  --api-token "your-api-token" \
  --initial-token "your-jwt-token" \
  --device-uuid "your-device-uuid"
```

## Linux (x86_64 / ARM64)

### Вариант 1: Сборка на Linux машине

1. **Установите зависимости на Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install -y build-essential libcurl4-openssl-dev openjdk-17-jdk
```

2. **Соберите:**
```bash
./gradlew linkReleaseExecutableLinuxX64   # для x86_64
./gradlew linkReleaseExecutableLinuxArm64  # для ARM64
```

### Вариант 2: Сборка через Docker

```bash
# Используйте готовый скрипт
./build-linux.sh
```

### Вариант 3: GitHub Actions

Создайте `.github/workflows/build.yml`:
```yaml
name: Build Linux Executables
on: [push, pull_request]
jobs:
  build-linux:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - run: sudo apt-get install -y libcurl4-openssl-dev
    - run: ./gradlew linkReleaseExecutableLinuxX64
    - uses: actions/upload-artifact@v3
      with:
        name: linux-executable
        path: build/bin/linuxX64/releaseExecutable/
```

## 🐳 Docker для production

```dockerfile
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y libcurl4
COPY build/bin/linuxX64/releaseExecutable/gates-opener-server-ktor.kexe /app/server
EXPOSE 8080
CMD ["/app/server", "--api-token", "$API_TOKEN", "--initial-token", "$INITIAL_TOKEN"]
```

## ⚠️ Известные проблемы

- **Кросс-компиляция с macOS на Linux**: Требует libcurl и другие системные библиотеки Linux
- **Решение**: Собирайте на целевой платформе или используйте Docker/CI

## 📦 Результат сборки

После успешной сборки executable файлы будут в:
- `build/bin/macosArm64/releaseExecutable/gates-opener-server-ktor.kexe`
- `build/bin/linuxX64/releaseExecutable/gates-opener-server-ktor.kexe`
- `build/bin/linuxArm64/releaseExecutable/gates-opener-server-ktor.kexe`
