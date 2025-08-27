#!/bin/bash

set -e

echo "🚀 Сборка Gates Opener Server для Linux ARM64..."

# Проверяем что Docker запущен
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker не запущен или недоступен"
    exit 1
fi

# Создаем Docker образ для ARM64 сборки
echo "🔧 Создаем Docker образ для ARM64 сборки..."
docker build -f Dockerfile.build-arm64 -t gates-opener-builder-arm64 .

# Запускаем сборку и копируем результат
echo "🔨 Запускаем ARM64 сборку..."
docker run --rm --platform linux/arm64 -v $(pwd)/build:/app/build gates-opener-builder-arm64

# Проверяем результат
echo "✅ Проверяем результат сборки..."
if [ -f "gatesopener/bin/gates-opener-server-ktor-aarch64" ]; then
    echo "🎉 Успешно! ARM64 executable создан:"
    ls -la gatesopener/bin/gates-opener-server-ktor-aarch64
    file gatesopener/bin/gates-opener-server-ktor-aarch64
else
    echo "❌ Не удалось найти gates-opener-server-ktor-aarch64"
    echo "Содержимое gatesopener/bin/:"
    ls -la gatesopener/bin/ || echo "Директория не существует"
    exit 1
fi

echo "🚀 ARM64 сборка завершена успешно!"
