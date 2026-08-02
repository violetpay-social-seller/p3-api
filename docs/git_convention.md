## 작업 전략
```
큰 작업 : Issue -> Branch -> Commit -> PR -> Review -> Merge
작은 작업 : Branch -> Commit -> PR -> Merge
긴급 수정 : fix 브랜치 -> PR -> Merge
```
### 큰 작업
- 기능 추가
- 버그 수정
- DB 변경
- API 변경
- 구조 변경 등등
### 작은 작업
- 오타
- README
- 주석
- 단순 스타일
- 설정값 소규모 수정
## 브랜치 전략
```
main
├── feat/* - 기능 개발
├── fix/* - 버그 수정
├── refactor/* - 기능 변화 없는 코드 개선
├── docs/* - 문서 수정
├── chore/* - 유지보수 및 환경 설정 
```
### 규칙
- main 에 직접 push 금지
- 모든 작업은 Branch 생성 후 진행
- Merge 는 PR통해서 진행
- Merge 완료 후 Branch 삭제
- 브랜치 내부에서는 서로 다른 커밋 타입 사용 가능
### 브랜치 네이밍
```
feat/#12-login
카테고리/이슈번호-내용
```

## 커밋 전략
```
feat: ~
fix: ~
refactor: ~
docs: ~
style: ~
test: ~
chore: ~
```