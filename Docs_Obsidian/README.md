# YouTube Music Desktop & Mobile App — Шпаргалка

Справочник по проекту **ytmdesktop** — Desktop (Electron) и Mobile (KMM) части.

> **Версия Desktop:** 2.0.10
> **Версия Mobile:** 0.1.0 (architectural foundation)
> **Desktop стек:** Electron 38 + Vue 3 + TypeScript 5.8 + Vite 5.4 + Fastify 5
> **Mobile стек:** Kotlin 2.1.10 + KMP + Jetpack Compose + SwiftUI + Ktor 3.1.1
> **Лицензия:** GPL-3.0 | **Автор:** NovusTheory

---

## Desktop — Навигация

| # | Тема | Описание |
|---|---|---|
| 01 | [[01-Зависимости и установка/Установка\|Установка]] | Node.js, Yarn, расширения IDE |
| 02 | [[02-Структура проекта/Архитектура\|Архитектура]] | Дерево каталогов, модули, процессы |
| 03 | [[03-Сборка и запуск/Сборка\|Сборка]] | dev/prod команды, сборка под macOS |
| 04 | [[04-Отладка/Отладка\|Отладка]] | Breakpoints, DevTools, дебаг Main/Renderer |
| 05 | [[05-Логирование/Логи\|Логи]] | electron-log, формат, расположение файлов |
| 06 | [[06-Синтаксис TypeScript/Синтаксис\|Синтаксис]] | TypeScript vs Kotlin vs Swift |
| 07 | [[07-Vue.js/Vue\|Vue.js]] | Компоненты, директивы, реактивность |
| 08 | [[08-Electron/Electron\|Electron]] | Процессная модель, BrowserWindow, BrowserView |
| 09 | [[09-Lifecycle приложения/Lifecycle\|Lifecycle]] | Фазы запуска, ready, загрузка YTM |
| 10 | [[10-IPC коммуникация/IPC\|IPC]] | Каналы, паттерны, безопасность |
| 11 | [[11-Интеграции/Интеграции\|Интеграции]] | Companion Server, Discord, Last.fm |

---

## Mobile (KMM) — Навигация

| # | Тема | Описание |
|---|---|---|
| 12 | [[12-Mobile зависимости и установка/Установка\|Установка]] | JDK, Android SDK, Xcode, Tuist, mise |
| 13 | [[13-Mobile архитектура/Архитектура\|Архитектура]] | KMP, Clean Architecture, shared-модуль |
| 14 | [[14-Mobile сборка и запуск/Сборка\|Сборка]] | Android/iOS сборка, Tuist, эмуляторы, подпись |
| 15 | [[15-Mobile Innertube API/Innertube\|Innertube API]] | YouTube Music API, аутентификация |
| 16 | [[16-Mobile плеер/Плеер\|Плеер]] | ExoPlayer, AVPlayer, background playback |

---

## Быстрые команды — Desktop

```bash
yarn install       # Установить зависимости
yarn start         # Запуск в dev-режиме
yarn make          # Сборка для текущей платформы
yarn lint          # Проверка линтером
yarn prettier:fix  # Автоформатирование
```

## Быстрые команды — Mobile

```bash
cd mobile

# Android
./gradlew :androidApp:assembleDebug     # Debug APK
./gradlew :androidApp:installDebug      # Установить на устройство
./gradlew :shared:build                 # Собрать shared-модуль

# iOS
cd iosApp
mise install                            # Установить Tuist (первый раз)
mise exec -- tuist generate --open      # Сгенерировать проект и открыть Xcode
# В Xcode: выбрать iosApp scheme → iPhone 16 → Cmd+R
```
