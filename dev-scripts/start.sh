#!/bin/bash

# PayStream MSA 서비스 일괄 실행 스크립트
# 사용법: ./dev-scripts/start.sh

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

# PID 디렉토리 생성
mkdir -p "$PID_DIR"

# 서비스 목록 (실행 순서 중요)
SERVICES=(
    "eureka-server:8761"
    "api-gateway:8000"
    "inventory-service:8081"
    "notification-service:8082"
    "order-service:8083"
    "payment-service:8084"
    "user-service:8085"
)

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}PayStream MSA 서비스 시작${NC}"
echo -e "${GREEN}========================================${NC}"

# 프로젝트 루트로 이동
cd "$PROJECT_ROOT"

# Gradle Wrapper 실행 권한 확인
if [ ! -x "./gradlew" ]; then
    chmod +x ./gradlew
fi

# 포트 사용 중인지 확인하는 함수 (LISTENING 상태만 확인)
check_port() {
    local port=$1
    
    # Linux/Mac: lsof 사용 (LISTEN 상태만)
    if command -v lsof > /dev/null 2>&1; then
        if lsof -ti:$port -sTCP:LISTEN > /dev/null 2>&1; then
            return 0
        fi
    fi
    
    # Linux: ss 사용 (LISTEN 상태만)
    if command -v ss > /dev/null 2>&1; then
        if ss -lnt 2>/dev/null | grep -q ":$port "; then
            return 0
        fi
    fi
    
    # Windows: netstat 사용 (LISTENING 상태만)
    if command -v netstat > /dev/null 2>&1; then
        # Windows 형식: netstat -ano | findstr LISTENING | findstr :포트번호
        if netstat -ano 2>/dev/null | grep "LISTENING" | grep -q ":$port "; then
            return 0
        fi
    fi
    
    # Linux: netstat 사용 (LISTEN 상태만)
    if command -v netstat > /dev/null 2>&1; then
        if netstat -tuln 2>/dev/null | grep "LISTEN" | grep -q ":$port "; then
            return 0
        fi
    fi
    
    return 1
}

# 서비스 실행 함수
start_service() {
    local service_name=$1
    local port=$2
    local pid_file="$PID_DIR/${service_name}.pid"
    
    # 이미 실행 중인지 확인
    if [ -f "$pid_file" ]; then
        local old_pid=$(cat "$pid_file")
        if ps -p "$old_pid" > /dev/null 2>&1 || kill -0 "$old_pid" 2>/dev/null; then
            echo -e "${YELLOW}⚠️  $service_name (포트: $port)는 이미 실행 중입니다. (PID: $old_pid)${NC}"
            return
        fi
    fi
    
    # 포트 사용 중인지 확인
    if check_port "$port"; then
        echo -e "${YELLOW}⚠️  포트 $port가 이미 사용 중입니다. $service_name을 건너뜁니다.${NC}"
        return
    fi
    
    echo -e "${GREEN}❤️ $service_name 시작 중... (포트: $port)${NC}"
    
    # 서비스 실행 (백그라운드)
    nohup ./gradlew :services:${service_name}:bootRun > "$PID_DIR/${service_name}.log" 2>&1 &
    local pid=$!
    
    # PID 저장
    echo $pid > "$pid_file"
    
    echo -e "${GREEN}💙 $service_name 시작됨 (PID: $pid)${NC}"
    
    # Eureka Server는 다른 서비스들이 등록할 수 있도록 대기
    if [ "$service_name" = "eureka-server" ]; then
        echo -e "${YELLOW}⏳ Eureka Server가 준비될 때까지 대기 중...${NC}"
        sleep 10
    else
        sleep 3
    fi
}

# 각 서비스 실행
for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port <<< "$service_info"
    start_service "$service_name" "$port"
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}모든 서비스 시작 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "서비스 상태 확인:"
echo -e "  - Eureka Dashboard: ${GREEN}http://localhost:8761${NC}"
echo -e "  - API Gateway: ${GREEN}http://localhost:8000${NC}"
echo ""
echo -e "로그 확인: ${YELLOW}dev-scripts/pids/*.log${NC}"
echo -e "서비스 종료: ${YELLOW}./dev-scripts/stop.sh${NC}"

