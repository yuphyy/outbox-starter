# Outbox Starter

Минимальный starter для outbox transaction. Бизнес-код пишет событие в таблицу `outbox_message` внутри своей транзакции. Фоновый паблишер отправляет его в Kafka и переводит статус в `SENT` после успешной отправки.

## Как подключить

Добавьте зависимость на этот модуль.

## Конфигурация (в приложении-потребителе)

В стартере нет собственного `application.yaml`. Конфигурация задается в приложении, которое его подключает.

### База данных (PostgreSQL)

```yaml
outbox:
  datasource:
    url: jdbc:postgresql://localhost:5432/outbox
    username: outbox
    password: outbox

spring:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```

Outbox использует собственный `outbox.datasource.*`. Можно дублировать настройки вашего основного `spring.datasource.*` — это явно показывает, куда пишет outbox.
Если `outbox.datasource.url` не задан, outbox будет использовать основной datasource приложения.

### Kafka (outbox, plaintext, локально)

```yaml
outbox:
  kafka:
    enabled: true
    bootstrap-servers: localhost:9092
    security-protocol: PLAINTEXT
```

Outbox использует собственный набор настроек `outbox.kafka.*` и не конфликтует с `spring.kafka.*` вашего сервиса.
Если `outbox.kafka.enabled=false`, Kafka-часть полностью выключена и остальные поля можно не заполнять.

### Kafka (outbox, SSL с keystore/truststore)

```yaml
outbox:
  kafka:
    enabled: true
    bootstrap-servers: kafka.prod:9093
    security-protocol: SSL
    ssl:
      truststore-location: /etc/ssl/kafka.truststore.jks
      truststore-password: changeit
      truststore-type: JKS
      keystore-location: /etc/ssl/kafka.keystore.p12
      keystore-password: changeit
      keystore-type: PKCS12
      key-password: changeit
```

### Outbox-настройки

```yaml
outbox:
  routes:
    ORDER_CREATED:
      recipients:
        billing: outbox.billing
        shipping: outbox.shipping
  publisher:
    enabled: true
    batch-size: 100
    poll-interval-ms: 1000
    send-timeout-ms: 5000
```

Если маршрут для пары (`messageType`, `recipient`) не найден, `enqueue` выбросит `IllegalArgumentException`.

## Как пользоваться

Инжектируйте `OutboxClient` и вызывайте `enqueue(messageType, recipient, messageKey, payload)`:

```java
outboxClient.enqueue("ORDER_CREATED", "billing", "order-123", "{\"event\":\"ORDER_CREATED\"}");
```

## Структура пакетов

- `api`: публичный контракт (`OutboxClient`).
- `service`: запись в outbox (`OutboxService`).
- `model`: сущности и статус (`OutboxMessage`, `OutboxMessageStatus`).
- `repository`: JPA-репозиторий.
- `publisher`: выборка и отправка (`OutboxBatchReader`, `OutboxSender`, `OutboxPublisher`).
- `config`: свойства (`OutboxProperties`, `OutboxKafkaProperties`, `OutboxDataSourceProperties`).
- `autoconfigure`: автоконфигурация Spring Boot.

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
- `message_type` (varchar(255)): тип сообщения.
- `recipient` (varchar(255)): целевой получатель/сервис.
- `message_key` (varchar(255), nullable): ключ сообщения в Kafka.
- `payload` (text): полезная нагрузка.
- `status` (varchar(32)): состояние отправки (`PENDING` или `SENT`).
- `created_at` (timestamp with time zone): время создания записи.
- `sent_at` (timestamp with time zone, nullable): время успешной отправки.
- `version` (bigint): оптимистическая версия для JPA.

Индекс:

- `idx_outbox_message_status_created` по (`status`, `created_at`) ускоряет выборку ожидающих сообщений.

## Методы для использования

Минимальный контракт — один метод:

- `OutboxClient.enqueue(messageType, recipient, messageKey, payload)`

Поведение:

- Сохраняет запись со статусом `PENDING` в текущей транзакции.
- `topic` выбирается по `outbox.routes` по паре (`messageType`, `recipient`).
- `messageKey` можно передавать `null` (в Kafka уйдет без ключа).
- `messageType` и `recipient` обязательны.

Паблишер `OutboxPublisher` вызывать вручную не нужно — он запускается по расписанию, если `outbox.publisher.enabled=true`, `outbox.kafka.enabled=true` и настроен `outbox.kafka.bootstrap-servers`.

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
