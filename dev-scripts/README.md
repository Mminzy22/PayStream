# PayStream 개발 스크립트

MSA 서비스를 한 번에 실행하고 종료하는 스크립트입니다.

## 사용법

### 모든 서비스 시작

```bash
./dev-scripts/start.sh
```

### 모든 서비스 종료

```bash
./dev-scripts/stop.sh
```

## 서비스 목록

스크립트는 다음 순서로 서비스를 실행합니다:

1. **Eureka Server** (포트: 8761)
2. **API Gateway** (포트: 8000)
3. **Inventory Service** (포트: 8081)
4. **Notification Service** (포트: 8082)
5. **Order Service** (포트: 8083)
6. **Payment Service** (포트: 8084)
7. **User Service** (포트: 8085)

## 주요 기능

- **자동 순서 관리**: Eureka Server를 먼저 실행하고, 다른 서비스들이 등록할 수 있도록 대기합니다.
- **중복 실행 방지**: 이미 실행 중인 서비스는 건너뜁니다.
- **포트 충돌 확인**: 포트가 이미 사용 중인 경우 해당 서비스를 건너뜁니다.
- **로그 관리**: 각 서비스의 로그는 `dev-scripts/pids/*.log`에 저장됩니다.
- **PID 관리**: 각 서비스의 PID는 `dev-scripts/pids/*.pid`에 저장되어 종료 시 사용됩니다.

## 접속 정보

서비스 시작 후 다음 주소로 접속할 수 있습니다:

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8000

## 로그 확인

각 서비스의 로그는 다음 위치에서 확인할 수 있습니다:

```
dev-scripts/pids/
├── eureka-server.log
├── api-gateway.log
├── inventory-service.log
├── notification-service.log
├── order-service.log
├── payment-service.log
└── user-service.log
```

## 문제 해결

### 포트가 이미 사용 중인 경우

스크립트는 자동으로 포트 충돌을 감지하고 해당 서비스를 건너뜁니다. 수동으로 프로세스를 종료하려면:

```bash
# Linux/Mac
lsof -ti:포트번호 | xargs kill -9

# Windows (Git Bash)
netstat -ano | findstr :포트번호
taskkill /PID <PID번호> /F
```

### 서비스가 정상적으로 종료되지 않는 경우

`stop.sh` 스크립트는 강제 종료도 시도하지만, 그래도 종료되지 않으면 수동으로 종료하세요.

