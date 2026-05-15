# 🎮 Words Builder — Crossword & Word Wheel Game

[English](#english) | [Русский](#русский) | [Español](#español)

---

## English

A vibrant, mobile crossword puzzle game built with **Android Jetpack Compose**. Players connect scrambled letters on a 3D physical word wheel to unlock words hidden inside a dynamic crossword matrix.

### 🌟 Key Features
* **3D Word Wheel:** Custom-built circular keyboard that animates (scales) upon touch and links letters via animated lightning sparks.
* **Aesthetic Crosswords:** Tiles feature an organic 3D-esque look with physical depths, rounded edges, and borders.
* **Advanced Viewport Control:** Supports high-sensitivity Pinch-to-Zoom and Double-Tap focusing directly onto the clicked cell.
* **Passive Income System:** Earn coins in real-time even when offline. Claim rewards directly from the main menu with a neat coin explosion particle effect.
* **Word Dictionary Dialogs:** Long-press any cell to review definitions. Unsolved words remain securely masked behind `?` placeholders to prevent cheating.
* **Multi-Language Campaigns:** Includes 10 handcrafted escalating levels for English, Russian, and Spanish.

### 🛠️ Architecture & Tech Stack
* **UI Framework:** 100% Jetpack Compose (Declarative UI).
* **State Management:** Reactive Compose Architecture (`remember`, `mutableStateOf`, `SnapshotStateList`).
* **Animation Engine:** Hardware-accelerated transitions via `Animatable` and Canvas-level GPU rendering.
* **Data Persistence:** Local progress and stats stored securely using Android `SharedPreferences` and `Gson` serialization.

---

## Русский

Яркая мобильная игра-головоломка, созданная на базе **Android Jetpack Compose**. Игроки соединяют перепутанные буквы на трехмерном тактильном колесе, чтобы разгадать слова, скрытые в динамической сетке кроссворда.

### 🌟 Ключевые Особенности
* **Объемное колесо букв:** Кастомная круговая клавиатура, которая сочно увеличивается при тапе и соединяет буквы "магическими" разрядами молний.
* **Мультяшные плитки:** Ячейки кроссворда выполнены в виде объемных фишек настольных игр со скругленными краями, контурами и 3D-подложкой.
* **Интеллектуальная камера:** Плавное масштабирование двумя пальцами (Pinch-to-Zoom) и мгновенное приближение по двойному тапу точно в точку клика.
* **Пассивный заработок:** Генерация монет в реальном времени, работающая даже при выключенной игре. Сбор наград сопровождается эффектом фонтана золота.
* **Встроенный словарь:** Долгий тап по кроссворду открывает диалог с толкованием слова. Неразгаданные буквы маскируются знаками `?`, защищая от читерства.
* **Локализованные кампании:** 10 проработанных уровней возрастающей сложности с уникальными файлами конфигураций для русского, английского и испанского языков.

### 🛠️ Архитектура и Технологии
* **Интерфейс:** 100% Jetpack Compose (Декларативный UI).
* **Управление стейтом:** Реактивная архитектура Compose (`remember`, `mutableStateOf`, `SnapshotStateList`).
* **Движок анимаций:** Аппаратное сглаживание переходов через `Animatable` и рендеринг физики частиц на уровне GPU.
* **Хранение данных:** Локальное сохранение прогресса и игровой статистики через `SharedPreferences` и сериализацию `Gson`.

---

## Español

Un vibrante juego de rompecabezas de palabras para móviles desarrollado nativamente con **Android Jetpack Compose**. Los jugadores conectan letras en una rueda tridimensional para desbloquear las palabras ocultas en la matriz del crucigrama.

### 🌟 Características Principales
* **Rueda de Palabras en 3D:** Teclado circular personalizado que se expande al tacto y conecta las letras mediante rayos mágicos animados.
* **Crucigramas Estilizados:** Las celdas poseen un aspecto orgánico en 3D con relieve físico, esquinas redondeadas y bordes limpios.
* **Control de Cámara Avanzado:** Soporta pellizco para hacer zoom (Pinch-to-Zoom) de alta sensibilidad y doble toque para enfocar directamente la celda pulsada.
* **Sistema de Ingresos Pasivos:** Gana monedas en tiempo real incluso sin conexión. Reclama las recompensas con un poderoso efecto de explosión de monedas.
* **Diccionario Integrado:** Mantén presionado cualquier celda para ver las definiciones. Las palabras ocultas muestran signos de transformación `?` para evitar trampas.
* **Campañas Multilingües:** Incluye 10 niveles diseñados a mano con dificultad escalonada para español, inglés y ruso.

### 🛠️ Arquitectura y Tecnologías
* **Interfaz de Usuario:** 100% Jetpack Compose (UI Declarativa).
* **Gestión de Estado:** Arquitectura reactiva de Compose (`remember`, `mutableStateOf`, `SnapshotStateList`).
* **Motor de Animación:** Transiciones aceleradas por hardware mediante `Animatable` y renderizado de partículas en la GPU.
* **Persistencia de Datos:** Progreso local y estadísticas almacenadas de forma segura a través de `SharedPreferences` y serialización con `Gson`.
