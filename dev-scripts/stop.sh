#!/bin/bash

# PayStream MSA 서비스 일괄 종료 스크립트
# 사용법: ./dev-scripts/stop.sh

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 프로젝트 루트 디렉토리
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_DIR="$PROJECT_ROOT/dev-scripts/pids"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}PayStream MSA 서비스 종료${NC}"
echo -e "${YELLOW}========================================${NC}"

# 서비스 목록 (종료 순서 - 역순)
SERVICES=(
    "user-service:8085"
    "payment-service:8084"
    "order-service:8083"
    "notification-service:8082"
    "inventory-service:8081"
    "api-gateway:8000"
    "eureka-server:8761"
)

# Redis 설정 (기본값)
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_CONTAINER_NAME="paystream-redis"
KAFKA_PORT=${KAFKA_PORT:-29092}

# 서비스 종료 함수
stop_service() {
    local service_name=$1
    local port=$2
    local pid_file="$PID_DIR/${service_name}.pid"
    
    # PID 파일로 종료 시도
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}🛑 $service_name 종료 중... (PID: $pid)${NC}"
            kill "$pid" 2>/dev/null || true
            sleep 2
            
            # 강제 종료가 필요한 경우
            if ps -p "$pid" > /dev/null 2>&1; then
                echo -e "${RED}⚠️  강제 종료 중...${NC}"
                kill -9 "$pid" 2>/dev/null || true
            fi
            
            rm -f "$pid_file"
            echo -e "${GREEN}❤️ $service_name 종료됨${NC}"
            return
        else
            rm -f "$pid_file"
        fi
    fi
    
    # 포트로 프로세스 찾아서 종료
    local pids=""
    
    # Linux/Mac: lsof 사용
    if command -v lsof > /dev/null 2>&1; then
        pids=$(lsof -ti:$port 2>/dev/null || true)
    fi
    
    # Windows: netstat 사용
    if [ -z "$pids" ] && command -v netstat > /dev/null 2>&1; then
        # Windows 형식: netstat -ano | findstr :포트번호
        pids=$(netstat -ano 2>/dev/null | grep ":$port " | awk '{print $NF}' | sort -u || true)
    fi
    
    # Linux: ss 사용
    if [ -z "$pids" ] && command -v ss > /dev/null 2>&1; then
        # grep -oP는 일부 시스템에서 작동하지 않을 수 있으므로 sed 사용
        pids=$(ss -lntp 2>/dev/null | grep ":$port " | sed -n 's/.*pid=\([0-9]*\).*/\1/p' | sort -u || true)
    fi
    
    if [ -n "$pids" ]; then
        echo -e "${YELLOW}🛑 $service_name 종료 중... (포트: $port)${NC}"
        for pid in $pids; do
            kill "$pid" 2>/dev/null || true
            sleep 1
            if ps -p "$pid" > /dev/null 2>&1; then
                kill -9 "$pid" 2>/dev/null || true
            fi
        done
        echo -e "${GREEN}💚 $service_name 종료됨${NC}"
    else
        echo -e "${YELLOW}🛑 $service_name (포트: $port)는 실행 중이 아닙니다.${NC}"
    fi
}

# 각 서비스 종료
for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port <<< "$service_info"
    stop_service "$service_name" "$port"
done

# 남은 Java 프로세스 확인 (Gradle bootRun)
echo -e "${YELLOW}남은 Java 프로세스 확인 중...${NC}"
java_pids=$(ps aux 2>/dev/null | grep "[g]radle.*bootRun" | awk '{print $2}' || true)
if [ -n "$java_pids" ]; then
    for pid in $java_pids; do
        echo -e "${YELLOW}🛑 남은 Gradle 프로세스 종료 중... (PID: $pid)${NC}"
        kill "$pid" 2>/dev/null || true
        sleep 1
        if ps -p "$pid" > /dev/null 2>&1; then
            kill -9 "$pid" 2>/dev/null || true
        fi
    done
fi

# Docker가 실행 중인지 확인하는 함수
is_docker_running() {
    if ! command -v docker > /dev/null 2>&1; then
        return 1
    fi
    # Docker daemon이 실행 중인지 확인 (오류 메시지 숨김)
    docker info > /dev/null 2>&1
    return $?
}

# Docker가 실행 중인지 확인하는 함수
is_docker_running() {
    if ! command -v docker > /dev/null 2>&1; then
        return 1
    fi
    # Docker daemon이 실행 중인지 확인 (오류 메시지 숨김)
    docker info > /dev/null 2>&1
    return $?
}

