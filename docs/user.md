# User API Spec

## Register User
- Endpoint: `POST /api/v1/users/register`
- Request Body:
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```
- Response Body (Success):
```json
{
  "status": 200,
  "message": "User registered successfully",
  "data": {
    "userId": "number",
    "username": "string",
    "email": "string",
    "token": "string",
    "expiredAt": "number"
  }
}
```
- Catatan: token hanya dikembalikan pada response API (stateless), tidak disimpan di database.
- Response Body (Error):
- Status Code: 400 Bad Request
```json
{
  "error": "User registration failed",
  "details": "string"
}
```

## Login User
- Endpoint: `POST /api/v1/users/login`
- Request Body:
```json
{
  "email": "string",
  "password": "string"
}
```

- Response Body (Success):
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "string",
    "userId": "number",
    "username": "string",
    "email": "string",
    "expiredAt": "number"
  }
}
```

- Response Body (Error):
- Status Code: 401 Unauthorized
```json
{
    "error": "Invalid email or password",
    "details": "string"
}
```

## Get User
- Endpoint: `GET /api/v1/users/current`
- Headers: Authorization: Bearer {token}
- Response Body (Success):
```json
{
  "status": 200,
  "message": "Get user successful",
  "data": {
    "userId": "number",
    "username": "string",
    "email": "string"
  }
}
```

- Response Body (Error):
- Status Code: 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "details": "string"
}
```

## Update User
- Endpoint: `PATCH /api/v1/users/current`
- Headers: Authorization: Bearer {token}
- Request Body:
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```
- Response Body (Success):
```json
{
  "status": 200,
  "message": "Update user successful",
  "data": {
    "userId": "number",
    "username": "string",
    "email": "string",
    "token": "string",
    "expiredAt": "number"
  }
}
```
- Response Body (Error):
- Status Code: 400 Bad Request (semua field kosong)
- Status Code: 401 Unauthorized
- Status Code: 409 Conflict (username/email sudah dipakai)
```json
{
  "error": "User update failed",
  "details": "string"
}
```

## Delete User
- Endpoint: `DELETE /api/v1/users/current`
- Headers: Authorization: Bearer {token}
- Response Body (Success):
```json
{
  "status": 200,
  "message": "Delete user successful",
  "data": {
    "message": "User deleted successfully"
  }
}
```
- Response Body (Error):
- Status Code: 401 Unauthorized
- Status Code: 404 Not Found (user tidak ditemukan)
```json
{
  "error": "Unauthorized",
  "details": "string"
}
``` 
