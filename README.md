# VanillaAFK Plugin RU GUIDE

Простой и легкий плагин для AFK режима с кастомными сообщениями над головой.

## 📦 Установка

1. Скачайте файл `VanillaAFK.jar`
2. Поместите его в папку `plugins/` вашего сервера
3. Перезапустите сервер

## 🎮 Использование

### Основные команды:
- `/afk` - войти/выйти из AFK режима
- `/afk [текст]` - войти в AFK с кастомным текстом
- `/afk reload` - перезагрузить конфигурацию

### Примеры:
- `/afk` - AFK с текстом по умолчанию
- `/afk Отошел на обед` - AFK с кастомным текстом
- `/afk &cОтошел ненадолго` - AFK с цветным текстом

## ⚙️ Настройки
Все настройки находятся в файле `plugins/VanillaAFK/config.yml`:

### Сообщения:
```yaml
messages:
  afk-enabled: "&aВы вошли в режим AFK с текстом '&f<text>&a'"
  afk-disabled: "&aВы вышли из режима АФК"
  default-afk-text: "&7AFK"
```

## 🎨 Форматирование текста

Поддерживаются стандартные цветовые коды Minecraft:

### Цвета:
&0 - черный

&1 - темно-синий

&2 - темно-зеленый

&3 - темно-бирюзовый

&4 - темно-красный

&5 - пурпурный

&6 - золотой

&7 - серый

&8 - темно-серый

&9 - синий

&a - зеленый

&b - бирюзовый

&c - красный

&d - розовый

&e - желтый

&f - белый

### Форматирование:
&k - случайные символы

&l - жирный

&m - зачеркнутый

&n - подчеркнутый

&o - курсив

&r - сброс форматирования

## 🔐 Разрешения
vanillaafk.customtext - использование кастомного текста

vanillaafk.reload - перезагрузка конфигурации

## 📋 Особенности
Анимация песочных часов (можно отключить)

Поддержка цветового форматирования bukkit и HEX цветов

## 🐛 Поддержка
Если у вас возникли проблемы или предложения, создайте issue на GitHub.



# VanillaAFK Plugin ENG GUIDE

A simple and lightweight AFK plugin with custom overhead messages for Paper servers.

## 📦 Installation

1. Download the `VanillaAFK.jar` file
2. Place it in your server's `plugins/` folder
3. Restart the server

## 🎮 Usage

### Basic Commands:
- `/afk` - toggle AFK mode
- `/afk [text]` - enter AFK with custom text
- `/afk reload` - reload configuration

### Examples:
- `/afk` - AFK with default text
- `/afk Gone for lunch` - AFK with custom text
- `/afk &cBe back soon` - AFK with colored text

## ⚙️ Configuration
All settings are located in `plugins/VanillaAFK/config.yml`:

### Messages:
```yaml
messages:
  afk-enabled: "&aYou entered AFK mode with text '&f<text>&a'"
  afk-disabled: "&aYou exited AFK mode"
  default-afk-text: "&7AFK"
```

## 🎨 Text Formatting
Supports standard Minecraft color codes:

### Colors:
&0 - Black

&1 - Dark Blue

&2 - Dark Green

&3 - Dark Aqua

&4 - Dark Red

&5 - Purple

&6 - Gold

&7 - Gray

&8 - Dark Gray

&9 - Blue

&a - Green

&b - Aqua

&c - Red

&d - Pink

&e - Yellow

&f - White

### Formatting:
&k - Obfuscated

&l - Bold

&m - Strikethrough

&n - Underlined

&o - Italic

&r - Reset

## 🔐 Permissions
vanillaafk.customtext - use custom AFK text

vanillaafk.reload - reload plugin configuration

## 📋 Features
Custom overhead messages with color formatting

Animated sandclock emoji (configurable)

Movement detection to exit AFK mode

Support for both Bukkit colors and HEX colors

Easy configuration reload

Lightweight and optimized

## 🐛 Support
If you encounter any issues or have suggestions, please create an issue on GitHub.
