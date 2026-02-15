# Outbox Starter

Минимальный starter для outbox transaction. Бизнес-код пишет событие в таблицу `outbox_message` внутри своей транзакции. Фоновый паблишер отправляет его в Kafka и переводит статус в `SENT` после успешной отправки.

## Как подключить

Добавьте зависимость на этот модуль.

## Конфигурация (в приложении-потребителе)

В стартере нет собственного `application.yaml`. Конфигурация задается в приложении, которое его подключает.

### База данных

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/outbox
    username: outbox
    password: outbox
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```

### Kafka

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### Outbox-настройки

```yaml
outbox:
  topic: outbox.events
  publisher:
    enabled: true
    batch-size: 100
    poll-interval-ms: 1000
    send-timeout-ms: 5000
```

## Как пользоваться

Инжектируйте `OutboxService` и вызывайте `enqueue(messageKey, payload)`:

```java
outboxService.enqueue("order-123", "{\"event\":\"ORDER_CREATED\"}");
```

## Миграции БД (Liquibase)

В стартере есть changelog: `src/main/resources/db/changelog/outbox-changelog.xml`.

Чтобы применить миграцию в вашем сервисе:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/outbox-changelog.xml
```

Если вы используете общий `changelog-master.yaml`, подключите этот файл через `include`.

## Структура таблицы `outbox_message`

Колонки и назначения:

- `id` (uuid, PK): уникальный идентификатор сообщения.
- `topic` (varchar(255)): Kafka topic, куда отправляется сообщение.
- `message_key` (varchar(255), nullable): ключ сообщения в Kafka.
- `payload` (text): полезная нагрузка.
- `status` (varchar(32)): состояние отправки (`PENDING` или `SENT`).
- `created_at` (timestamp): время создания записи.
- `sent_at` (timestamp, nullable): время успешной отправки.
- `version` (bigint): оптимистическая версия для JPA.

Индекс:

- `idx_outbox_message_status_created` по (`status`, `created_at`) ускоряет выборку ожидающих сообщений.

## Методы для использования

Минимальный контракт — один метод:

- `OutboxService.enqueue(messageKey, payload)`

Поведение:

- Сохраняет запись со статусом `PENDING` в текущей транзакции.
- `topic` берется из `outbox.topic`.
- `messageKey` можно передавать `null` (в Kafka уйдет без ключа).

Паблишер `OutboxPublisher` вызывать вручную не нужно — он запускается по расписанию, если `outbox.publisher.enabled=true` и в контексте есть `KafkaTemplate`.

## Corner-cases и поведение

- **Kafka недоступна/таймаут отправки**: сообщение остается `PENDING` и будет повторно обработано при следующем цикле. Ошибка логируется.
- **Транзакционность**: `enqueue()` должен вызываться внутри бизнес-транзакции, иначе outbox-запись не будет атомарной с бизнес-изменениями.
- **Дубликаты**: возможны при повторной отправке после ошибок. Продюсер/консьюмер должны быть идемпотентны.
- **Мульти-инстанс**: используется `PESSIMISTIC_WRITE` без `SKIP LOCKED`. При нескольких инстансах возможны ожидания. Для горизонтального скейла можно заменить запрос на `FOR UPDATE SKIP LOCKED`.
- **Порядок сообщений**: выбираются по `createdAt`, но строгая глобальная упорядоченность при нескольких инстансах не гарантируется.
- **Статус SENT**: выставляется только после успешного `kafkaTemplate.send(...).get(timeout)`.

## Примечания по автоконфигурации

Автоконфигурация активируется автоматически через `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
Все бины создаются без необходимости `@ComponentScan` в приложении-потребителе.