# Redis 종료 함수
stop_redis() {
    echo -e "${YELLOW}🛑 Redis 종료 중...${NC}"
    
    # Docker가 실행 중인 경우에만 Docker 관련 명령 실행
    if is_docker_running; then
        # Docker Compose로 실행 중인지 확인 및 종료
        if [ -f "$PROJECT_ROOT/docker-compose.yml" ]; then
            if command -v docker-compose > /dev/null 2>&1; then
                if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${REDIS_CONTAINER_NAME}$"; then
                    echo -e "${YELLOW}🐳 Redis Docker Compose 종료 중...${NC}"
                    cd "$PROJECT_ROOT"
                    docker-compose stop redis > /dev/null 2>&1 || true
                    echo -e "${GREEN}💚 Redis Docker Compose 종료됨${NC}"
                    return
                fi
            elif docker compose version > /dev/null 2>&1; then
                # Docker Compose V2
                if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${REDIS_CONTAINER_NAME}$"; then
                    echo -e "${YELLOW}🐳 Redis Docker Compose V2 종료 중...${NC}"
                    cd "$PROJECT_ROOT"
                    docker compose stop redis > /dev/null 2>&1 || true
                    echo -e "${GREEN}💚 Redis Docker Compose 종료됨${NC}"
                    return
                fi
            fi
        fi
        
        # Docker 컨테이너로 실행 중인지 확인
        if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${REDIS_CONTAINER_NAME}$"; then
            echo -e "${YELLOW}🐳 Redis Docker 컨테이너 종료 중...${NC}"
            docker stop "$REDIS_CONTAINER_NAME" > /dev/null 2>&1 || true
            echo -e "${GREEN}💚 Redis Docker 컨테이너 종료됨${NC}"
            return
        fi
    fi
    
    # 로컬 redis-server 프로세스 확인
    local redis_pids=""
    if command -v lsof > /dev/null 2>&1; then
        redis_pids=$(lsof -ti:$REDIS_PORT 2>/dev/null || true)
    elif command -v ss > /dev/null 2>&1; then
        redis_pids=$(ss -lntp 2>/dev/null | grep ":$REDIS_PORT " | sed -n 's/.*pid=\([0-9]*\).*/\1/p' | sort -u || true)
    fi
    
    if [ -n "$redis_pids" ]; then
        for pid in $redis_pids; do
            # redis-server 프로세스인지 확인
            if ps -p "$pid" > /dev/null 2>&1; then
                local process_name=$(ps -p "$pid" -o comm= 2>/dev/null || echo "")
                if echo "$process_name" | grep -q "redis"; then
                    echo -e "${YELLOW}🖥️  로컬 Redis 서버 종료 중... (PID: $pid)${NC}"
                    kill "$pid" 2>/dev/null || true
                    sleep 1
                    if ps -p "$pid" > /dev/null 2>&1; then
                        kill -9 "$pid" 2>/dev/null || true
                    fi
                    echo -e "${GREEN}💚 로컬 Redis 서버 종료됨${NC}"
                    return
                fi
            fi
        done
    fi
    
    echo -e "${YELLOW}⚠️  Redis가 실행 중이 아닙니다.${NC}"
}

# Kafka 종료 함수
stop_kafka() {
    echo -e "${YELLOW}🛑 Kafka 종료 중...${NC}"

    if is_docker_running && [ -f "$PROJECT_ROOT/docker-compose.yml" ]; then
        cd "$PROJECT_ROOT"
        if command -v docker-compose > /dev/null 2>&1; then
            docker-compose stop kafka kafka-ui > /dev/null 2>&1 || true
        elif docker compose version > /dev/null 2>&1; then
            docker compose stop kafka kafka-ui > /dev/null 2>&1 || true
        fi
    fi

    if command -v lsof > /dev/null 2>&1; then
        kafka_pids=$(lsof -ti:$KAFKA_PORT 2>/dev/null || true)
        if [ -n "$kafka_pids" ]; then
            for pid in $kafka_pids; do
                kill "$pid" 2>/dev/null || true
                sleep 1
                if ps -p "$pid" > /dev/null 2>&1; then
                    kill -9 "$pid" 2>/dev/null || true
                fi
            done
        fi
    fi

    echo -e "${GREEN}💚 Kafka 종료 처리 완료${NC}"
}

# Redis 종료
stop_redis
# Kafka 종료
stop_kafka

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}모든 서비스 종료 완료!${NC}"
echo -e "${GREEN}========================================${NC}"

