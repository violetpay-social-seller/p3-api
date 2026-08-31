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

Point3 운영 연동값을 dev ECS에서 활성화할 때는 SSM SecureString으로 추가한 뒤 task definition의 `secrets`에 연결한다.

- `/p3/dev/api/P3_POINT3_CLIENT_ID`
- `/p3/dev/api/P3_POINT3_API_TOKEN`

## 네트워크 기준

NAT Gateway를 사용하지 않으므로 ECS container instance는 public subnet에서 outbound가 가능해야 한다.

- ASG subnet: `subnet-090003f5128ce53ef`, `subnet-0f07842750a5a144e`
- Launch template은 변경하지 않는다.
- Public subnet 이동 후 EC2 public IP가 붙지 않으면 launch template network interface 설정 확인이 필요하다.

ECS task는 `bridge` 모드에서 `hostPort: 8080`으로 실행한다. ALB target group은 `instance` 타입을 사용한다.

## 현재 차단 조건

ASG를 public subnet으로 옮긴 뒤 생성된 EC2 instance에 public IP가 붙지 않았다. Launch template의 network interface 설정이 public IP 자동 할당을 끄고 있으므로, NAT Gateway 없이 outbound를 확보하려면 launch template 수정 또는 다른 outbound 경로 구성이 필요하다. Launch template은 사용자 결정사항에 따라 이 작업에서 변경하지 않는다.

ECS service는 `desiredCount=0`으로 생성되어 있다. 실제 dev 배포 전 아래 조건을 먼저 해결한다.

- ECS EC2 instance outbound 확보
- `/p3/dev/api/POSTGRES_PASSWORD` SecureString 생성
- 필요 시 RDS `p3-rds-dev` 시작
