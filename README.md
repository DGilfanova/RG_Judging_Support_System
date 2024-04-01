# RG_Judging_Support_System

## Локальный запуск

#### Необходимо запустить все сервисы из docker-compose.yaml:
```
docker compose -f %User%/RG_Judging_Support_System/docker-compose.yaml -p rg_judging_support_system up -d
```

#### Для работы с инструментом через UI перейдите по:
http://localhost:8080/


#### Доступные методы можно посмотреть в Swagger:
http://localhost:8080/swagger-ui/index.html

1. GET /api/v1/element - получение всех элементов, по которым доступно оценивание
2. POST /api/v1/element/evaluate - метод оценивания элемента. В качестве параметра необходимо передать elementId из GET-ручки и видео элемента. В папке test-videos есть примеры, по которым можно протестировать систему.