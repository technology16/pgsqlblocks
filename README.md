### pgSqlBlocks

pgSqlBlocks - это standalone приложение, написанное на языке программирования Java, 
которое позволяет легко ориентироваться среди процессов и получать информацию о блокировках и ожидающих запросов в СУБД PostgreSQL. 
Отображается информация о состоянии подключения к БД, а также информация о процессах в БД.

Требуется Java JRE версии 1.8 и выше для вашей платформы.

### Сборка, запуск тестов, запуск приложения

#### Добавление зависимостей

pgSqlBlocks умеет читать пароли из файла pgpass благодаря библиотеке [PgPass](https://github.com/technology16/pgpass). Перед сборкой приложения необходимо установить эту библиотеку в свой локальный мавен репозиторий (инструкция есть в репозитории PgPass). 

##### Для сборки без запуска тестов 
Выполните команду с использованием флага -DskipTests, к примеру: ```mvn package -P Linux-64 -DskipTests```

##### Для запуска тестов требуется:
1. Создать роль для тестового пользователя в БД:
```
CREATE ROLE pgsqlblocks_test LOGIN CREATEDB PASSWORD 'pgsqlblocks_test_user_password';
```

2. Создать файл application.conf в директории src/test/resources:
```
pgsqlblocks-test-configs {
  remote-host = "localhost" // test DB host 
  remote-db = "postgres" // test DB name
  remote-port = "5432"
  remote-version = "10.0" // 9.2,  9.3,  9.4,  9.5,  9.6 
  remote-username = "pgsqlblocks_test" // test user login
  remote-password = "pgsqlblocks_test_user_password" // test user password

  select-pg-backend-pid = "select pg_backend_pid();"
  pg-terminate-backend = "select pg_terminate_backend(?);"
  terminated-succesed = "pg_terminate_backend"
  pg-backend = "pg_backend_pid"
  testing-dump-sql = "testing_dump.sql"
  create-rule-sql = "create_rule.sql"
  drop-rule-sql = "drop_rule.sql"
  select-1000-sql = "select_1000.sql"
  select-sleep-sql = "select_sleep.sql"
  create-index-sql = "create_index.sql"

  delay-ms = 250.0 // waiting time between request's
}
```
3. Выполните команду указав профиль, к примеру: ```mvn package -P Linux-64```,
либо без указания профиля, если требуется собрать для всех платформ ```./package.sh```

* Перед каждой сборкой рекомендуется выполнить команду ```mvn clean```

##### Запуск приложения

Запуск jar-файла через консоль командой ```java -jar pgSqlBlocks-1.3.6-Linux-64.jar```

* Для пользователей Gtk3, если возникают сложности с отображаемыми всплывающими сообщениями, рекомендуется запускать приложение с ключом *SWT_GTK3=0*.

### Структура пакетов

_ru.taximaxim.pgsqlblocks_: В данном пакете находятся все классы, реализующие бизнес логику проекта.

_ru.taximaxim.pgsqlblocks.ui_: Тут лежит все, что связано с UI.

_ru.taximaxim.pgsqlblocks.utils_: Тут находятся утилиты для работы с фильтрами процессов, иконками, директориями, настройками, XML и чтения файла pgpass.

### Бизнес логика

Есть 3 основных класса:

* `DbcData.java`
В данном классе хранится вся необходимая для соединения с БД информация + его статус (private DbcStatus status).
Каждому статусу необходимо указывать соответсвующую иконку (public String getImageAddr()). Иконки хранятся в src/main/resources/images.
Эти данные сохраняются в формате xml. Вся работа с xml реализована в DbcDataListBuilder.java.

* `Process.java`
Данный класс хранит информацию о процессах DB.
Данные реализованы в виде дерева. У процесса могут быть потомки(блокируемые данным процессом процессы) и родитель(блокирующий процесс).

* `DbcDataRunner.java`
В нем реализуется связь, отправка запросов и получение данных с БД. Все новые запросы необходимо оборачивать в Runnable и выполнять в отдельном потоке с помощью ScheduledExecutorService, к примеру, во избежании блокировки/подтормаживания ui-потока.
В начале необходимо уведомить, что происходит обновление информации о процессах в подключенной БД: dbcData.setInUpdateState(false)
Для уведомления ui-потока о том, что запрос отработал, необходимо в его конце вызвать dbcData.setInUpdateState(false) и dbcData.notifyUpdated();;

### Запросы

Для получения всех процессов сервера используется [скрипт](src/main/resources/query.sql).

Для получения всех процессов сервера, включая idle(бездействующие), используется [скрипт](src/main/resources/query_with_idle.sql).

Уничтожается процесс командой: _select pg_terminate_backend(?);_

Послать сигнал для отмены процесса: _select pg_cancel_backend(?);_

### UI
Все, что связано с UI необходимо писать в пакете ru.taximaxim.pgsqlblocks.ui
Основной UI приложения написан в классе MainForm.java.

### Homepage

http://pgcodekeeper.ru/pgsqlblocks.html

### License

This application is licensed under the Apache License, Version 2.0. See LICENCE for details.