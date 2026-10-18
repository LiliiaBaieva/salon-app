# Салон «Орхідея»: облік записів клієнтів

Навчальний проєкт (Комплексний тренінг, варіант 28 «Перукарня»). Java 17, Spring Boot 3, Maven, H2 (файл), HTML/CSS/JS, Swagger, JUnit 5 + REST Assured.

## Запуск

```
mvn spring-boot:run
```

- Сайт: http://localhost:8080/
- Запис онлайн: http://localhost:8080/booking.html
- Адміністратор: http://localhost:8080/admin.html (логін `admin`, пароль `admin123`)
- Swagger UI: http://localhost:8080/swagger-ui.html (для методів `/api/admin/**` натиснути Authorize і ввести логін та пароль)

База створюється автоматично у файлі `data/salon.mv.db`, початкові чотири послуги додаються при першому запуску.

## Підключення DBeaver до бази

Поки застосунок запущений: драйвер H2, режим Server (AUTO_SERVER), JDBC URL
`jdbc:h2:file:<повний шлях до папки проєкту>/data/salon;AUTO_SERVER=TRUE`, користувач `sa`, пароль порожній.

## Тести

```
mvn test
```

Модульні тести: `SlotCalculatorTest`, `DiscountCalculatorTest`. API-тести (REST Assured): `BookingApiTest`. Для тестів використовується окрема база в пам'яті.

## Основні правила

- робочий день 09:00-20:00, початок запису кожні 15 хвилин, візит має закінчуватися не пізніше 20:00;
- записи на одну дату не перетинаються;
- клієнт визначається за телефоном `+380XXXXXXXXX`;
- знижка 3% з п'ятого запису клієнта, остаточна ціна зберігається в записі;
- послугу, на яку є записи, видалити не можна.

## CI

`.github/workflows/ci.yml` запускає `mvn test` при кожному push і pull request.
