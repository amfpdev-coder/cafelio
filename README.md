# Cafélio

Planner literário digital — vintage e aconchegante.

## Stack

- Backend: Java 25 + Spring Boot 4.1.1 (Maven)
- Banco de dados: PostgreSQL via Supabase
- Frontend: HTML + CSS + JavaScript puro (SPA)
- Migrations: Flyway
- Testes: JUnit 5 + MockMvc

## Como rodar o backend

1. Instale o JDK 25 (Eclipse Temurin) e confirme com `java -version`
2. `cd backend`
3. Copie as credenciais do banco (Supabase → Connect → Session pooler → JDBC) para `src/main/resources/application-local.yaml` (esse arquivo não vai pro Git)
4. Rode:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
5. Acesse `http://localhost:8000/health`

## Como rodar o frontend

1. Clique com o botão direito em `frontend/index.html` e escolha "Open with Live Server" (extensão do VS Code)
- **Não abra o arquivo direto pelo explorador** — isso quebra a conexão com a API por causa de CORS
2. Com o backend rodando, a tela deve mostrar "API conectada — status: ok"

## Como rodar os testes

Dentro de `backend/`, com o Java configurado:
.\mvnw.cmd test "-Dspring.profiles.active=local"