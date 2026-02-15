# IPC коммуникация

#ipc #communication #events

## Что такое IPC

**IPC** (Inter-Process Communication) — механизм обмена сообщениями между Main и Renderer процессами.

> [!info] Аналогия
> - Android: `BroadcastReceiver` / `Intent` / `Binder`
> - iOS: `NotificationCenter` / XPC / `Delegate`

Renderer process (sandbox) **не может** напрямую вызывать Node.js API. Вся коммуникация — через IPC-каналы.

---

## Два паттерна IPC

### 1. Fire-and-forget (send → on)

```
Renderer ──send──→ Main
                   (обработать, нет ответа)
```

```typescript
// Renderer (через preload):
ipcRenderer.send("mainWindow:minimize");

// Main:
ipcMain.on("mainWindow:minimize", (event) => {
  mainWindow.minimize();
});
```

> [!note] Аналог
> Android: `sendBroadcast()` / iOS: `NotificationCenter.post()`

### 2. Request-response (invoke → handle)

```
Renderer ──invoke──→ Main
                     (обработать)
Renderer ←─result──← Main
```

```typescript
// Renderer (через preload):
const version = await ipcRenderer.invoke("app:getVersion");

// Main:
ipcMain.handle("app:getVersion", (event) => {
  return app.getVersion();  // Возвращается как Promise
});
```

> [!note] Аналог
> Android: `bindService()` + `ServiceConnection` / iOS: XPC request-response

### 3. Main → Renderer (push)

```
Main ──send──→ Renderer
               (обработать)
```

```typescript
// Main:
mainWindow.webContents.send("mainWindow:stateChanged", { maximized: true });

// Renderer (через preload):
ipcRenderer.on("mainWindow:stateChanged", (event, state) => {
  windowMaximized.value = state.maximized;
});
```

> [!note] Аналог
> Android: `LiveData.observe()` / iOS: `@Published` → SwiftUI subscription

---

## Роль Preload-скрипта

Preload — **мост** между Main и Renderer. Безопасно экспонирует ограниченный API:

```typescript
// preload.ts
contextBridge.exposeInMainWorld("ytmd", {
  // Fire-and-forget
  minimizeWindow: () => ipcRenderer.send("mainWindow:minimize"),
  
  // Request-response
  memoryStore: {
    get: (key: string) => ipcRenderer.invoke("memoryStore:get", key)
  },
  
  // Push (подписка)
  handleWindowEvents: (callback) => {
    ipcRenderer.on("mainWindow:stateChanged", callback);
  }
});

// Использование в Vue-компоненте:
window.ytmd.minimizeWindow();
const loading = await window.ytmd.memoryStore.get("ytmViewLoading");
```

---

## Все IPC-каналы проекта

### Main Window

| Канал | Тип | Направление | Назначение |
|---|---|---|---|
| `mainWindow:minimize` | send/on | Renderer → Main | Свернуть окно |
| `mainWindow:maximize` | send/on | Renderer → Main | Развернуть |
| `mainWindow:restore` | send/on | Renderer → Main | Восстановить |
| `mainWindow:close` | send/on | Renderer → Main | Закрыть (или скрыть) |
| `mainWindow:requestWindowState` | send/on | Renderer → Main | Запросить состояние |
| `mainWindow:stateChanged` | send | Main → Renderer | Уведомление о смене состояния |

### Settings Window

| Канал | Тип | Направление | Назначение |
|---|---|---|---|
| `settingsWindow:open` | send/on | Renderer → Main | Открыть настройки |
| `settingsWindow:minimize` | send/on | Renderer → Main | Свернуть |
| `settingsWindow:close` | send/on | Renderer → Main | Закрыть |
| `settingsWindow:restartapplication` | send/on | Renderer → Main | Перезапуск приложения |
| `settingsWindow:stateChanged` | send | Main → Renderer | Состояние окна |

### YTM View

| Канал | Тип | Направление | Назначение |
|---|---|---|---|
| `ytmView:loaded` | send/on | YTM → Main | **Ключевой:** YTM готов |
| `ytmView:videoProgressChanged` | send/on | YTM → Main | Прогресс воспроизведения |
| `ytmView:videoStateChanged` | send/on | YTM → Main | Состояние (play/pause/buffer) |
| `ytmView:videoDataChanged` | send/on | YTM → Main | Метаданные трека |
| `ytmView:storeStateChanged` | send/on | YTM → Main | Очередь, громкость, like |
| `ytmView:switchFocus` | send/on | YTM ↔ Main | Переключение фокуса |
| `ytmView:navigateDefault` | send/on | Main Window → Main | Навигация на главную |
| `ytmView:recreate` | send/on | Main Window → Main | Пересоздать view |
| `ytmView:getIntegrationScripts` | invoke/handle | YTM → Main | Получить скрипты интеграций |
| `ytmView:navigationStateChanged` | send | Main → YTM | Состояние Back/Forward |
| `remoteControl:execute` | send | Main → YTM | Команды управления плеером |
| `ytmView:executeScript` | send | Main → YTM | Выполнить скрипт интеграции |

### Stores

| Канал | Тип | Направление | Назначение |
|---|---|---|---|
| `memoryStore:set` | send/on | Renderer → Main | Записать в memory store |
| `memoryStore:get` | invoke/handle | Renderer → Main | Прочитать из memory store |
| `memoryStore:stateChanged` | send | Main → All renderers | Broadcast изменений |
| `settings:set` | send/on | Settings → Main | Записать настройку |
| `settings:get` | invoke/handle | Any → Main | Прочитать настройку |
| `settings:reset` | invoke/handle | Settings → Main | Сбросить настройку |
| `settings:stateChanged` | send | Main → Settings + YTM | Broadcast изменений |

### Safe Storage & App

| Канал | Тип | Направление | Назначение |
|---|---|---|---|
| `safeStorage:encryptString` | invoke/handle | Settings → Main | Шифрование |
| `safeStorage:decryptString` | invoke/handle | Settings → Main | Расшифровка |
| `app:getVersion` | invoke/handle | Settings → Main | Версия приложения |
| `app:checkForUpdates` | send/on | Settings → Main | Проверка обновлений |
| `app:restartApplicationForUpdate` | send/on | Any → Main | Перезапуск для обновления |

---

## Безопасность IPC

Каждый обработчик проверяет `event.sender`:

```typescript
ipcMain.on("mainWindow:minimize", event => {
  if (mainWindow !== null) {
    if (event.sender !== mainWindow.webContents) return;  // ПРОВЕРКА!
    mainWindow.minimize();
  }
});
```

> [!warning] Зачем проверка sender?
> Без проверки любой renderer мог бы вызвать любой IPC-обработчик.
> Это как проверка permissions в Android BroadcastReceiver.

---

## Поток данных при воспроизведении

```
YouTube Music (DOM)
    │
    ▼ hookPlayerApiEvents → videoDataChanged
    │
YTM View preload
    │
    ▼ ipcRenderer.send("ytmView:videoDataChanged", details)
    │
Main Process (index.ts)
    │
    ├──▶ playerStateStore.updateVideoDetails()
    │       │
    │       ├──▶ CompanionServer → WebSocket broadcast
    │       ├──▶ DiscordPresence → Rich Presence update
    │       ├──▶ LastFM → Scrobble
    │       └──▶ Notifications → OS notification
    │
    └──▶ lastVideoId = videoDetails.videoId  (для state saving)
```

---

Далее: [[11-Интеграции/Интеграции|Интеграции →]]
