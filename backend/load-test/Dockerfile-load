# 1. Base image
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 프로젝트의 소스파일을 컨테이너로 복사
COPY .. /app

# 4. 프로젝트 빌드
RUN ./gradlew build -x test

# 5. JAR 파일 실행
CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]