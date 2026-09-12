# AWS Dev ECS 배포

## 배포 구조

- ECS launch type: EC2
- ECS network mode: bridge
- ECR repository: `p3-ecr-api-dev`
- Image tag: Git commit SHA
- ALB listener: HTTP 80
- HTTPS/ACM: 미사용
- External HTTPS: Cloudflare에서 처리
- Health check path: `/actuator/health/liveness`

## AWS 리소스

- Cluster: `p3-ecs-cluster-dev`
- Service: `p3-api-dev`
- Task definition family: `p3-api-dev`
- Container: `p3-api`
- Log group: `/ecs/p3-api-dev`
- ECR URI: `851563824752.dkr.ecr.ap-northeast-2.amazonaws.com/p3-ecr-api-dev`
- Target group: `p3-tg-api-ec2-dev`
- ALB DNS: `p3-alb-dev-991901093.ap-northeast-2.elb.amazonaws.com`
- Launch template: `lt-083bc41e8834652c8`
- Launch template version: `3`
- Current task definition: `p3-api-dev:4`

## GitHub Actions variables

- `AWS_REGION=ap-northeast-2`
- `AWS_ROLE_TO_ASSUME=arn:aws:iam::851563824752:role/p3-role-github-actions-deploy-dev`
- `ECR_REPOSITORY=p3-ecr-api-dev`
- `ECS_CLUSTER=p3-ecs-cluster-dev`
- `ECS_SERVICE=p3-api-dev`
- `ECS_TASK_DEFINITION=p3-api-dev`
- `ECS_CONTAINER_NAME=p3-api`

## SSM parameters

실제 비밀값은 GitHub Secrets가 아니라 AWS SSM Parameter Store에 둔다.

- `/p3/dev/api/POSTGRES_PASSWORD`
- `/p3/dev/api/P3_INTERNAL_AUTH_TOKEN`
- `/p3/dev/api/P3_ACCOUNT_ENCRYPTION_KEY`
- `/p3/dev/api/P3_KFTC_CLIENT_ID`
- `/p3/dev/api/P3_KFTC_CLIENT_SECRET`
- `/p3/dev/api/P3_KFTC_USE_ORG_CODE`

정산계좌 암호화 키는 32바이트 값을 Base64로 인코딩해 SecureString으로 저장한다. ECS에는 `P3_ACCOUNT_ENCRYPTION_KEY` Secret으로 연결하고 키 버전은 `P3_ACCOUNT_ENCRYPTION_KEY_VERSION=v1` 환경변수로 관리한다. 실제 계좌정보가 저장된 뒤에는 기존 키를 임의로 변경하지 않는다.

금융결제원 계좌실명조회와 기관용 OAuth 요청은 `https://openapi.openbanking.or.kr`를 사용한다. Client ID, Client Secret과 이용기관 코드는 SSM SecureString에서 ECS Secret으로 주입하며 저장소와 로그에 실제 값을 기록하지 않는다.

금융결제원 운영 승인 전 dev 환경은 `P3_KFTC_VERIFICATION_ENABLED=false`로 설정하여 입력값 기반 임시 정산계좌 등록을 사용한다. 이 모드는 실제 계좌와 예금주를 검증하지 않으므로 실제 정산 기능을 활성화하기 전에 반드시 `true`로 전환하고 기존 임시 등록 계좌의 재검증 정책을 적용해야 한다.

Point3 운영 연동값을 dev ECS에서 활성화할 때는 SSM SecureString으로 추가한 뒤 task definition의 `secrets`에 연결한다.

- `/p3/dev/api/P3_POINT3_CLIENT_ID`
- `/p3/dev/api/P3_POINT3_API_TOKEN`

## 네트워크 기준

NAT Gateway를 사용하지 않으므로 ECS container instance는 public subnet에서 outbound가 가능해야 한다.

- ASG subnet: `subnet-090003f5128ce53ef`, `subnet-0f07842750a5a144e`
- Launch template version 3은 version 2와 동일한 설정에서 `AssociatePublicIpAddress=true`만 반영한다.
- ASG는 `$Latest`를 사용하므로 새 ECS container instance는 launch template version 3으로 생성된다.

ECS task는 `bridge` 모드에서 `hostPort: 8080`으로 실행한다. ALB target group은 `instance` 타입을 사용한다.

단일 `t3.small` EC2 instance에서 실행하므로 rolling deployment 중 태스크 2개가 동시에 배치되지 않도록 ECS service 배포 설정을 아래처럼 둔다.

- `maximumPercent=100`
- `minimumHealthyPercent=0`
- `healthCheckGracePeriodSeconds=180`
- `availabilityZoneRebalancing=DISABLED`

앱 부팅에 약 90초가 걸리므로 ALB health check grace가 필요하다. Target group deregistration delay는 dev 배포 대기 시간을 줄이기 위해 30초로 설정한다.

## 현재 dev 상태

- ASG desired capacity: `1`
- ECS service desired count: `1`
- ECS running task: `1`
- ALB target health: `healthy`
- ALB liveness response: `200 {"status":"UP"}`
- RDS `p3-rds-dev`: running
- RDS database: `p3`
- Flyway: v1-v5 applied

RDS master password는 2026-09-01에 재설정했고, 값은 SSM SecureString `/p3/dev/api/POSTGRES_PASSWORD`에 저장한다. 값은 문서나 GitHub에 기록하지 않는다.

## 남은 운영 설정

- Cloudflare DNS에서 앞단 HTTPS를 구성하고 ALB HTTP 80으로 연결한다.
- 실제 frontend origin이 정해지면 `P3_CORS_ALLOWED_ORIGINS`, `P3_WEBSOCKET_ALLOWED_ORIGINS`, `P3_WEB_BASE_URL` 값을 ECS task definition에 반영한다.
- Point3 운영 연동을 활성화하려면 `P3_POINT3_CLIENT_ID`, `P3_POINT3_API_TOKEN` 값을 발급받아 SSM SecureString에 저장하고 task definition의 `secrets`에 연결한다.

## 이미지 callback 설정

Lambda `p3-lambda-image-dev`는 processed 이미지 생성 후 API callback으로 `asset_variants`를 등록한다.

- `API_BASE_URL=https://api.wihada.com`
- `INTERNAL_API_KEY`: SSM SecureString `/p3/dev/api/P3_INTERNAL_AUTH_TOKEN`과 같은 값
