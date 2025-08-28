#!/bin/bash

# Скрипт для быстрого тестирования сервера

API_TOKEN="03cba5ea"
INITIAL_TOKEN="PASTE INITIAL TOKEN HERE"
DEVICE_UUID="fe9883696cbcffff"

echo "🚀 Запуск Gates Opener Server..."

# Остановить предыдущий процесс если есть
pkill -f gates-opener-server-ktor

# Собрать если нужно
if [ ! -f "build/bin/macosArm64/releaseExecutable/gates-opener-server-ktor.kexe" ]; then
    echo "📦 Сборка приложения..."
    ./gradlew linkReleaseExecutableMacosArm64
fi

# Запустить сервер
echo "🔄 Запуск сервера на порту 8080..."
./build/bin/macosArm64/releaseExecutable/gates-opener-server-ktor.kexe \
  --api-token "$API_TOKEN" \
  --initial-token "$INITIAL_TOKEN" \
  --device-uuid "$DEVICE_UUID" &

SERVER_PID=$!
echo "📋 Сервер запущен с PID: $SERVER_PID"

# Ждем запуска
sleep 3

# Тестируем health check
echo "🏥 Тестируем health check..."
curl -s http://localhost:8080/health | jq . || curl -s http://localhost:8080/health

echo ""
echo "🚪 Тестируем открытие ворот..."
curl -s -X POST http://localhost:8080/open-door \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doorphone_id": "3836", "door_number": "2"}' | jq . || \
curl -s -X POST http://localhost:8080/open-door \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doorphone_id": "3836", "door_number": "2"}'

echo ""
echo "🏠 Тестируем открытие подъезда..."
curl -s -X POST http://localhost:8080/open-door \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doorphone_id": "3770", "door_number": "2"}' | jq . || \
curl -s -X POST http://localhost:8080/open-door \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"doorphone_id": "3770", "door_number": "2"}'

echo ""
echo "✨ Тестирование завершено!"
echo "🛑 Для остановки сервера выполните: kill $SERVER_PID"
