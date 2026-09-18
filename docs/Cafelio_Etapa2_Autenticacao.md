# Etapa 2 — Autenticação completa

*Documento de acompanhamento. Última atualização: 10/09/2026.*

---

## Objetivo

Conforme a tabela "Autenticação e conta" do escopo v1.1, esta etapa entrega sete recursos: cadastro próprio, login com Google, login tradicional, recuperação de senha, alteração de senha, foto de perfil e tratamento de erros com mensagens claras.

## Situação atual

| Recurso | Estado |
|---|---|
| Cadastro próprio | Backend concluído — falta a tela |
| Login com Google | Concluído e integrado à `develop` |
| Login tradicional | Concluído — aguardando revisão em Pull Request |
| Autenticação por JWT (pré-requisito técnico) | Concluído — aguardando revisão em Pull Request |
| Tratamento de erros | Concluído |
| Alterar senha | Não iniciado |
| Recuperação de senha | Não iniciado |
| Foto de perfil | Não iniciado |

---

## O que já funciona

### Cadastro próprio — `POST /auth/register`

Recebe nome de usuário, e-mail, senha e confirmação. As validações estão nas anotações do `RegisterRequest`:

- Nome de usuário entre 3 e 50 caracteres, único no sistema
- E-mail em formato válido, único no sistema
- Senha com no mínimo 8 caracteres, contendo maiúscula, minúscula e número (validado por expressão regular)
- Confirmação conferida no `AuthService.register()`

A senha nunca é armazenada em texto puro: o `PasswordEncoder` (BCrypt) gera um hash gravado em `password_hash`.

Ainda **não existe tela** para este endpoint — hoje só é possível criar conta pelo Swagger ou por chamada direta à API.

### Login com Google — `POST /auth/google`

O frontend usa a biblioteca Google Identity Services, que devolve um *ID token*. Esse token é enviado ao backend, onde o `GoogleTokenVerifier` confere a assinatura junto ao Google e extrai `sub` (identificador permanente da conta), e-mail e nome.

O `AuthService.loginOrRegisterWithGoogle()` então:

1. Procura alguém com aquele `google_id` — se achar, é login
2. Se não achar, procura pelo e-mail — se achar, vincula o `google_id` à conta existente (evita conta duplicada para quem já se cadastrou por senha)
3. Se não achar nenhum dos dois, cria uma conta nova, com `email_verified = true` (o Google já confirmou o e-mail) e nome de usuário gerado a partir do prefixo do e-mail

A geração do nome de usuário resolve colisões acrescentando um sufixo numérico: `maria`, `maria1`, `maria2`.

### Login tradicional — `POST /auth/login`

Recebe dois campos: `identifier` e `password`. O `identifier` aceita **e-mail ou nome de usuário** — a busca tenta primeiro por e-mail e, não encontrando, por nome de usuário. A senha é conferida com `passwordEncoder.matches()` contra o hash gravado.

### Autenticação por JWT

Os três fluxos acima devolvem um token JWT assinado, gerado pelo `JwtService` e válido por 24 horas. O frontend o guarda no `localStorage` sob a chave `cafelio_token`.

O `JwtAuthenticationFilter` intercepta toda requisição e, havendo um cabeçalho `Authorization: Bearer <token>`, valida a assinatura e a expiração e registra a usuária autenticada no contexto de segurança do Spring. Token ausente ou inválido não gera erro: a requisição simplesmente segue sem autenticação, e o `SecurityConfig` decide se aquela rota permitia isso.

Rotas públicas: `/auth/register`, `/auth/login`, `/auth/google`, `/health`, `/books/search` e as do Swagger. Todo o resto exige token.

### `GET /auth/me`

Devolve os dados de quem está autenticada, a partir do token. É o que permitirá ao frontend exibir a usuária logada, e é pré-requisito da alteração de senha e da foto de perfil, que precisam saber de quem é a conta.

### Tratamento de erros

As três mensagens previstas no escopo existem:

| Situação | Resposta |
|---|---|
| Login incorreto | `400` — "E-mail/usuário ou senha incorretos" |
| E-mail já cadastrado | `400` — "Este e-mail já está cadastrado" |
| Senha fora do padrão | `400` — mensagem da validação, por campo |
| Requisição sem autenticação em rota protegida | `401` — "Autenticação necessária" |

O `GlobalExceptionHandler` centraliza a conversão de exceções em respostas JSON. No frontend, o `apiPost` lê o corpo da resposta de erro e exibe a mensagem do backend na tela — antes mostrava apenas o código HTTP.

---

## Decisões técnicas

**Mensagem de erro genérica no login.** Usuário inexistente, senha errada e conta sem senha retornam exatamente a mesma mensagem. Mensagens específicas permitiriam descobrir quais e-mails têm conta no Cafélio testando um a um, expondo dados das usuárias. A perda de clareza é intencional.

**Contas criadas pelo Google não entram por senha.** Elas não possuem `password_hash`, e o método de login verifica isso antes de comparar senhas. *Ponto em aberto:* pela decisão acima, essa pessoa recebe a mensagem genérica e pode não entender o motivo. Vale avaliar um aviso na tela de login sugerindo tentar o Google.

