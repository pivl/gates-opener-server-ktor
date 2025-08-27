#!/bin/bash

# Скрипт для сборки Linux x86_64 executable через Docker на macOS

echo "🐳 Сборка Linux x86_64 executable через Docker..."
echo "📱 Система: $(uname -s) $(uname -m)"

# Проверяем что Docker запущен
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker не запущен или недоступен"
    exit 1
fi

# Проверяем что мы на macOS (опционально)
if [[ "$(uname -s)" == "Darwin" ]]; then
    echo "✅ Запуск на macOS - используем Docker для сборки Linux x86_64"
else
    echo "⚠️  Запуск не на macOS, но продолжаем..."
fi

# Собираем Docker образ с кешированием, принуждая использовать x86_64 платформу
echo "📦 Собираем Docker образ для сборки x86_64 через QEMU эмуляцию..."
docker build --platform linux/amd64 -f Dockerfile.build -t gates-opener-builder-x64 .

if [ $? -ne 0 ]; then
    echo "❌ Не удалось собрать Docker образ"
    exit 1
fi

# Создаем директорию для результатов если её нет
mkdir -p "$(pwd)/build"

# Запускаем сборку и копируем результат
echo "🔨 Запускаем сборку x86_64 через QEMU эмуляцию..."
docker run --rm \
    --platform linux/amd64 \
    -v "$(pwd)/build:/app/build" \
    gates-opener-builder-x64

if [ $? -ne 0 ]; then
    echo "❌ Ошибка при выполнении Docker контейнера"
    exit 1
fi

# Проверяем результат
if [ -f "build/bin/linuxX64/releaseExecutable/gates-opener-server-ktor.kexe" ]; then
    echo "✅ Сборка завершена успешно!"
    echo "📁 Linux x86_64 executable: build/bin/linuxX64/releaseExecutable/gates-opener-server-ktor.kexe"
    echo "📊 Размер файла:"
    ls -lh build/bin/linuxX64/releaseExecutable/gates-opener-server-ktor.kexe
    echo "🔍 Архитектура:"
    file build/bin/linuxX64/releaseExecutable/gates-opener-server-ktor.kexe
else
    echo "❌ Сборка не удалась - executable не найден"
    echo "📁 Содержимое директории build:"
    find build -name "*.kexe" -ls 2>/dev/null || echo "Файлы *.kexe не найдены"
    exit 1
fi
