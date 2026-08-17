TASK TRACKER

Учебный проект на Java + Spring Boot.

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
repository — хранит задачи в Map.
dto — классы для входящих и исходящих данных.
exception — обработка ошибок.
model — основные классы Task, TaskStatus и TaskPriority.

Запуск:

Запустить класс TaskTrackerApplication.

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