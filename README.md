### pgSqlBlocks

Приложение, которое позволяет легко ориентироваться среди процессов и получать информацию о блокировках и ожидающих запросов. 
Отображается информация о состоянии подключения к БД, а также информация о процессах в БД.

### Installing

1. Установите Java JRE версии 1.8 и выше для вашей платформы. Скачать можно по [ссылке](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
2. Скачайте последнюю версию pgSqlBlocks для вашей платформы по [ссылке](http://pgcodekeeper.ru/pgsqlblocks.html).
3. Запустите pgSqlBlocks двойным кликом мыши по скачанному jar-файлу либо через консоль командой _java -jar pgSqlBlocks-1.3.6-Linux-64.jar_

### Структура пакетов

_ru.taximaxim.pgsqlblocks_: В данном пакете находятся все классы, реализующие бизнес логику проекта.

_ru.taximaxim.pgsqlblocks.ui_: Тут лежит все, что связано с UI.

### Бизнес логика

Есть 3 основных класса:

* `DbcData.java`
В данном классе хранится вся необходимая для соединения с БД информация + его статус (private DbcStatus status).
Каждому статусу необходимо указывать соответсвующую картинку (public String getImageAddr()). Картинки хранятся в src/main/resources/images.
Эти данные сохраняются в формате xml. Вся работа с xml реализована в DbcDataList.java.

* `Process.java`
Данный класс хранит информацию о процессах DB. Для вывода информации в UI-дерево используется метод String[] toTree().
Данные реализованы в виде дерева. У процесса могут быть потомки(блокируемые данным процессом процессы) и родитель(блокирующий процесс).

* `Provider.java`
В нем реализуется связь, отправка запросов и получение данных с БД. Все новые запросы необходимо оборачивать в Runnable и выполнять их в отдельном потоке, во избежании блокировки/подтормаживания ui-потока.
Для уведомления ui-потока о том, что запрос отработал, необходимо в его конце вызвать метод display.asyncExec(Runnable runnable);

### Запросы

Для получения всех процессов сервера используется [скрипт](src/main/resources/query.sql)

Уничтожить процесс: select pg_terminate_backend(?);

Послать сигнал для отмены процесса: select pg_cancel_backend(?);

### UI
Все, что связано с UI необходимо писать в пакете ru.taximaxim.pgsqlblocks.ui
Основной UI приложения написан в классе MainForm.java.

### Homepage

http://pgcodekeeper.ru/pgsqlblocks.html

###License

This application is licensed under the Apache License, Version 2.0. See LICENCE for details.