# iOS Calculator Clone

Точная копия калькулятора Apple iOS — Progressive Web App (PWA) с поддержкой Capacitor для сборки APK под Android.

## Возможности

- Точный дизайн iOS калькулятора (цвета, шрифты, кнопки)
- Полная математическая логика (AC/C, %, +/-, цепочки операций)
- PWA — устанавливается на Android/iOS как приложение
- Работает офлайн (Service Worker)
- Поддержка клавиатуры на десктопе
- Адаптивная вёрстка (портрет/ландшафт)

---

## Быстрый старт (локальный запуск)

```bash
# 1. Установить зависимости
npm install

# 2. Запустить локальный сервер
npm start
# Открыть: http://localhost:3000
```

---

## Загрузка на GitHub

```bash
# Инициализировать git
git init
git add .
git commit -m "feat: iOS Calculator clone — PWA + Capacitor"

# Создать репозиторий (нужен GitHub CLI)
gh repo create ios-calculator --public --push

# ИЛИ через git remote вручную:
git remote add origin https://github.com/ВАШ_ЛОГИН/ios-calculator.git
git branch -M main
git push -u origin main
```

---

## Сборка APK через Capacitor

### Требования
- Node.js 18+
- Java JDK 17 (`java -version`)
- Android Studio (или Android SDK + `ANDROID_SDK_ROOT`)

### Шаги

```bash
# 1. Установить зависимости
npm install

# 2. Добавить платформу Android (только первый раз)
npx cap add android

# 3. Синхронизировать файлы
npx cap sync android

# 4. Собрать debug APK
cd android
./gradlew assembleDebug

# APK будет здесь:
# android/app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (для Google Play)

```bash
# Создать keystore (один раз)
keytool -genkey -v -keystore calculator.keystore \
  -alias calculator -keyalg RSA -keysize 2048 -validity 10000

# Собрать release
cd android
./gradlew assembleRelease

# Подписать APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore ../calculator.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk calculator

# Выровнять
zipalign -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  calculator-release.apk
```

---

## Структура проекта

```
ios-calculator/
├── index.html           ← Главный файл (весь UI + JS)
├── manifest.json        ← PWA манифест
├── sw.js               ← Service Worker (офлайн)
├── capacitor.config.json ← Настройки Capacitor
├── package.json
├── README.md
└── icons/
    ├── icon-72.png
    ├── icon-96.png
    ├── icon-128.png
    ├── icon-144.png
    ├── icon-152.png
    ├── icon-167.png
    ├── icon-180.png
    ├── icon-192.png
    ├── icon-384.png
    └── icon-512.png
```

---

## Установка как PWA на Android

1. Открыть сайт в Chrome на Android
2. Нажать меню (три точки) → «Добавить на главный экран»
3. Приложение установится без APK

---

## Лицензия

MIT
