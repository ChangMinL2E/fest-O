# 베이스 이미지 선택
FROM python:3.9.11

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . /app

# 종속성 설치
RUN pip install --no-cache-dir -r requirements.txt

# 포트 열기
EXPOSE 8000

# 애플리케이션 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
