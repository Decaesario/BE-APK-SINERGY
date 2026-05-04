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

- Catatan:
  - Login manual hanya bisa jika akun memiliki password.
  - Jika akun dibuat dari Google login dan password belum di-set, gunakan endpoint update user untuk set password terlebih dahulu.

## Login Google User
- Endpoint: `POST /api/v1/users/login/google`
- Request Body:
```json
{
  "idToken": "firebase_id_token"
}
```
- Alur backend:
  - Backend memverifikasi `idToken` dengan Firebase Admin SDK.
  - Backend mengambil email terverifikasi dari token (bukan dari request client).
  - Jika email sudah ada: gunakan akun existing, update `firebase_uid` jika masih kosong.
  - Jika email belum ada: buat user baru dengan `password = null`, `provider = google`.
- Konfigurasi Firebase Admin:
  - Set salah satu env berikut ke path file service account JSON:
    - `FIREBASE_SERVICE_ACCOUNT_PATH`
    - `GOOGLE_APPLICATION_CREDENTIALS`
- Response Body (Success):
```json
{
  "status": 200,
  "message": "Google login successful",
  "data": {
    "token": "string",
    "userId": "number",
    "email": "string",
    "expiredAt": "number"
  }
}
```

- Response Body (Error):
- Status Code: 401 Unauthorized (token invalid / email token tidak valid)
- Status Code: 500 Internal Server Error (Firebase Admin belum dikonfigurasi)
```json
{
  "error": "Unauthorized",
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
- Endpoint: `PATCH /api/v1/update/users/current` atau `PUT /api/v1/update/users/current`
- Headers: Authorization: Bearer {token}
- Request Body:
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

## Auth Linking Notes
- `email` adalah unique identifier utama untuk semua metode login.
- Satu email hanya merepresentasikan satu akun (`users.email` unik).
- `firebase_uid` digunakan untuk mengikat akun Google ke user yang sama.
- `provider` menyimpan sumber login: `manual`, `google`, atau `both`.
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
