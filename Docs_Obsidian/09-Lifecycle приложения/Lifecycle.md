# Lifecycle приложения

#lifecycle #startup #shutdown

## Общая схема

```
[Запуск процесса]
    │
    ▼
[Фаза 1: Ранняя инициализация]
    │  Crash Reporter, Logging
    │  Squirrel Check (Windows)
    │  Sandbox, Menu
    │  Инстанцирование интеграций
    │  Single Instance Lock
    │  Протокол ytmd://
    │  Memory Store + Auto Updater + Persistent Store
    │  Hardware / Speaker Fill
    │
    ▼
═══ app.on("ready") ═══
    │
    ▼
[Фаза 2: Главная инициализация]
    │  First Run / v1 Migration
    │  Safe Storage Check
    │  Регистрация IPC-обработчиков (~30 шт.)
    │  Permission Handlers
    │  Global Shortcuts
    │  System Tray
    │  createMainWindow()  ─── пользователь видит загрузочный экран
    │  Auto Update Check  ─── блокирующий
    │  createYTMView()    ─── загрузка music.youtube.com
    │  Taskbar Features
    │  Запуск интеграций
    │
    ▼
═══ В YTM View (preload.ts) ═══
    │
    ▼
[Фаза 3: Загрузка YouTube Music]
    │  Хук Reflect.decorate → перехват YTM Store
    │  window.load
    │  Поллинг: __YTMD_HOOK__.ytmStore
    │  Поллинг: playerApi.isReady()
    │  UI расширения (навигация, кнопки)
    │  hookPlayerApiEvents
    │  Continue where you left off
    │  ipcRenderer.send("ytmView:loaded")
    │
    ▼
═══ Назад в Main Process ═══
    │
    ▼
[Фаза 4: Приложение работает]
    │  mainWindow.addBrowserView(ytmView)
    │  Экран загрузки скрывается
    │  ←── playerState events ←── ytmView
    │  ──→ integrations broadcast ──→ Discord/Last.fm
    │  ←── remote control ←── Companion API / Shortcuts / Tray
    │  state save каждые 5 минут
    │
    ▼
[Фаза 5: Завершение]
    │  app.on("before-quit") → saveState()
    │  window-all-closed (macOS: ничего / другие: quit)
    │  activate (macOS: пересоздание окон)
```

---

## Фаза 1: Ранняя инициализация

Код выполняется **до** `app.on("ready")` — при загрузке модуля `src/main/index.ts`.

### 1.1. Crash Reporter + Logging (строки 58-147)

```typescript
crashReporter.start({ uploadToServer: false });
log.initialize({ preload: true, spyRendererConsole: true });
log.errorHandler.startCatching({ showDialog: false, ... });
```

- Запуск crash reporter (без отправки)
- Настройка форматов логов (консоль и файл)
- Фильтр спам-сообщений от YTM
- Глобальный перехват необработанных ошибок

### 1.2. Squirrel Check (строка 150)

```typescript
if (electronSquirrelStartup) app.quit();
```

Windows: если запущено установщиком — мгновенный выход.

### 1.3. Sandbox + Menu (строки 157-162)

```typescript
app.enableSandbox();
Menu.setApplicationMenu(isDarwin ? builtMenu : null);
```

- Sandbox обязателен для всех renderer
- macOS: нативное меню (appMenu + editMenu для Cmd+C/V)
- Windows/Linux: `null` (для производительности)

### 1.4. Интеграции (строки 164-169)

```typescript
const companionServer = new CompanionServer();
const discordPresence = new DiscordPresence();
// ... и т.д.
```

Создаются объекты, но **не запускаются**. Запуск — в Фазе 2.

### 1.5. Single Instance Lock (строки 188-203)

```typescript
const gotTheLock = app.requestSingleInstanceLock();
if (!gotTheLock) app.exit(0);
```

Только один экземпляр приложения. При повторном запуске — фокус на существующее окно.

> [!note] Аналогия
> `singleTask` launch mode (Android) / UIApplicationSupportsMultipleScenes = false (iOS)

### 1.6. Persistent Store (строки 336-553)

```typescript
const store = new Conf<StoreSchema>({
  configName: "config",
  defaults: { ... },
  migrations: { ... }
});
```

- Конфигурация пишется в `config.json`
- Миграции при обновлении версий
- `onDidAnyChange()` — при изменении настроек включает/выключает интеграции

---

## Фаза 2: app.on("ready") (строка 1332)

