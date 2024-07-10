### pgSqlBlocks

pgSqlBlocks - это standalone приложение, написанное на языке программирования Java, которое позволяет легко ориентироваться среди процессов и получать информацию о блокировках и ожидающих запросов в СУБД PostgreSQL. Отображается информация о состоянии подключения к БД, а также информация о процессах в БД.

Требуется Java JRE версии 1.8 и выше для вашей платформы.

### Сборка, запуск тестов, запуск приложения

##### Для сборки без запуска тестов 
Выполните команду с использованием флага -DskipTests, к примеру: ```mvn package -P Linux-64 -DskipTests```

##### Для запуска тестов требуется:
1. Наличие [docker](https://docs.docker.com/engine/install/).
2. Выполните команду: ```mvn test```.

##### Запуск приложения

Запуск jar-файла через консоль командой ```java -jar pgSqlBlocks-1.3.6-Linux-64.jar```

* Для пользователей MacOS необходим дополнительный параметр ```-XstartOnFirstThread```.

* Для пользователей Gtk3, если возникают сложности с отображаемыми всплывающими сообщениями, рекомендуется запускать приложение с ключом *SWT_GTK3=0*.

### Запросы

Для получения всех процессов сервера, включая или исключая idle(бездействующие), используется [скрипт](src/main/resources/query_with_idle.sql) или [скрипт](src/main/resources/query.sql) соответственно.

Для версии PostgreSQL 10 и выше, для получения всех процессов сервера, включая или исключая idle(бездействующие), используется [скрипт](src/main/resources/query_with_idle_10.sql) или [скрипт](src/main/resources/query_10.sql) соответственно.

Уничтожается процесс командой: _select pg_terminate_backend(?);_

Послать сигнал для отмены процесса: _select pg_cancel_backend(?);_

### UI
Все, что связано с UI необходимо писать в пакете ru.taximaxim.pgsqlblocks.ui

### Homepage

https://pgcodekeeper.org/pgsqlblocks.html

### License

This application is licensed under the Apache License, Version 2.0. See [LICENCE](LICENSE) for details.
