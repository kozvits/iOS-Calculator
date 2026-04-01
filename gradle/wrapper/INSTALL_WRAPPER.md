# Как получить gradle-wrapper.jar

Файл `gradle-wrapper.jar` не включён в репозиторий из-за ограничений сети.
Выполните одну из команд **до первого запуска** `./gradlew`:

## Вариант 1 — Android Studio (рекомендуется)
Откройте проект в Android Studio. IDE автоматически скачает wrapper и синхронизирует зависимости.

## Вариант 2 — Gradle CLI (если установлен глобально)
```bash
cd /path/to/IOSCalculator
gradle wrapper --gradle-version 8.6
```

## Вариант 3 — wget/curl
```bash
cd gradle/wrapper
curl -L "https://github.com/gradle/gradle/raw/v8.6.0/gradle/wrapper/gradle-wrapper.jar" \
     -o gradle-wrapper.jar
```

После этого:
```bash
chmod +x gradlew
./gradlew assembleDebug
```
