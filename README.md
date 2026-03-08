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
TOKEN='<paste token here>'
```bash

TOKEN='eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsImlhdCI6MTc3Mjk1MzIwNywiZXhwIjoxNzcyOTU2ODA3LCJ1aWQiOjF9.hXALFWfMv1u9Ti5Qft2X7OmIHlQ9TBrCJDoledr55EA'
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

- Data is stored in H2 at `./data/todo-db`.
- H2 console is enabled at `http://localhost:8080/h2-console`.
- The JWT secret in `application.yml` is for local scaffold use only and should be replaced before production use.
