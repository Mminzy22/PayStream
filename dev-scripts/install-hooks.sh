#!/bin/bash
#
# Git hooks 설치 스크립트
# githooks/ 디렉토리의 hooks를 .git/hooks/로 복사합니다
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GITHOOKS_DIR="$PROJECT_ROOT/githooks"
GIT_HOOKS_DIR="$PROJECT_ROOT/.git/hooks"

echo "Git hooks 설치 중..."

if [ ! -d "$GITHOOKS_DIR" ]; then
    echo "😈 오류: githooks 디렉토리를 찾을 수 없습니다: $GITHOOKS_DIR"
    exit 1
fi

if [ ! -d "$GIT_HOOKS_DIR" ]; then
    echo "⚠️  .git/hooks 디렉토리가 없어서 생성합니다..."
    mkdir -p "$GIT_HOOKS_DIR"
fi

# githooks/의 모든 hooks를 .git/hooks/로 복사
for hook in "$GITHOOKS_DIR"/*; do
    if [ -f "$hook" ]; then
        hook_name=$(basename "$hook")
        target="$GIT_HOOKS_DIR/$hook_name"
        
        echo "$hook_name 설치 중..."
        cp "$hook" "$target"
        chmod +x "$target"
    fi
done

echo "❤️ Git hooks 설치 완료!"

