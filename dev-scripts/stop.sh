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
        echo -e "${YELLOW}  $service_name는 실행 중이 아닙니다.${NC}"
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

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}모든 서비스 종료 완료!${NC}"
echo -e "${GREEN}========================================${NC}"

