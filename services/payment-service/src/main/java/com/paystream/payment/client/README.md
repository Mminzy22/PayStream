# Client 패키지

이 패키지는 다른 마이크로서비스와 통신하기 위한 Feign Client 인터페이스를 포함합니다.

## UserServiceClient

`UserServiceClient`는 User Service와 통신하기 위한 Feign Client입니다.

### 사용 목적
- 결제 요청 생성 시 사용자 정보 조회
- 사용자의 이름, 이메일, 전화번호 등 구매자 정보 자동 설정

### 사용 위치
- `PaymentService.createPayment()`: 결제 요청 생성 시 사용자 정보 조회

### 설정
- `@FeignClient(name = "user-service", path = "/users")`
- Eureka를 통해 user-service를 자동으로 찾습니다.

