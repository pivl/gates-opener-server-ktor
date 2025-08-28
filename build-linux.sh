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



# Запускаем сборку и копируем результат
echo "🔨 Запускаем сборку всех Linux платформ через QEMU эмуляцию..."
docker run --rm \
    --platform linux/amd64 \
    -v "$(pwd)/gatesopener:/app/gatesopener" \
    gates-opener-builder-x64

if [ $? -ne 0 ]; then
    echo "❌ Ошибка при выполнении Docker контейнера"
    exit 1
fi

# Проверяем результат
if [ -f "gatesopener/bin/gates-opener-server-ktor-amd64" ] || [ -f "gatesopener/bin/gates-opener-server-ktor-aarch64" ]; then
    echo "✅ Сборка завершена успешно!"
    echo "📁 Результаты сборки в gatesopener/bin/:"
    if [ -f "gatesopener/bin/gates-opener-server-ktor-amd64" ]; then
        echo "✅ Linux x86_64 executable: gatesopener/bin/gates-opener-server-ktor-amd64"
        echo "📊 Размер файла (x86_64):"
        ls -lh gatesopener/bin/gates-opener-server-ktor-amd64
        echo "🔍 Архитектура (x86_64):"
        file gatesopener/bin/gates-opener-server-ktor-amd64
    fi
    if [ -f "gatesopener/bin/gates-opener-server-ktor-aarch64" ]; then
        echo "✅ Linux ARM64 executable: gatesopener/bin/gates-opener-server-ktor-aarch64"
        echo "📊 Размер файла (ARM64):"
        ls -lh gatesopener/bin/gates-opener-server-ktor-aarch64
        echo "🔍 Архитектура (ARM64):"
        file gatesopener/bin/gates-opener-server-ktor-aarch64
    fi
else
    echo "❌ Сборка не удалась - executable не найден"
    echo "📁 Содержимое директории gatesopener:"
    find gatesopener -name "gates-opener-server-ktor-*" -ls 2>/dev/null || echo "Executable файлы не найдены"
    exit 1
fi