**Campo único para e-mail ou nome de usuário.** Evita obrigar a pessoa a escolher previamente o tipo de credencial, como o escopo pede ("entrar com e-mail/usuário e senha").

**Sessão `STATELESS`.** Com JWT, cada requisição carrega a própria identidade; manter sessão no servidor seria redundante e prejudicaria a escalabilidade.

**401 em vez do 403 padrão.** O comportamento padrão do Spring Security para requisição não autenticada é 403 ("proibido"), que semanticamente significa "sei quem você é, mas você não pode". O correto aqui é 401 ("não autenticado"). Configurado via `authenticationEntryPoint`, devolvendo o mesmo formato `{"error": "..."}` do resto da API.

**Rotas públicas listadas explicitamente.** A regra anterior era `/auth/**`, que liberava tudo sob aquele prefixo. Com a chegada do `/auth/me`, isso o deixaria aberto. As três rotas públicas passaram a ser declaradas uma a uma.

---

## Cobertura de testes

| Onde | Testes |
|---|---|
| Já na `develop` | 11 |
| Adicionados pelo login tradicional | 8 |
| Adicionados pela autenticação JWT | 7 |
| **Total previsto após as duas integrações** | **26** |

Os testes de serviço usam Mockito puro, sem subir o Spring. Os de controller usam `@SpringBootTest` com `@MockitoBean` nas dependências externas, para não tocar o banco real nem chamar o Google.

Um detalhe relevante: o MockMvc **não** passa pela corrente do Spring Security por padrão. Sem aplicar `springSecurity()` na construção, testes de 401 passariam sem que a segurança estivesse sequer ativa — dando falsa confiança.

---

## O que falta

**1. Formulário de cadastro no frontend.** O endpoint existe; falta a tela. Sem dependências externas.

**2. Alterar senha.** Endpoint autenticado mais tela nas configurações. Depende da autenticação JWT estar integrada.

**3. Recuperação de senha.** O item mais extenso da etapa: o escopo pede **três** mecanismos — link por e-mail, código por e-mail e código por telefone. Vai exigir:
- Uma migration nova para a tabela de tokens de recuperação, com expiração
- Configuração do Spring Mail (a dependência já está no `pom.xml`, mas nunca foi configurada nem usada)
- Dependência e conta no Twilio, para o SMS

**4. Foto de perfil.** O campo `profile_picture_url` já existe na tabela de usuárias, mas não há upload nem integração com o Cloudinary. Observação de organização: o Cloudinary aparece no escopo apenas na Etapa 5, então vale definir quem configura a conta para não duplicar o trabalho.

---

## Notas de infraestrutura

**Banco compartilhado.** As duas desenvolvedoras usam a mesma instância do Supabase. Isso tem duas consequências práticas:

*Migrations afetam todo mundo na hora.* Uma migration aplicada a partir de uma branch muda o banco de ambas, mesmo antes de ser mesclada. O banco já está na versão 2 do schema, enquanto a `develop` só contém a `V1` — a próxima migration a ser criada deve ser `V3`, e não `V2`, sob pena de conflito.

*Migration aplicada não deve ser editada.* O Flyway guarda uma assinatura de cada arquivo aplicado. Alterar um arquivo já executado faz a aplicação falhar na inicialização, com erro de incompatibilidade de assinatura — para todas as pessoas do projeto. Correções devem vir sempre em uma migration nova.

**Limite de conexões.** O plano gratuito do Supabase limita as sessões simultâneas. Manter a aplicação rodando enquanto se executa a suíte de testes pode estourar esse limite, e a falha aparece como erro de contexto do Spring, parecendo um problema de código. Regra prática: parar a aplicação antes de rodar os testes.

**Ambiente de execução.** Há um `compose.yaml` com Java 25 padronizado, útil para quem tenha uma versão diferente no terminal. Não é obrigatório: `./mvnw test` direto funciona para quem já esteja em Java 25.

---

## Conflito previsto na integração

As duas Pull Requests abertas partiram do mesmo ponto da `develop` e alteraram **os mesmos arquivos**:

| Arquivo | Login tradicional | Autenticação JWT |
|---|---|---|
| `AuthController.java` | adiciona `POST /auth/login` | adiciona `GET /auth/me` |
| `AuthService.java` | adiciona `login()` | adiciona `findById()` |
| `AuthControllerTest.java` | adiciona 3 testes | adiciona 3 testes e altera o `setUp` |

A primeira a ser integrada entra sem atrito. A segunda vai apresentar conflito nesses três arquivos.

**A resolução é simples e sempre a mesma: manter os dois lados.** Os métodos e testes são independentes e complementares — nenhum substitui o outro. O único ponto que exige atenção é o `setUp` do `AuthControllerTest`, onde deve prevalecer a versão que aplica `springSecurity()`.

Vale resolver esse conflito assim que possível: os dois casos anteriores em que uma dependência se perdeu no `pom.xml` aconteceram exatamente em conflitos assim, resolvidos escolhendo um lado em vez de manter ambos.
