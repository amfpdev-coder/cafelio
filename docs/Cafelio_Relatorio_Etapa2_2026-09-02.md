# Relatório de desenvolvimento — Etapa 2 (Autenticação completa)

**Data:** 02/09/2026
**Responsável:** Angelica Mariana (`amfpdev-coder`)
**Branch de trabalho:** `feature/login-tradicional`

---

## Resumo

O foco do dia foi o **login tradicional** (entrar com e-mail ou nome de usuário e senha), terceiro item da tabela "Autenticação e conta" do escopo v1.1. A funcionalidade foi entregue completa — backend, frontend e testes automatizados.

Em paralelo, foram identificadas e tratadas duas regressões de dependência herdadas da branch `feature/book-search-api`, que impediam o projeto de compilar e de subir.

Total: **7 arquivos alterados, 201 linhas adicionadas**, distribuídos em 2 commits de funcionalidade e 1 commit de correção.

---

## 1. Login tradicional (entregue)

### Backend

| Arquivo | Alteração |
|---|---|
| `dto/LoginRequest.java` | **Novo.** DTO com os campos `identifier` (e-mail ou nome de usuário) e `password`, ambos validados com `@NotBlank` |
| `service/AuthService.java` | Novo método `login(LoginRequest)`: busca o usuário por e-mail, depois por nome de usuário, e valida a senha com o `PasswordEncoder` (BCrypt) |
| `controller/AuthController.java` | Novo endpoint `POST /auth/login`, retornando `AuthResponse` com o JWT — mesmo formato do login com Google |

Não foi necessário alterar o `SecurityConfig`: a rota `/auth/**` já estava liberada. O `UserRepository` também já possuía os métodos `findByEmail` e `findByUsername` usados pela busca.

### Frontend

| Arquivo | Alteração |
|---|---|
| `js/app.js` | Formulário de login na tela inicial (e-mail/usuário, senha, botão) e função `handleLogin` que chama `/auth/login` e guarda o token no `localStorage` |
| `js/api.js` | O `apiPost` passou a ler o corpo da resposta de erro e propagar a mensagem vinda do backend, em vez de descartá-la e mostrar apenas o código HTTP |

A melhoria no `api.js` atende diretamente o requisito "Tratamento de erros — mensagens claras" do escopo: antes, qualquer falha virava `"Erro na API: 400"` na tela.

### Testes

**8 testes novos**, todos passando:

`AuthServiceTest` (5 novos, 9 no total):
- login com e-mail correto retorna o usuário
- login com nome de usuário correto retorna o usuário
- senha incorreta lança exceção
- usuário inexistente lança exceção
- conta criada pelo Google (sem senha) não permite login por senha

`AuthControllerTest` (3 novos, 6 no total):
- credenciais válidas retornam o JWT
- credenciais inválidas retornam 400 com a mensagem de erro
- requisição sem os campos obrigatórios retorna 400

Suíte completa validada: **19 testes, 0 falhas**.

---

## 2. Decisões técnicas tomadas

**Campo único `identifier` em vez de dois campos separados.** O escopo pede "entrar com e-mail/usuário e senha". Um único campo que aceita os dois é mais simples para quem usa e evita a pessoa ter que escolher previamente qual tipo de credencial vai digitar.

**Mensagem de erro genérica em todas as falhas de login.** Usuário inexistente, senha errada e conta sem senha retornam exatamente a mesma mensagem: *"E-mail/usuário ou senha incorretos"*. Se as mensagens fossem específicas, seria possível descobrir quais e-mails têm conta no Cafélio testando um por um — o que expõe dados das usuárias. A perda de clareza é intencional e é a prática recomendada para telas de login.

**Contas criadas pelo Google não entram por senha.** Quem se cadastrou via OAuth não tem `passwordHash` gravado. O método `login` verifica isso antes de comparar senhas. *Ponto de atenção para o futuro:* pela decisão acima, essa pessoa recebe a mensagem genérica e pode ficar sem entender por que não consegue entrar. Vale avaliar um aviso na tela de login do tipo "já tentou entrar com o Google?".

**Mensagens de erro na própria tela, não em `alert()`.** O fluxo do login com Google ainda usa `alert`; vale alinhar os dois numa próxima iteração.

---

## 3. Correções de infraestrutura

Durante o desenvolvimento, o projeto parou de compilar por motivos não relacionados ao login. Investigando o histórico, as duas causas foram rastreadas ao merge `4f5f1ce` (`develop` → `feature/book-search-api`), onde um conflito no `pom.xml` foi resolvido descartando blocos de dependência em vez de mantê-los.

