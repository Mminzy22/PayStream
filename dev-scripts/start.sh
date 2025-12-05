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

# .env 파일 로드 함수
load_env_file() {
    local env_file="$PROJECT_ROOT/.env"
    
    if [ ! -f "$env_file" ]; then
        echo -e "${YELLOW}👿  .env 파일을 찾을 수 없습니다. 기본값을 사용합니다.${NC}"
        return
    fi
    
    echo -e "${GREEN}📄 .env 파일 로드 중...${NC}"
    
    # .env 파일을 읽어서 환경 변수로 export
    # 형식: KEY: "value" 또는 KEY: value
    while IFS= read -r line || [ -n "$line" ]; do
        # 앞뒤 공백 제거
        line=$(echo "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        
        # 빈 줄이나 주석 줄 건너뛰기
        if [[ -z "$line" || "$line" =~ ^# ]]; then
            continue
        fi
        
        # KEY: "value" 또는 KEY: value 형식 파싱
        if [[ "$line" =~ ^([^:]+):[[:space:]]*(.+)$ ]]; then
            local key=$(echo "${BASH_REMATCH[1]}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
            local value=$(echo "${BASH_REMATCH[2]}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
            
            # 따옴표 제거 (앞뒤 따옴표가 있는 경우)
            if [[ "$value" =~ ^\".*\"$ ]]; then
                value="${value#\"}"
                value="${value%\"}"
            fi
            
            # 환경 변수로 export
            export "$key=$value"
        fi
    done < "$env_file"
    
    echo -e "${GREEN}❤️ .env 파일 로드 완료${NC}"
}

# .env 파일 로드
load_env_file

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

# Redis 설정 (기본값)
REDIS_HOST=${REDIS_HOST:-localhost}
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_CONTAINER_NAME="paystream-redis"

# Docker가 실행 중인지 확인하는 함수
is_docker_running() {
    if ! command -v docker > /dev/null 2>&1; then
        return 1
    fi
    # Docker daemon이 실행 중인지 확인 (docker ps로 실제 연결 테스트)
    # 오류 메시지는 stderr로 리다이렉트하여 숨김
    docker ps > /dev/null 2>&1
    return $?
}

# Docker Desktop 시작 함수 (macOS & Windows)
start_docker_desktop() {
    # macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if [ -d "/Applications/Docker.app" ]; then
            echo -e "${GREEN}🐳 Docker Desktop 시작 중... (macOS)${NC}"
            open -a Docker > /dev/null 2>&1
            _wait_for_docker_daemon
            return $?
        else
            echo -e "${YELLOW}⚠️  Docker Desktop이 설치되어 있지 않습니다.${NC}"
            return 1
        fi
    # Windows (Git Bash, WSL, MSYS2 등)
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "win32" ]]; then
        # Windows에서 Docker Desktop 경로 확인
        local docker_paths=(
            "/c/Program Files/Docker/Docker/Docker Desktop.exe"
            "/c/Program Files (x86)/Docker/Docker/Docker Desktop.exe"
            "C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe"
            "C:\\Program Files (x86)\\Docker\\Docker\\Docker Desktop.exe"
        )
        
        local docker_found=""
        for path in "${docker_paths[@]}"; do
            if [ -f "$path" ] || [ -f "$(cygpath -u "$path" 2>/dev/null)" ]; then
                docker_found="$path"
                break
            fi
        done
        
        if [ -n "$docker_found" ]; then
            echo -e "${GREEN}🐳 Docker Desktop 시작 중... (Windows)${NC}"
            # Windows에서 Docker Desktop 시작
            if command -v cmd.exe > /dev/null 2>&1; then
                # Git Bash에서 Windows 명령 실행
                cmd.exe //c start "" "$docker_found" > /dev/null 2>&1
            else
                # 직접 실행 시도
                "$docker_found" > /dev/null 2>&1 &
            fi
            _wait_for_docker_daemon
            return $?
        else
            echo -e "${YELLOW}⚠️  Docker Desktop이 설치되어 있지 않습니다.${NC}"
            return 1
        fi
    # Linux
    else
        echo -e "${YELLOW}⚠️  Linux에서는 Docker Desktop 자동 시작을 지원하지 않습니다.${NC}"
        echo -e "${YELLOW}💡 Docker daemon을 수동으로 시작하세요: ${NC}sudo systemctl start docker"
        return 1
    fi
}

# Docker daemon이 준비될 때까지 대기하는 헬퍼 함수
_wait_for_docker_daemon() {
    local max_wait=60
    local waited=0
    
    while [ $waited -lt $max_wait ]; do
        if docker ps > /dev/null 2>&1; then
            echo -e "${GREEN}🐳 Docker Desktop 시작 완료${NC}"
            return 0
        fi
        sleep 2
        waited=$((waited + 2))
        # 커서를 같은 줄에 유지하면서 진행 상황 표시
        echo -ne "\r${YELLOW}⏳ Docker daemon 준비 중... (${waited}초/${max_wait}초)${NC}"
    done
    echo "" # 줄바꿈
    echo -e "${RED}👿 Docker Desktop 시작 타임아웃 (60초)${NC}"
    return 1
}

# Redis 시작 함수
start_redis() {
    echo -e "${GREEN}❤️‍🔥 Redis 시작 중...${NC}"
    
    # 포트가 이미 사용 중인지 확인
    if check_port "$REDIS_PORT"; then
        echo -e "${YELLOW}👿  Redis 포트 $REDIS_PORT가 이미 사용 중입니다.${NC}"
        
        # Docker Compose로 실행 중인지 확인
        if is_docker_running; then
            if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${REDIS_CONTAINER_NAME}$"; then
                echo -e "${GREEN}🐳 Redis Docker Compose 컨테이너가 이미 실행 중입니다.${NC}"
                return
            fi
        fi
        
        # Docker 컨테이너로 실행 중인지 확인
        if is_docker_running; then
            if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${REDIS_CONTAINER_NAME}$"; then
                echo -e "${GREEN}🐳 Redis Docker 컨테이너가 이미 실행 중입니다.${NC}"
                return
            fi
        fi
        
        echo -e "${YELLOW}👿  기존 Redis 인스턴스를 사용합니다.${NC}"
        return
    fi
    
    # Docker가 실행 중이 아니면 자동으로 시작 시도
    if ! is_docker_running; then
        echo -e "${YELLOW}🐳 Docker daemon이 실행 중이 아닙니다. Docker Desktop을 시작합니다...${NC}"
        if start_docker_desktop; then
            echo -e "${GREEN}🐳 Docker Desktop 시작 완료, Redis 시작을 계속합니다.${NC}"
        else
            echo -e "${RED}👿 Docker Desktop 시작 실패. Redis 없이 서비스를 계속 실행합니다.${NC}"
            echo -e "${YELLOW}💡 수동으로 Docker Desktop을 시작한 후 다시 실행하세요.${NC}"
            return
        fi
    fi
    
    # Docker가 실행 중인 경우에만 Docker 관련 명령 실행
    if is_docker_running; then
        # Docker Compose로 Redis 시작 시도 (우선)
        if [ -f "$PROJECT_ROOT/docker-compose.yml" ]; then
            if command -v docker-compose > /dev/null 2>&1; then
                echo -e "${GREEN}🐳 Docker Compose로 Redis 시작 시도 중...${NC}"
                cd "$PROJECT_ROOT"
                if docker-compose up -d redis > /dev/null 2>&1; then
                    sleep 2
                    if check_port "$REDIS_PORT"; then
                        echo -e "${GREEN}🐳 Redis Docker Compose 시작 완료${NC}"
                        return
                    fi
                fi
            elif docker compose version > /dev/null 2>&1; then
                # Docker Compose V2 (docker compose)
                echo -e "${GREEN}🐳 Docker Compose V2로 Redis 시작 시도 중...${NC}"
                cd "$PROJECT_ROOT"
                if docker compose up -d redis > /dev/null 2>&1; then
                    sleep 2
                    if check_port "$REDIS_PORT"; then
                        echo -e "${GREEN}🐳 Redis Docker Compose 시작 완료${NC}"
                        return
                    fi
                fi
            fi
        fi
        
        # Docker Compose가 없거나 실패한 경우 기존 방식으로 시도
        echo -e "${GREEN}🐳 Docker로 Redis 시작 시도 중...${NC}"
        
        # 기존 컨테이너가 중지된 상태로 있는지 확인
        if docker ps -a --format '{{.Names}}' 2>/dev/null | grep -q "^${REDIS_CONTAINER_NAME}$"; then
            echo -e "${YELLOW}🐳 기존 Redis 컨테이너 시작 중...${NC}"
            if docker start "$REDIS_CONTAINER_NAME" > /dev/null 2>&1; then
                sleep 2
                if check_port "$REDIS_PORT"; then
                    echo -e "${GREEN}🐳 Redis Docker 컨테이너 시작 완료${NC}"
                    return
                fi
            fi
        fi
        
        # 새 컨테이너 생성 및 시작
        echo -e "${GREEN}🐳 새 Redis Docker 컨테이너 생성 중...${NC}"
        if docker run -d \
            --name "$REDIS_CONTAINER_NAME" \
            -p "${REDIS_PORT}:6379" \
            --restart unless-stopped \
            redis:7-alpine > /dev/null 2>&1; then
            sleep 2
            if check_port "$REDIS_PORT"; then
                echo -e "${GREEN}🐳 Redis Docker 컨테이너 시작 완료${NC}"
                return
            fi
        fi
    fi
    
    # Docker가 없거나 실패한 경우 로컬 redis-server 확인
    if command -v redis-server > /dev/null 2>&1; then
        echo -e "${GREEN}🖥️  로컬 Redis 서버 시작 시도 중...${NC}"
        redis-server --port "$REDIS_PORT" --daemonize yes > /dev/null 2>&1
        sleep 2
        if check_port "$REDIS_PORT"; then
            echo -e "${GREEN}🖥️ 로컬 Redis 서버 시작 완료${NC}"
            return
        fi
    fi
    
    # 모든 방법 실패
    if ! is_docker_running; then
        echo -e "${RED}👿 Redis 시작 실패: Docker daemon이 실행 중이 아닙니다.${NC}"
        echo -e "${YELLOW}💡 Docker Desktop을 시작하거나 다음 명령으로 Docker를 시작하세요:${NC}"
        echo -e "  ${YELLOW}open -a Docker${NC} (macOS)"
    else
        echo -e "${RED}👿 Redis 시작 실패. 다음 중 하나를 설치하세요:${NC}"
        echo -e "  1. Docker & Docker Compose: ${YELLOW}https://www.docker.com/get-started${NC}"
        echo -e "  2. Redis: ${YELLOW}https://redis.io/download${NC}"
    fi
    echo -e "${YELLOW}⚠️  Redis 없이 서비스를 계속 실행합니다. (토큰 블랙리스트 기능이 작동하지 않을 수 있습니다)${NC}"
}

# Gradle Wrapper 실행 권한 확인
if [ ! -x "./gradlew" ]; then
    chmod +x ./gradlew
fi

# Redis 시작 (Eureka Server보다 먼저)
start_redis

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
    # export된 환경 변수는 자동으로 자식 프로세스에 상속됨
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
echo -e "  - Swagger UI: ${GREEN}http://localhost:8000/swagger-ui.html${NC}"
echo -e "  - Redis: ${GREEN}localhost:${REDIS_PORT}${NC}"
echo ""
echo -e "로그 확인: ${YELLOW}dev-scripts/pids/*.log${NC}"
echo -e "서비스 종료: ${YELLOW}./dev-scripts/stop.sh${NC}"

