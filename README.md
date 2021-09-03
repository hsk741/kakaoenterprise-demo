# 카카오 계정을 이용한 사용자 관리 서비스 개발
## 목차
- [개발 환경 및 프레임워크](#개발-환경-및-프레임워크)
- [프로젝트 빌드와 실행방법](#프로젝트-빌드와-실행방법)
- [문제해결전략](#문제해결전략)



## 개발 환경 및 프레임워크
- 기본 환경
  - IDE: IntelliJ IDEA
  - OS: MAC
- Back-End
  - Java16
  - Spring Boot 2.5.4
  - Spring Framework 5.3.9
  - Spring Data JPA 2020.0.1
  - QueryDSL 5.0.0
  - H2 1.4.200
  - Tomcat 9.0.52(Embedded) 
  - Jackson 2.12.5
  - JUnit5 5.7.2
  - Mockito 3.12.4
  - Gradle Wrapper

## 프로젝트 빌드와 실행방법
### 터미널 환경
- Maven, Git, Java 설치 및 실행명령어(mvn, java, git)를 위한 경로들을 환경변수에 등록한다.
- H2 데이터베이스를 in-memory mode로 구성
  - JDBC url : jdbc:h2:mem:member;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
- spring boot 어플리케이션을 아래와 같이 빌드 및 실행시킨다.
  - 프로젝트 소스내 console에서 실행
```
$ cd kakaoenterprise-demo
$ ./gradlew bootRun 
`````

  - console에서 빌드 및 실행
```
$ cd kakaoenterprise-demo
$ ./gradlew build
$ cd build/libs
$ java -jar kakaoenterprise-demo-0.0.1-SNAPSHOT.war  
```

### Intellij 환경
- 파일 인코딩 UTF-8
- Lombok, QueryDSL을 활성화하기 위해 annotation processing를 활성화해야 한다.

## 문제해결전략
### 1. 카카오 로그인 및 회원 프로필 구현
#### 흐름도
- 웹 브라우저에서 최초 [http://localhost:8080]()으로 접속하며 [카카오계정으로 로그인]이 표시
  - [로그인]버튼을 누르면 authorization endpoint를 통해 kakao인증서버로 접속
  - 프로필 정보(닉네임, 이메일, 프로필 이미지)를 표시하고 아래 [로그아웃] 및 [탈퇴]버튼을 표시
    - 이미 kakao인증서버로 로그인된 상태가 아닌 경우 kakao 계정관리서버에서 제공하는 로그인 페이지로 이동하여 id/pwd를 입력
    - 위의 경우 이후나 예전에 로그인 된 상태면 authorization endpoint를 호출하여 인증서버로부터 code(인가 코드) 수신을 통한 자격증명 
      다음 token endpoint를 호출하여 access token을 획득한 후 kakao API서버로부터 사용자 정보 조회를 통해 프로필 정보를 표시
    - 사용자 정보 프로필 정보 조회 후 카카오 계정 ID로 서비스 회원(member)테이블을 조회하여 존재하지 않으면 회원 가입 처리
    - access token를 획득한 후 사용자 프로필 정보를 조회하면 서비스 UI와 서버 간 session 생성
- [로그아웃]이나 [탈퇴]를 선택한다.
    - 로그아웃 시 회원정보는 미삭제, 서비스 UI와 서버간 session 및 kakao API server로 access token을 송신하여 invalidate
    - 탈퇴시 로그아웃 기능에 서비스 회원 정보까지 삭제

### 2. 회원 관리 API 구현
#### 구현
- Rest API형태로 response는 json포맷으로 응답한다.
- 기능 요청 url은 /api로 시작하며 해당 url은 earerTokenRequestInterceptor를 반드시 수행 후 요청 API 접근 
- BearerTokenRequestInterceptor은 API 기능 호출 전에 사전 Bearer token에 대한 검증을 수행
  - access token 유효기간을 확인하여 만료되었고 refresh token이 존재하면 kakao 인증서버에 재발급 요청을 통해 access token을 재발급받아 session에 저장
  - 예전에 재발급 access token이 만료된 경우 서비스 로그인 페이지로 이동
  - session이 미존재하거나 access token이 만료되고 refresh token이 없는 경우 서비스 로그인 페이지 이동
  - session에 저장된 access token과 Bearer형식으로 전달된 access token값이 다른 경우 예외 발생
- 해당 기능은 JUnit5 기반의 단위 테스트로 구성하였으며 src/test/java/controller/MemberRestControllerTest를 수행하면 테스트 가능
  - 기능에 대한 설명은 단위 테스트 메소드별 @Display에 상세하게 기술
- 개인 회원 ID로 정보 등록
  - 카카오 로그인시 받은 회원 정보 및 access token을 전송하여 회원 테이블에 추가
- 개인 회원 ID로 정보 수정
  - nickname만 수정할 수 있다고 가정한 상태에서 카카오 로그인시 받은 회원id 및 access token, 수정한 nickname을 전송하여 회원 레코드 수정
- 개인 회원 ID로 정보 삭제
  - 카카오 로그인시 받은 회원id를 전송하여 카카오 계정 토큰 철회 및 회원 테이블에서 레코드 삭제
- 개인 회원 ID로 정보 조회
  - 카카오 로그인시 받은 회원id를 전송하여 회원 테이블에서 해당 id에 대한 회원 정보 전송
- 페이징, 정렬, 검색 기능 포함 * 아래 기준으로 회원 목록 검색 - 연령대 - 이메일 도메인(xxx.com)
  - 해당 기능은 동적 query 형태로 사용하고자 QueryDSL을 사용