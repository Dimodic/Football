# FootballStats (SStats API)

Android-приложение на Jetpack Compose, которое показывает:
- Home: список ближайших матчей (следующие ~2 дня)
- Search: фильтрация по лиге + Upcoming/Past, поиск по строке
- Match Details: экран деталей матча с полями Country/Status/Match ID и API endpoints

## Требования
- Android Studio (актуальная версия)
- JDK 17
- Android SDK (compileSdk/targetSdk указаны в Gradle)

## Настройка API-ключа (обязательно)
Добавьте ключ в файл `local.properties` (в корне проекта):

SSTATS_API_KEY=ВАШ_КЛЮЧ

Альтернатива для CI:
- переменная окружения `SSTATS_API_KEY`

## Запуск
1) Откройте проект в Android Studio  
2) Дождитесь Gradle Sync  
3) Run ▶

## Примечание по авторизации
Ключ добавляется в каждый запрос как query-параметр `apikey` (через OkHttp interceptor).