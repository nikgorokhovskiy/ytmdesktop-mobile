# Mobile — Innertube API

#mobile #api #innertube #youtube #auth

## Что такое Innertube

**Innertube** — приватный JSON API YouTube/YouTube Music. Не документирован официально, но широко используется альтернативными клиентами (InnerTune, ViMusic, Piped).

> [!info] Связь с Desktop
> Desktop-приложение загружает `music.youtube.com` в BrowserView — веб-страница сама вызывает Innertube API.
> Mobile-приложение вызывает Innertube API **напрямую** через Ktor HTTP-клиент, эмулируя клиент `ANDROID_MUSIC`.

---

## Конфигурация клиента

| Параметр | Значение |
|---|---|
| **Base URL** | `https://music.youtube.com/youtubei/v1/` |
| **Client Name** | `ANDROID_MUSIC` |
| **Client Version** | `7.27.52` |
| **API Key** | `AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI` |
| **User-Agent** | `com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 14)` |

Файл: `shared/src/commonMain/kotlin/.../data/remote/InnertubeEndpoints.kt`

> [!warning] Нестабильный API
> Innertube — **неофициальный** API. Google может менять формат ответов, версии клиентов и ключи. Альтернативные клиенты регулярно обновляют `clientVersion` и `API_KEY`.

---

## Endpoints

| Endpoint | Метод | Назначение | Реализация |
|---|---|---|---|
| `/player` | POST | Получить stream URL, метаданные трека | ✅ Реализован |
| `/search` | POST | Поиск музыки | ✅ Реализован (парсинг) |
| `/browse` | POST | Каталог: альбомы, плейлисты, home feed | 🔲 Stub |
| `/next` | POST | Очередь воспроизведения, related tracks | 🔲 Stub |
| `/music/get_search_suggestions` | POST | Автодополнение поиска | 🔲 Stub |

---

## Формат запроса

Каждый запрос содержит `context` с информацией о клиенте:

```json
{
  "context": {
    "client": {
      "clientName": "ANDROID_MUSIC",
      "clientVersion": "7.27.52",
      "hl": "en",
      "gl": "US",
      "platform": "MOBILE",
      "androidSdkVersion": 34,
      "userAgent": "com.google.android.apps.youtube.music/7.27.52 ..."
    }
  },
  "videoId": "dQw4w9WgXcQ"
}
```

Реализация в `InnertubeModels.kt`:

```kotlin
@Serializable
data class PlayerRequest(
    val videoId: String,
    val context: InnertubeContext = InnertubeContext(ClientContext()),
)
```

---

## /player — получение аудиопотока

Основной endpoint для воспроизведения. Возвращает:
- `videoDetails` — метаданные (title, author, duration, thumbnail)
- `streamingData.adaptiveFormats` — список аудио/видео потоков с прямыми URL

```kotlin
suspend fun getStreamInfo(videoId: String): StreamInfo? {
    val response = innertubeApi.player(videoId)
    val format = response.streamingData
        ?.adaptiveFormats
        ?.filter { it.mimeType.startsWith("audio/") }
        ?.maxByOrNull { it.bitrate }
        ?: return null

    return StreamInfo(
        url = format.url ?: return null,
        mimeType = format.mimeType,       // "audio/webm; codecs=\"opus\""
        bitrate = format.bitrate,          // 128000
        codec = extractCodec(format.mimeType),
        contentLength = format.contentLength?.toLongOrNull(),
    )
}
```

> [!tip] Почему ANDROID_MUSIC?
> Клиент `ANDROID_MUSIC` возвращает **прямые URL** аудиопотоков без необходимости деобфускации подписей (signature cipher). Это ключевое преимущество перед `WEB` клиентом.

### Типичные аудиоформаты

| itag | Формат | Битрейт | Кодек |
|---|---|---|---|
| 140 | audio/mp4 | 128 kbps | AAC |
| 141 | audio/mp4 | 256 kbps | AAC |
| 251 | audio/webm | 160 kbps | Opus |
| 250 | audio/webm | 70 kbps | Opus |
| 249 | audio/webm | 50 kbps | Opus |

---

## /search — поиск

Возвращает результаты сгруппированные по категориям (songs, albums, artists, playlists):

```kotlin
suspend fun search(query: String): List<SearchResult> {
    val response = innertubeApi.search(query)
    // Парсинг: tabbedSearchResultsRenderer → tabs → sectionListRenderer
    //   → musicShelfRenderer → contents → musicResponsiveListItemRenderer
    return parseSearchResponse(response)
}
```

Структура ответа (упрощённо):

