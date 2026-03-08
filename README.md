# Todo API Baseline

Runnable Spring Boot 3 / Java 17 scaffold for a multi-user todo API with JWT auth, per-user todo ownership, H2 persistence, validation, and baseline integration tests.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

## Test

```bash
mvn test
```

## API summary

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/todos`
- `GET /api/todos`
- `GET /api/todos/{id}`
- `PUT /api/todos/{id}`
- `DELETE /api/todos/{id}`

Authenticated requests must include `Authorization: Bearer <token>`.

## Example curl flow

Register a user:

```bash
curl -s http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"password123"}'
```

Login:

```bash
curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"password123"}'
```

Save the returned token:

```bash
TOKEN='<paste token here>'
```

Create a todo:

```bash
curl -s http://localhost:8080/api/todos \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"2 liters","completed":false,"dueDate":"2026-03-10"}'
```

List current user's todos:

```bash
curl -s http://localhost:8080/api/todos \
  -H "Authorization: Bearer $TOKEN"
```

Get one todo:

```bash
curl -s http://localhost:8080/api/todos/1 \
  -H "Authorization: Bearer $TOKEN"
```

Update a todo:

```bash
curl -s -X PUT http://localhost:8080/api/todos/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"Skimmed","completed":true,"dueDate":"2026-03-11"}'
```

Delete a todo:

```bash
curl -i -X DELETE http://localhost:8080/api/todos/1 \
  -H "Authorization: Bearer $TOKEN"
```

## Notes

- Data is stored in H2 at `./data/todo-db`.
- H2 console is enabled at `http://localhost:8080/h2-console`.
- The JWT secret in `application.yml` is for local scaffold use only and should be replaced before production use.
