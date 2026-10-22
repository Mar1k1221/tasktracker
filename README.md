TASK TRACKER

Учебный проект на Java + Spring Boot + PostgreSQL.

Что умеет программа:
- создавать задачи;
- получать все задачи;
- находить задачу по ID;
- фильтровать задачи по статусу, приоритету и тегу;
- изменять статус задачи;
- изменять приоритет задачи;
- добавлять теги;
- удалять задачи;
- показывать статистику;
- возвращать понятные ошибки.

Проект разделён на слои:

controller — принимает HTTP-запросы и возвращает ответы.
service — содержит основную бизнес-логику.
repository — хранит задачи в Базе данных.
dto — классы для входящих и исходящих данных.
exception — обработка ошибок.
model — основные классы Task, TaskStatus и TaskPriority.

Запуск:

Запустить класс TaskTrackerApplication.

Проверка без Docker:

```powershell
.\mvnw.cmd verify
```

Отчёт покрытия JaCoCo будет создан в `target/site/jacoco/index.html`.

Интеграционные тесты требуют запущенный Docker, потому что поднимают PostgreSQL 17 через Testcontainers:

```powershell
.\mvnw.cmd verify -Pintegration-tests
```

После запуска приложение работает на:
http://localhost:8080

Основные запросы:

GET /api/tasks
GET /api/tasks/{id}
POST /api/tasks
PATCH /api/tasks/{id}/status
PATCH /api/tasks/{id}/priority
POST /api/tasks/{id}/tags
DELETE /api/tasks/{id}
GET /api/tasks/statistics

Для проверки запросов используется файл requests.http.

Данные хранятся в памяти приложения, поэтому после перезапуска все задачи исчезают.

## Доступ и безопасность - неделя 10, день 1
HTTP-метод

Эндпоинт

Кто может обращаться

Правило Spring Security

GET

/api/tasks

Аутентифицированный пользователь

authenticated()

GET

/api/tasks/statistics

Аутентифицированный пользователь

authenticated()

GET

/api/tasks/{id}

Аутентифицированный пользователь

authenticated()

POST

/api/tasks

Аутентифицированный пользователь

authenticated()

PATCH

/api/tasks/{id}/status

Аутентифицированный пользователь

authenticated()

PATCH

/api/tasks/{id}/priority

Аутентифицированный пользователь

authenticated()

POST

/api/tasks/{id}/tags

Аутентифицированный пользователь

authenticated()

DELETE

/api/tasks/{id}

Аутентифицированный пользователь

authenticated()

PUT

/api/tasks/{id}/update

Аутентифицированный пользователь

authenticated()
Правила безопасности

POST /api/auth/** — публичный доступ через permitAll(), чтобы в будущем можно было реализовать регистрацию и вход.

/api/admin/** — доступ только для роли ADMIN через hasRole("ADMIN"). Административные эндпоинты будут добавлены позднее.

/api/tasks и /api/tasks/** — доступ только аутентифицированным пользователям через authenticated().

anyRequest().denyAll() — запрещает запросы, не совпавшие с предыдущими правилами.

Правила проверяются сверху вниз. Применяется первое совпавшее правило, поэтому более конкретные ограничения нужно располагать до широких разрешений.
Проверенные сценарии

Сценарий

Ожидаемый результат

Результат проверки

GET /api/tasks без учётных данных

401 Unauthorized

Пройден

GET /api/tasks с корректными учётными данными

200 OK

Пройден

GET /api/admin/tasks с ролью USER

403 Forbidden

Пройден

Примечание: /api/admin/tasks пока является проверкой правила безопасности, а не реализованным эндпоинтом контроллера.