```
SearchResponse
  └── contents
      └── tabbedSearchResultsRenderer
          └── tabs[0]
              └── tabRenderer.content
                  └── sectionListRenderer.contents[]
                      └── musicShelfRenderer
                          ├── title: "Songs" / "Albums" / ...
                          └── contents[]
                              └── musicResponsiveListItemRenderer
                                  ├── flexColumns[0] → title
                                  ├── flexColumns[1] → artist
                                  ├── thumbnail → image URL
                                  └── playlistItemData → videoId
```

> [!note] Парсинг
> Ответы Innertube — глубоко вложенный JSON. Модели в `InnertubeModels.kt` отражают эту структуру. `ignoreUnknownKeys = true` в Json конфигурации — обязателен, т.к. ответ содержит множество неиспользуемых полей.

---

## Аутентификация — Cookie-based

### Принцип

1. Пользователь логинится через **WebView** на `music.youtube.com`
2. Из WebView извлекаются cookies (`SAPISID`, `SID`, `__Secure-1PSID`, ...)
3. Cookies сохраняются в `CookieStore` (SharedPreferences / NSUserDefaults)
4. При запросах к Innertube формируется заголовок `Authorization: SAPISIDHASH`

### SAPISIDHASH

Формат: `SAPISIDHASH {timestamp}_{hash}`

```kotlin
fun generateSapisidHash(sapisid: String, origin: String, timestamp: Long): String {
    val input = "$timestamp $sapisid $origin"
    return sha1(input)  // SHA-1 хеш
}

// Результат: "Authorization: SAPISIDHASH 1708000000_a1b2c3d4e5..."
```

### Headers для аутентифицированного запроса

| Header | Значение | Пример |
|---|---|---|
| `Authorization` | `SAPISIDHASH {ts}_{hash}` | `SAPISIDHASH 1708000000_abc123...` |
| `Cookie` | Все auth cookies | `SAPISID=xxx; SID=yyy; __Secure-1PSID=zzz` |
| `Origin` | `https://music.youtube.com` | — |
| `User-Agent` | Android Music client | `com.google.android.apps.youtube.music/...` |
| `X-Goog-Api-Key` | API ключ | `AIzaSyAO...` |

### Необходимые cookies

| Cookie | Обязателен | Назначение |
|---|---|---|
| `SAPISID` | ✅ | Основа для SAPISIDHASH |
| `SID` | ✅ | Session ID |
| `HSID` | ✅ | HTTP Session |
| `SSID` | ✅ | Secure Session |
| `APISID` | ✅ | API Session |
| `__Secure-1PSID` | ⚠️ | First-party secure SID |
| `__Secure-3PSID` | ⚠️ | Third-party secure SID |

### WebView Login — Android

```kotlin
// AuthWebView.kt — Compose + Android WebView
webView.loadUrl("https://music.youtube.com")

// В onPageFinished, когда URL = music.youtube.com:
val cookieString = CookieManager.getInstance().getCookie(url)
val cookies = parseCookieString(cookieString)
if (cookies.containsKey("SAPISID")) {
    onCookiesExtracted(cookies)
}
```

### WebView Login — iOS

```swift
// AuthView.swift — SwiftUI + WKWebView
let store = webView.configuration.websiteDataStore.httpCookieStore
store.getAllCookies { cookies in
    let map = cookies.filter { $0.domain.contains("youtube.com") }
        .reduce(into: [:]) { $0[$1.name] = $1.value }
    if map["SAPISID"] != nil {
        onCookiesExtracted(map)
    }
}
```

---

## Ktor HTTP Client — конфигурация

```kotlin
// SharedModule.kt
single {
    HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // критично для Innertube!
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }
}
```

> [!info] Платформенные движки
> Ktor использует разные HTTP-движки на разных платформах:
> - **Android**: OkHttp (`ktor-client-okhttp`)
> - **iOS**: NSURLSession (`ktor-client-darwin`)
>
> Движок подключается в `androidMain.dependencies` / `iosMain.dependencies` в `shared/build.gradle.kts`.

---

## Без аутентификации vs с аутентификацией

| Функция | Без auth | С auth |
|---|---|---|
| Поиск | ✅ | ✅ |
| Воспроизведение | ✅ (но могут быть ограничения) | ✅ |
| Home feed | ❌ (generic) | ✅ (персонализированный) |
| Плейлисты пользователя | ❌ | ✅ |
| Лайки | ❌ | ✅ |
| История | ❌ | ✅ |

---

Далее: [[16-Mobile плеер/Плеер|Плеер и фоновое воспроизведение →]]
