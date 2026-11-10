# 🎮 Guess Game — Trò Chơi Đoán Số

Dự án game đoán số full-stack với Spring Boot (backend) + Vue.js (frontend), sử dụng Docker để chạy toàn bộ hệ thống chỉ với 1 lệnh.

---

## 📐 Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────┐
│                  Docker Network              │
│                                             │
│  ┌──────────┐     ┌──────────────────────┐  │
│  │  Nginx   │────▶│  Spring Boot Backend │  │
│  │  :80     │     │  :8080               │  │
│  │  Vue SPA │     └──────┬───────────────┘  │
│  └──────────┘            │                  │
│       ▲             ┌────┴────┐  ┌────────┐ │
│       │             │  MySQL  │  │ Redis  │ │
│  localhost:5173     │  :3306  │  │  :6379 │ │
│                     └─────────┘  └────────┘ │
└─────────────────────────────────────────────┘
```

| Service | Công nghệ | Port |
|---------|-----------|------|
| Frontend | Vue 3 + Nginx | `5173` |
| Backend | Spring Boot 3 + Java 17 | `8080` |
| Database | MySQL 8.0 | `3306` |
| Cache / Leaderboard | Redis 7 | `6379` |

---

## ✅ Yêu Cầu Cài Đặt

Chỉ cần cài **Docker Desktop** — không cần Java, Maven, Node.js trên máy.

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows / macOS / Linux)

Kiểm tra Docker đã cài:
```bash
docker --version
docker-compose --version
```

---

## 🚀 Hướng Dẫn Chạy Bằng Docker (Khuyến Nghị)

### Bước 1: Clone repository

```bash
git clone <URL_REPOSITORY>
cd inmobi
```

### Bước 2: Build và chạy toàn bộ hệ thống

```bash
docker-compose up --build -d
```

> Lần đầu build mất khoảng **3–5 phút** (tải dependencies Maven + npm).

### Bước 3: Truy cập ứng dụng

| Trang | URL |
|-------|-----|
| 🎮 Game (Frontend) | http://localhost:5173 |
| ⚙️ API Backend | http://localhost:8080/api |

### Bước 4: Dừng ứng dụng

```bash
# Dừng tạm (giữ lại container)
docker-compose stop

# Dừng và xóa container
docker-compose down

# Dừng + xóa cả dữ liệu MySQL (reset hoàn toàn)
docker-compose down -v
```

---

## 🔄 Khi Thay Đổi Code

Sau khi sửa code, rebuild lại image:

```bash
docker-compose down
docker-compose up --build -d
```

---

## 📋 Xem Log

```bash
# Log backend Spring Boot
docker logs game-backend

# Log frontend Nginx
docker logs game-frontend

# Log MySQL
docker logs game-mysql

# Log Redis
docker logs game-redis

# Theo dõi log realtime
docker logs -f game-backend
```

---

## 🛠️ Chạy Thủ Công (Không Dùng Docker)

<details>
<summary>Mở rộng nếu muốn chạy thủ công</summary>

### Yêu cầu
- Java 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8.0 (tạo database `game_doan_so`)
- Redis 7+

### Chạy Backend

```bash
cd guess-game

# Sửa application.yml trỏ đúng MySQL và Redis local
# spring.datasource.url: jdbc:mysql://localhost:3306/game_doan_so
# spring.data.redis.host: localhost

mvn clean package -DskipTests
java -jar target/*.jar
```

Backend chạy tại: http://localhost:8080

### Chạy Frontend

```bash
cd guess-game-ui
npm install
npm run dev
```

Frontend chạy tại: http://localhost:5173

</details>

---

## 🎲 Cách Chơi

1. **Đăng ký** tài khoản mới tại `/register`
2. **Đăng nhập** tại `/login`
3. Mỗi tài khoản nhận **5 lượt chơi miễn phí**
4. Tại trang Game: nhập số từ **1 đến 5** và nhấn **ĐOÁN NGAY** (hoặc Enter)
5. Nếu đoán trúng → +1 điểm, cập nhật Bảng Xếp Hạng
6. Hết lượt → Mua thêm **5 lượt / 50.000 VNĐ** qua VNPAY

> **Tỷ lệ thắng: 5%** — game có cơ chế house edge.

---

## 🔐 Bảo Mật

- Mật khẩu băm bằng **BCrypt**
- Xác thực bằng **JWT** (Access Token 15 phút + Refresh Token 7 ngày qua HttpOnly Cookie)
- Chống Race Condition bằng **Optimistic Locking** (`@Version`)
- Thanh toán ký bằng **HMAC-SHA512** (chuẩn VNPAY)

---

## 📁 Cấu Trúc Thư Mục

```
inmobi/
├── guess-game/              # Backend Spring Boot
│   ├── src/
│   │   └── main/java/com/inmobi/guess_game/
│   │       ├── controller/  # REST API endpoints
│   │       ├── service/     # Business logic
│   │       ├── security/    # JWT Filter + Security Config
│   │       ├── entity/      # JPA Entity
│   │       ├── dto/         # Request/Response objects
│   │       └── repository/  # JPA Repository
│   ├── Dockerfile
│   └── pom.xml
├── guess-game-ui/           # Frontend Vue 3
│   ├── src/
│   │   ├── views/           # GameView, LoginView, RegisterView...
│   │   ├── utils/api.js     # Axios instance + JWT interceptor
│   │   └── router/          # Vue Router
│   ├── nginx.conf           # Nginx proxy config
│   └── Dockerfile
├── docker-compose.yml       # Orchestration toàn bộ hệ thống
└── README.md
```

---

## 🌐 API Endpoints

| Method | Endpoint | Auth | Mô tả |
|--------|----------|:----:|-------|
| POST | `/api/auth/register` | ❌ | Đăng ký |
| POST | `/api/auth/login` | ❌ | Đăng nhập |
| POST | `/api/auth/refresh` | 🍪 | Làm mới token |
| POST | `/api/game/guess` | ✅ | Đoán số |
| GET | `/api/me` | ✅ | Thông tin cá nhân |
| GET | `/api/leaderboard` | ✅ | Top 10 bảng xếp hạng |
| POST | `/api/payment/buy-turns` | ✅ | Tạo link VNPAY |
| GET | `/api/payment/vnpay-ipn` | ❌ | Webhook VNPAY |