> [!important] Аналогия
> `app.on("ready")` = `applicationDidFinishLaunching` (iOS) / `onCreate()` в Application (Android)

### Порядок инициализации

1. **First Run Check** — если `.first-run` не существует, предлагает миграцию с v1
2. **Safe Storage** — проверка доступности шифрования ОС
3. **IPC Handlers** — регистрация ~30 обработчиков (см. [[10-IPC коммуникация/IPC|IPC]])
4. **Permission Handlers** — YTM view разрешён только fullscreen
5. **Global Shortcuts** — регистрация горячих клавиш
6. **System Tray** — иконка + контекстное меню
7. **createMainWindow()** — пользователь видит окно с загрузочным экраном
8. **Auto Update Check** — блокирующий (ждёт результата)
9. **createYTMView()** — BrowserView загружает YouTube Music
10. **Taskbar Features** — кнопки в таскбаре Windows
11. **Start Integrations** — запуск включённых интеграций

---

## Фаза 3: Загрузка YouTube Music

### Хук YTM Store (preload.ts, строки 191-220)

```typescript
// Перехватываем Angular декоратор чтобы поймать Redux store
Object.defineProperty(Reflect, "decorate", {
  get: () => (...args) => {
    // Ловим объекты, ищем ytmusic-app с его store
    if (!window.__YTMD_HOOK__) {
      ytmdHookedObjects.push(args[1]);
    }
    return decorate(...args);
  }
});
```

### Последовательность после window.load

```
window.load
    │
    ├── 1. Поллинг (250мс): ждём __YTMD_HOOK__.ytmStore
    │
    ├── 2. Загрузка Material Symbols (иконки)
    │
    ├── 3. Поллинг (250мс): ждём playerApi.isReady()
    │
    ├── 4. Инициализация UI:
    │   ├── createStyleSheet()
    │   ├── createNavigationMenuArrows()    ← Back/Forward кнопки
    │   ├── createKeyboardNavigation()
    │   ├── createAdditionalPlayerBarControls() ← Library, Playlist, Sleep Timer
    │   ├── hideChromecastButton()
    │   └── hookPlayerApiEvents()           ← Подписка на события плеера
    │
    ├── 5. Continue where you left off      ← Навигация к последнему треку
    │
    └── 6. ipcRenderer.send("ytmView:loaded")  ← СИГНАЛ В MAIN PROCESS
```

### Момент "оживления" (строки 1572-1596 в index.ts)

```typescript
ipcMain.on("ytmView:loaded", event => {
  memoryStore.set("ytmViewLoading", false);     // Скрыть loading screen
  mainWindow.addBrowserView(ytmView);            // YouTube Music появляется!
  ytmView.setBounds({ x: 0, y: 36, ... });      // Под тайтлбаром
});
```

> [!important] Ключевой момент
> BrowserView создаётся и загружает YTM **до** присоединения к окну.
> Присоединяется **только после** `ytmView:loaded`.
> Это создаёт плавный UX: пользователь видит загрузочный экран, а не белую страницу.

---

## Фаза 4: Работа

- **ytmView → Main**: прогресс видео, состояние, метаданные трека, очередь
- **Main → Integrations**: Discord обновляет статус, Last.fm скробблит
- **Companion Server → External**: REST API + WebSocket broadcast
- **State Saver**: каждые 5 минут сохраняет lastUrl, lastVideoId, lastPlaylistId
- **Auto-recover**: при крэше ytmView — автоматическое пересоздание

---

## Фаза 5: Завершение

```typescript
app.on("before-quit", () => {
  applicationQuitting = true;
  saveState();  // lastUrl, lastVideoId, lastPlaylistId → config.json
});

app.on("window-all-closed", () => {
  if (!isDarwin) app.quit();  // macOS: остаётся в доке
});

app.on("activate", () => {
  // macOS: пересоздать окна при клике на dock
  if (BrowserWindow.getAllWindows().length === 0) {
    createMainWindow();
    createYTMView();
  }
});
```

### macOS-специфика закрытия

```typescript
mainWindow.on("close", event => {
  if (!applicationQuitting && (hideToTrayOnClose || isDarwin)) {
    event.preventDefault();
    mainWindow.hide();  // Скрыть, не закрыть
  }
});
```

На macOS при нажатии X — окно **скрывается** (не уничтожается). Закрытие только через Cmd+Q или меню Quit.

---

Далее: [[10-IPC коммуникация/IPC|IPC коммуникация →]]
