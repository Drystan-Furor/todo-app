# Todo API Baseline

Runnable Spring Boot 3 / Java 17 scaffold for a multi-user todo API with JWT auth, refresh-token rotation, per-user todo ownership, H2 persistence, optional PostgreSQL profile, and integration tests.

## Requirements

- Java 17+
- Maven 3.9+

## Run with local H2

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

Local defaults:

- Profile: default
- Database: file-backed H2 at `./data/todo-db`
- H2 console: `http://localhost:8080/h2-console`

## Run with Docker Postgres

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the app against Postgres:

```bash
SPRING_PROFILES_ACTIVE=postgres \
DB_URL=jdbc:postgresql://localhost:5432/todo_app \
DB_USERNAME=todo \
DB_PASSWORD=todo \
APP_JWT_SECRET=change-me-in-production-please-32bytes \
mvn spring-boot:run
```

You can override `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, and `POSTGRES_PORT` in `docker-compose.yml` through environment variables.

## Test

```bash
mvn test
```

## API summary

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/todos`
- `GET /api/todos`
- `GET /api/todos/{id}`
- `PUT /api/todos/{id}`
- `DELETE /api/todos/{id}`

Authenticated requests must include `Authorization: Bearer <access-token>`.

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
TOKEN='<paste accessToken here>'
REFRESH_TOKEN='<paste refreshToken here>'
```

Refresh the session:

```bash
curl -s http://localhost:8080/api/auth/refresh \
  -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
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

List with filters and pagination:

```bash
curl -s 'http://localhost:8080/api/todos?completed=false&q=milk&page=0&size=10' \
  -H "Authorization: Bearer $TOKEN"
```

`GET /api/todos` supports these optional query params:

- `completed`: boolean filter
- `q`: case-insensitive substring match against `title` or `description`
- `page`: zero-based page index, defaults to `0`
- `size`: page size, defaults to `20`, max `100`

Example list response:

```json
{
  "items": [
    {
      "id": 1,
      "title": "Buy milk",
      "description": "2 liters",
      "completed": false,
      "dueDate": "2026-03-10",
      "ownerId": 1,
      "createdAt": "2026-03-08T09:00:00Z",
      "updatedAt": "2026-03-08T09:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
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

- Access tokens are stateless JWTs; refresh tokens are opaque, persisted, and rotated on use.
- The default profile stores data in H2 at `./data/todo-db`.
- The `postgres` profile uses PostgreSQL settings from `application-postgres.yml`.
- The JWT secret in `application.yml` is for local scaffold use only and should be replaced before production use.
