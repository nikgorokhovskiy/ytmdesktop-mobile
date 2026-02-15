# Electron для мобильного разработчика

#electron #desktop #processes

## Что такое Electron

**Electron** — фреймворк для создания десктопных приложений на веб-технологиях (HTML/CSS/JS). Внутри — **Chromium** (браузер) + **Node.js** (backend).

> [!info] Аналогия
> Electron = Android App (Kotlin) + встроенный WebView, но наоборот:
> основной UI — это "WebView", а native-часть — сервисный слой.

Проект использует **Electron 38**.

---

## Процессная модель

```
┌────────────────────────────────┐
│        MAIN PROCESS            │  ← Node.js (backend)
│  - Управление окнами           │     Аналог: AppDelegate + Services
│  - Файловая система            │
│  - Нативные API                │
│  - IPC-хаб                     │
│  - Интеграции                  │
└──────────┬─────────────────────┘
           │ IPC (inter-process communication)
           │
┌──────────┴─────────────────────┐
│      RENDERER PROCESSES        │  ← Chromium (frontend)
│  - Каждое окно = отдельный     │     Аналог: Activity/Fragment + UI
│    процесс Chromium            │
│  - HTML/CSS/Vue.js             │
│  - Sandbox (нет доступа к FS)  │
└────────────────────────────────┘
```

### Важное правило
- **Main Process** может всё: файлы, сеть, нативные API, создание окон
- **Renderer Process** изолирован (sandbox) — не может ничего кроме UI
- Связь между ними — только через **IPC** (см. [[10-IPC коммуникация/IPC|IPC]])

---

## BrowserWindow

Окно приложения. Каждое окно — отдельный renderer process.

```typescript
mainWindow = new BrowserWindow({
  width: 1280,
  height: 720,
  frame: false,              // Без нативного заголовка (кастомный тайтлбар)
  show: false,               // Не показывать сразу (показать после ready-to-show)
  icon: getIconPath("ytmd.png"),
  titleBarStyle: "hidden",   // macOS: скрыть заголовок, но оставить светофор
  titleBarOverlay: {
    color: "#000000",
    symbolColor: "#BBBBBB",
    height: 36
  },
  webPreferences: {
    sandbox: true,            // Изоляция (БЕЗОПАСНОСТЬ)
    contextIsolation: true,   // Preload в отдельном контексте
    preload: path.join(__dirname, "preload.js"),
    devTools: true
  }
});

// Показать когда готово
mainWindow.on("ready-to-show", () => mainWindow.show());

// Загрузить UI
mainWindow.loadURL("http://localhost:5173/windows/main/index.html"); // dev
mainWindow.loadFile("dist/renderer/windows/main/index.html");        // prod
```

| Параметр | Аналог |
|---|---|
| `frame: false` | Кастомный ActionBar/NavigationBar |
| `show: false` → `ready-to-show` | Splash screen пока UI готовится |
| `sandbox: true` | Android sandbox / iOS sandbox |
| `preload` | Service binding / Protocol |

---

## BrowserView

Встроенный "под-браузер" внутри окна. Используется для YouTube Music.

```typescript
ytmView = new BrowserView({
  webPreferences: {
    sandbox: true,
    contextIsolation: true,
    partition: "persist:ytmview",    // Отдельная сессия (cookies, storage)
    preload: path.join(__dirname, "ytmview/preload.js")
  }
});

// Позиционирование внутри окна (ниже тайтлбара)
mainWindow.addBrowserView(ytmView);
ytmView.setBounds({
  x: 0,
  y: 36,  // Высота тайтлбара
  width: mainWindow.getContentBounds().width,
  height: mainWindow.getContentBounds().height - 36
});

// Загрузка YouTube Music
ytmView.webContents.loadURL("https://music.youtube.com/");
```

> [!info] BrowserView vs WebView
> `BrowserView` = `WKWebView` (iOS) / `WebView` (Android)
> Но работает как **отдельный процесс** с собственной сессией.

---

## Preload-скрипты

Мост между Main и Renderer. Выполняется **до** загрузки страницы, в изолированном контексте.

```typescript
// preload.ts
import { contextBridge, ipcRenderer } from "electron";

// Безопасно экспонирует API в renderer
contextBridge.exposeInMainWorld("ytmd", {
  minimizeWindow: () => ipcRenderer.send("mainWindow:minimize"),
  maximizeWindow: () => ipcRenderer.send("mainWindow:maximize"),
  closeWindow: () => ipcRenderer.send("mainWindow:close"),
  
  memoryStore: {
    get: (key: string) => ipcRenderer.invoke("memoryStore:get", key),
    onStateChanged: (callback) => {
      ipcRenderer.on("memoryStore:stateChanged", (_, newState, oldState) => {
        callback(newState, oldState);
      });
    }
  }
});
```

В renderer-процессе (Vue-компонент):
```typescript
// Доступ через window.ytmd
window.ytmd.minimizeWindow();
const value = await window.ytmd.memoryStore.get("ytmViewLoading");
```

> [!note] Зачем preload?
> Renderer process в sandbox **не имеет** доступа к Node.js API.
> Preload — единственный способ дать ему контролируемый доступ.
> Аналог: Android Binder / iOS XPC.

---

## Ключевые Electron API

### app — жизненный цикл приложения

```typescript
app.on("ready", () => { });           // Приложение готово (= applicationDidFinishLaunching)
app.on("before-quit", () => { });     // Перед закрытием (= applicationWillTerminate)
app.on("window-all-closed", () => {}); // Все окна закрыты
app.on("activate", () => { });        // macOS: клик по dock (= applicationDidBecomeActive)
app.quit();                            // Завершить
app.relaunch();                        // Перезапуск
```

### Tray — системный трей

```typescript
tray = new Tray(iconPath);
tray.setContextMenu(Menu.buildFromTemplate([
  { label: "Show", click: () => mainWindow.show() },
  { label: "Quit", click: () => app.quit() }
]));
```

### globalShortcut — глобальные горячие клавиши

```typescript
globalShortcut.register("CommandOrControl+Shift+P", () => {
  ytmView.webContents.send("remoteControl:execute", "playPause");
});
```

### safeStorage — шифрованное хранилище

```typescript
const encrypted = safeStorage.encryptString("secret");   // → Buffer
const decrypted = safeStorage.decryptString(encrypted);   // → "secret"
// Аналог: Keychain (iOS) / EncryptedSharedPreferences (Android)
```

### dialog — нативные диалоги

```typescript
const result = dialog.showMessageBoxSync({
  type: "question",
  buttons: ["Yes", "No"],
  message: "Are you sure?"
});
// Аналог: UIAlertController / AlertDialog
```

### autoUpdater — автообновления

```typescript
autoUpdater.setFeedURL({ url: "https://update.electronjs.org/..." });
autoUpdater.checkForUpdates();
autoUpdater.on("update-downloaded", () => autoUpdater.quitAndInstall());
// Только Windows (macOS требует code signing)
```

---

## Безопасность

| Мера | Значение | Назначение |
|---|---|---|
| `sandbox: true` | Все renderer | Изоляция от ОС |
| `contextIsolation: true` | Все renderer | Preload в отдельном контексте |
| `nodeIntegration: false` | По умолчанию | Нет Node.js в renderer |
| ASAR integrity | Fuses | Проверка целостности бандла |
| `event.sender` check | Все IPC | Проверка источника сообщений |

---

Далее: [[09-Lifecycle приложения/Lifecycle|Lifecycle приложения →]]
