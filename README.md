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
