# Учебное задание 1: Сервис API Gateway на Spring Boot (JWT)

### Цель 
Создать простой API Gateway, который:
- валидирует входящие данные.
- регистрирует пользователей (если их нет).
- генерирует JWT токен с ролью USER для всех запросов (кроме регистрации) и добавляет его в последующие заголовки (фильтр)

---

## 📌 **Функциональность**
- регистрация нового пользователя (хранение в памяти, можно использовать ConcurrentHashMap или Map)
- логин пользователя и Генерация JWT-токена с ролью `USER` (время жизни токена 30 минут)
- автоматическое добавление JWT в заголовки запросов (кроме `/register`, `/login`)
---

## 🛠 **Технологии**
- Java 17
- Spring Boot 3.x
- Spring Security
- JWT (библиотека `jjwt` или её аналоги)
- Lombok (опционально)
---

## 📂 **Структура проекта**

src    
├── main  
│ ├── java  
│ │ └── ru.creditbank.apigateway  
│ │ ├── config # SecurityConfig, JwtConfig  
│ │ ├── core # UserModel  
│ │ ├── registration  
│ │ │ ├── rest/# AuthController, RegisterRequest   
│ │ │ ├── service # AuthService  
│ │ ├── jwt  
│ │ │ ├── filter # JwtGenerationFilter  
│ │ │ ├── service # JwtService  
│ │ └── ApiGatewayApplication.java  
│ └── resources  
│ └── application.yml  

---

## 🔐 **API Endpoints**
[open-api](gateway-open-api.yaml)
1. Регистрация пользователя
2. Вход пользователя
3. Любой другой тестовый последующий запрос
   * Автоматически добавляет в заголовки:
    ```http
    Authorization: Bearer <JWT_TOKEN>
    ```
---
## 🧪 Тестирование
1. Зарегистрируйте пользователя
```bash
curl -X POST http://localhost:8080/api/v1/auth/register
-H "Content-Type: application/json" \
-d '{"email": "test@ya.ru", "password":"test"}'
```
2. Войдите под пользователем
```bash
curl -X POST http://localhost:8080/api/v1/auth/login
-H "Content-Type: application/json" \
-d '{"email": "test@ya.ru", "password":"test"}'
```
3. Отправьте тестовый запрос — в ответе должен быть JWT в заголовках.
---
## 📌 Дополнительные задания
* Добавить роль ADMIN и разделение прав
* Реализовать refresh-токены
* Вместо хранения в памяти использовать базу данных postgresql, Spring Data Jpa