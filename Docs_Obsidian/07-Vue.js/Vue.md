# Vue.js для мобильного разработчика

#vue #ui #components

## Что такое Vue

**Vue.js 3** — UI-фреймворк для веба. Проект использует **Composition API** с `<script setup>`.

> [!info] Аналогия
> Vue.js = **Jetpack Compose** (Android) / **SwiftUI** (iOS)
> Компонент `.vue` = `@Composable` функция / SwiftUI `View`

---

## Структура .vue файла

Каждый `.vue` файл — это **один компонент** с тремя секциями:

```vue
<script setup lang="ts">
// ЛОГИКА — аналог ViewModel / @Observable / @StateObject
import { ref, onBeforeMount } from "vue";

const count = ref(0);                    // Реактивная переменная
const increment = () => count.value++;   // Метод
</script>

<template>
  <!-- РАЗМЕТКА — аналог XML layout / SwiftUI body / @Composable -->
  <div>
    <p>{{ count }}</p>
    <button @click="increment">+</button>
  </div>
</template>

<style scoped>
/* СТИЛИ — аналог styles.xml / CSS */
/* scoped = стили только для этого компонента */
div { background: black; }
</style>
```

| Секция | Назначение | Аналог |
|---|---|---|
| `<script setup>` | Логика и состояние | ViewModel / @Observable |
| `<template>` | Разметка | XML layout / body в SwiftUI |
| `<style scoped>` | Стили (изолированные) | styles.xml / CSS Modules |

---

## Реактивность

### `ref()` — реактивная переменная

```typescript
import { ref } from "vue";

const count = ref(0);          // Создаём реактивную переменную
count.value++;                  // В JS-коде обращаемся через .value
// В template: {{ count }}      // В шаблоне .value НЕ нужен
```

| Vue | Compose | SwiftUI |
|---|---|---|
| `ref(0)` | `mutableStateOf(0)` | `@State var count = 0` |
| `count.value` | `count.value` | `count` |

### `reactive()` — реактивный объект

```typescript
import { reactive } from "vue";

const state = reactive({
  name: "hello",
  count: 0
});
state.count++;  // Без .value — напрямую
```

---

## Директивы в `<template>`

### Условный рендеринг: `v-if` / `v-else`

```vue
<span v-if="count > 0">Positive</span>
<span v-else>Zero or negative</span>
```

```kotlin
// Compose:
if (count > 0) Text("Positive") else Text("Zero or negative")
```

```swift
// SwiftUI:
if count > 0 { Text("Positive") } else { Text("Zero or negative") }
```

### Цикл: `v-for`

```vue
<li v-for="item in items" :key="item.id">{{ item.name }}</li>
```

```kotlin
// Compose:
LazyColumn { items(items) { item -> Text(item.name) } }
```

### Привязка атрибута: `:attr`

```vue
<img :src="imageUrl" />           <!-- :src = динамический атрибут -->
<div :class="{ active: isActive }"></div>  <!-- условный CSS-класс -->
```

> [!note] Двоеточие `:` = data binding
> `:src="variable"` → значение берётся из переменной
> `src="text"` → значение буквальная строка

### Обработчик события: `@event`

```vue
<button @click="handleClick">Click me</button>
<input @input="onInput" />
<form @submit.prevent="onSubmit">...</form>
```

| Vue | Android | iOS |
|---|---|---|
| `@click="fn"` | `onClick { fn() }` | `.onTapGesture { fn() }` |
| `@input="fn"` | `addTextChangedListener` | `onChange` |

### Двусторонняя привязка: `v-model`

```vue
<input v-model="searchText" />
<!-- searchText обновляется автоматически при вводе -->
```

```kotlin
// Compose:
TextField(value = searchText, onValueChange = { searchText = it })
```

### Вывод текста: `{{ }}`

```vue
<p>{{ message }}</p>
<p>{{ count + 1 }}</p>
<p>{{ isActive ? "Yes" : "No" }}</p>
```

---

## Props (входные параметры компонента)

```vue
<script setup lang="ts">
const props = defineProps({
  title: {
    type: String,
    default: null
  },
  hasHomeButton: Boolean,
  isMainWindow: {
    type: Boolean,
    default: false
  }
});
</script>

<template>
  <p>{{ title }}</p>
</template>
```

```kotlin
// Compose:
@Composable
fun TitleBar(
  title: String? = null,
  hasHomeButton: Boolean = false,
  isMainWindow: Boolean = false
) { }
```

### Использование компонента

```vue
<TitleBar
  title="Settings"
  :has-home-button="true"
  :is-main-window="false"
/>
```

---

## Lifecycle хуки

| Vue | Android (Compose) | iOS (SwiftUI) |
|---|---|---|
| `onBeforeMount()` | — | — |
| `onMounted()` | `LaunchedEffect(Unit)` | `.onAppear` |
| `onBeforeUnmount()` | `DisposableEffect` | `.onDisappear` |
| `onUnmounted()` | cleanup в DisposableEffect | — |

```typescript
import { onBeforeMount, onMounted } from "vue";

onBeforeMount(() => {
  // Вызывается ДО отрисовки
});

onMounted(() => {
  // Вызывается ПОСЛЕ отрисовки — загрузка данных, подписки
});
```

---

## Slots (аналог children / content)

```vue
<!-- Родитель -->
<TitleBar>
  <template #app-buttons>
    <button>Custom Button</button>
  </template>
</TitleBar>

<!-- TitleBar.vue -->
<div class="right">
  <slot name="app-buttons"></slot>  <!-- Здесь появится Custom Button -->
</div>
```

```kotlin
// Compose:
@Composable
fun TitleBar(appButtons: @Composable () -> Unit) {
  Row { appButtons() }
}

TitleBar { Button(onClick = {}) { Text("Custom Button") } }
```

---

## Компоненты в этом проекте

| Компонент | Файл | Назначение |
|---|---|---|
| **TitleBar** | `components/TitleBar.vue` | Кастомный заголовок окна |
| **YTMViewLoading** | `components/YTMViewLoading.vue` | Экран загрузки YTM |
| **YTMDSetting** | `components/YTMDSetting.vue` | Элемент настройки (checkbox, slider, select) |
| **KeybindInput** | `components/KeybindInput.vue` | Ввод горячей клавиши |
| **Settings** | `windows/settings/Settings.vue` | Страница настроек |
| **Auth** | `windows/authorize-companion/Auth.vue` | Диалог авторизации |

---

Далее: [[08-Electron/Electron|Electron →]]
