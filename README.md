# Учебная платформа для онлайн курса

Итоговый проект «Веб-приложение (учебная платформа) на базе Spring Boot, которое использует Hibernate/JPA для доступа к базе данных PostgreSQL».

## Описание

Проект представляет собой web-приложение для управления онлайн-курсами на базе Spring Boot и Hibernate (JPA). 
Приложение позволяет создавать курсы со структурой (модули, уроки), записывать студентов, назначать задания, проводить тестирование и оценивать результаты. 

## Стек технологий

- **Java 21**.
- **Spring Boot 3.5.7**.
  - Spring Web (REST API).
  - Spring Data JPA (Hibernate).
  - Validation (для валидации и ограничений).
  - Testcontainers (для интеграционных тестов).
  - Lombok (для уменьшения boilerplate кода).
- **PostgreSQL 16** (СУБД)
- **Maven** (сборка, запуск и тестирование через `./mvnw`).

## Предусловия

- Java 21.
- Docker и Docker Compose (для PostgreSQL в dev-режиме)
- WSL2/Ubuntu (рекомендуется) или любая Unix-подобная система.

## Запуск приложения (dev-профиль)

### 1. Запустить PostgreSQL

```bash
docker compose up -d
```

### 2. Запустить само приложение

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

При первом запуске:
- Hibernate создаст схему БД (16 таблиц).
- `DevDataLoader` загрузит демо-данные.
- При повторных запусках демо-данные не дублируются (проверка пустоты курсов гарантирует идемпотентность).

После запуска приложение становится доступно на `http://localhost:8080`.


## Запуск тестов

```bash
./mvnw -DskipTests=false test
```

## REST API

Приложение предоставляет REST API для основных операций.

### Курсы и контент

### Обработка ошибок

- **400 Bad Request** - ошибки валидации, не найдено.
- **409 Conflict** - нарушение уникальности (повторная запись/сдача).
- **500 Internal Server Error** - внутренние ошибки.

## CI/CD

В проекте настроен GitHub Actions workflow (`.github/workflows/ci.yml`):
- Триггеры: push и pull_request на ветку `master`.
- Окружение: Ubuntu + Java 21 (Temurin) + Maven cache.
- Автоматический запуск всех тестов с Testcontainers.
- При ошибках сохраняются test reports как артефакты.

## Структура кода проекта

```
src/main/java/ru/mgubina/mashaschool/
├── config/          DevDataLoader для загрузки демо-данных (для профиля dev)
├── controller/      REST контроллеры
├── dto/             Request/Response DTO с валидацией
├── entity/          JPA-сущности
├── exception/       Обработка ошибок
├── repository/      Spring Data JPA репозитории
└── service/         Бизнес-логика

src/test/java/ru/mgubina/mashaschool/
├── controller/      REST-тесты с MockMvc
├── service/         Service-тесты с Testcontainers
└── mashaschoolApplicationTests.java (контекстный тест).
```


