Для работы потребуется docker-контейнер с PostgreSQL
(docker run --name postgres -e=POSTGRES_USER=user -e=POSTGRES_PASSWORD=userpassword -e=POSTGRES_DB=counter_db -p 5432:5432 -d postgres:latest)

1) Запустить PostgreSQL в docker
2) Запустить CounterServerApplication
3) Запустить приложение

Так как функция добавления новых счётчиков ещё не реализована, чтобы протестировать работу можно добавить их через SQL запросы в psql