### 3.1 `google-api-client` — corrigido (PR #3)

**Sintoma:** `package com.google.api.client.googleapis.auth.oauth2 does not exist`

A dependência usada pelo `GoogleTokenVerifier` havia sumido do `pom.xml`. Rastreamento commit a commit:

| Commit | Tem a dependência? |
|---|---|
| `1120776` feat: login com Google | sim |
| `44aae37` Merge PR #1 (google-oauth → develop) | sim |
| `4f5f1ce` Merge develop → feature/book-search-api | **não** |
| `95e1b25` Merge PR #2 (book-search → develop) | não |

Corrigido na branch `fix/dependencia-google-api-client`, commit `3755c6c`, mesclado pela PR #3.

### 3.2 `spring-boot-starter-restclient` — diagnosticado, pendente

**Sintoma:** `No qualifying bean of type 'org.springframework.web.client.RestClient$Builder' available`

O `OpenLibraryClient` recebe um `RestClient.Builder` no construtor, mas nenhum bean desse tipo existe no contexto. A causa é que, no **Spring Boot 4**, a auto-configuração do `RestClient` foi movida para um módulo próprio (`spring-boot-starter-restclient`) e não vem mais junto com o `spring-boot-starter-webmvc`, como acontecia nas versões anteriores.

Consequência: o contexto do Spring não sobe. Isso derruba a aplicação inteira e todos os testes que usam `@SpringBootTest` — apenas os testes de unidade com mock continuam passando, o que faz o problema não aparecer numa verificação superficial.

A correção (adicionar o starter ao `pom.xml`, sem `<version>`, pois o BOM já gerencia) foi **testada e validada** — com ela a suíte passa 19/19. A alteração foi revertida para ser feita pela responsável pela Etapa 3, na reunião do dia seguinte.

---

## 4. Organização do repositório

- Repositório local sincronizado com a `develop` (que estava 9 commits atrás, sem o trabalho das duas desenvolvedoras)
- Branches já mescladas removidas, local e remotamente: `fix/dependencia-google-api-client`, `feature/google-oauth` e `feature/book-search-api`
- Referências locais obsoletas limpas com `git fetch --prune`

---

## 5. Situação da Etapa 2

| Recurso do escopo | Estado |
|---|---|
| Cadastro próprio | Concluído (endpoint; falta a tela) |
| Login com Google | Concluído |
| Login tradicional | **Concluído hoje** |
| Tratamento de erros | Concluído — as três mensagens previstas no escopo existem |
| Filtro de autenticação JWT | Pendente |
| Formulário de cadastro no frontend | Pendente |
| Alterar senha | Pendente |
| Recuperação de senha (link e-mail, código e-mail, código SMS) | Pendente |
| Foto de perfil (Cloudinary) | Pendente |

### Observação sobre a autenticação JWT

O token é gerado corretamente nos três fluxos de login, mas **nada o valida nas requisições**: não existe filtro lendo o cabeçalho `Authorization: Bearer ...`, e o método `JwtService.extractUserId()` nunca é chamado. O `SecurityConfig` exige autenticação nas rotas não liberadas, mas cai no comportamento padrão do Spring Security.

É pré-requisito técnico para "alterar senha" e "foto de perfil", que são funcionalidades de usuária logada. Por isso é o próximo item da fila.

---

## 6. Próximos passos

**Imediato (depende de reunião):**
1. Corrigir a dependência `spring-boot-starter-restclient` na `develop`
2. Mesclar a `develop` na `feature/login-tradicional`, rodar a suíte e abrir a PR do login tradicional

**Etapa 2, na ordem planejada:**
3. Filtro de autenticação JWT — sem dependências externas
4. Formulário de cadastro no frontend — sem dependências externas
5. Alterar senha — depende do item 3
6. Recuperação de senha — depende de conta SMTP (e-mail) e Twilio (SMS)
7. Foto de perfil — depende de conta Cloudinary

**Acordos a firmar com a equipe:**
- Rodar `./mvnw test` antes de abrir Pull Request (as duas regressões de dependência teriam sido detectadas)
- Em conflito no `pom.xml`, manter os blocos dos dois lados em vez de escolher um
- Definir quem configura o Cloudinary: a foto de perfil é da Etapa 2, mas o Cloudinary só aparece na Etapa 5

---

## Commits do dia

```
ce3cbe2  test: adiciona testes do login tradicional
2e3afad  feat: adiciona login tradicional com e-mail ou usuario e senha
f303dec  Merge pull request #3 from amfpdev-coder/fix/dependencia-google-api-client
3755c6c  fix: restaura dependencia google-api-client perdida no merge
```
