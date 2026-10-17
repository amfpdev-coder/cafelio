# Etapa 2 — Autenticação completa

*Documento de acompanhamento. Última atualização: 07/10/2026.*

---

## Objetivo

Conforme a tabela "Autenticação e conta" do escopo v1.1, esta etapa entrega sete recursos: cadastro próprio, login com Google, login tradicional, recuperação de senha, alteração de senha, foto de perfil e tratamento de erros com mensagens claras.

## Situação atual

| Recurso | Estado |
|---|---|
| Cadastro próprio | Concluído — endpoint e tela |
| Login com Google | Concluído |
| Login tradicional | Concluído |
| Autenticação por JWT (pré-requisito técnico) | Concluído |
| Alterar senha | Concluído — endpoint e tela |
| Recuperação de senha | Concluído — link e código de 6 dígitos, ambos por e-mail |
| Foto de perfil | Concluído — aguardando revisão em Pull Request |
| Tratamento de erros | Concluído |

**A etapa está entregue.** A recuperação por SMS, prevista na versão original do escopo, foi retirada — o motivo está registrado na seção "Fora do escopo" do `Cafelio_Escopo_v1.1.md`.

---

## Endpoints

| Método | Rota | Autenticação | O que faz |
|---|---|---|---|
| `POST` | `/auth/register` | pública | Cria conta com nome de usuário, e-mail e senha |
| `POST` | `/auth/login` | pública | Entra com e-mail **ou** nome de usuário |
| `POST` | `/auth/google` | pública | Entra ou cria conta a partir do ID token do Google |
| `GET` | `/auth/me` | token | Dados de quem está autenticada, incluindo a foto |
| `PUT` | `/auth/password` | token | Troca a senha conhecendo a atual |
| `POST` | `/auth/password/reset-request` | pública | Dispara o e-mail com link e código |
| `POST` | `/auth/password/reset` | pública | Redefine a senha pelo link |
| `POST` | `/auth/password/reset-code` | pública | Redefine a senha pelo código de 6 dígitos |
| `POST` | `/auth/profile-picture` | token | Envia a foto de perfil (multipart) |
| `DELETE` | `/auth/profile-picture` | token | Remove a foto de perfil |

Rotas públicas são declaradas uma a uma no `SecurityConfig`. Todo o resto cai em `anyRequest().authenticated()`.

---

## O que cada recurso faz

### Cadastro próprio

Nome de usuário entre 3 e 50 caracteres e único, e-mail válido e único, senha com no mínimo 8 caracteres contendo maiúscula, minúscula e número. A confirmação é conferida no `AuthService`. A senha nunca é gravada em texto puro: o BCrypt gera um hash em `password_hash`.

### Login com Google

O frontend usa o Google Identity Services, que devolve um *ID token*. O `GoogleTokenVerifier` confere a assinatura junto ao Google e extrai `sub`, e-mail e nome. O `AuthService.loginOrRegisterWithGoogle()` então procura pelo `google_id`; não achando, procura pelo e-mail e vincula à conta existente; não achando nenhum dos dois, cria conta nova com `email_verified = true` e nome de usuário derivado do prefixo do e-mail, resolvendo colisões com sufixo numérico (`maria`, `maria1`, `maria2`).

### Login tradicional

Campo único `identifier`, que aceita e-mail ou nome de usuário. A busca tenta primeiro por e-mail. A senha é conferida com `passwordEncoder.matches()`.

### Autenticação por JWT

Os três fluxos de entrada devolvem um token assinado, válido por 24 horas, guardado pelo frontend em `localStorage` sob a chave `cafelio_token`.

O `JwtAuthenticationFilter` intercepta toda requisição e, havendo `Authorization: Bearer <token>`, valida assinatura e expiração e popula o contexto de segurança. Token ausente ou inválido não gera erro no filtro: a requisição segue sem autenticação, e o `SecurityConfig` decide se aquela rota permitia isso.

### Alterar senha

Rota autenticada que exige a senha atual. Contas criadas pelo Google não possuem senha — para elas a tela muda para "Definir senha" e o campo de senha atual não aparece.

### Recuperação de senha

Um único pedido em `/auth/password/reset-request` gera **duas** formas de recuperar, enviadas no mesmo e-mail:

- um **link** com um token aleatório de uso único
- um **código de 6 dígitos**

Ambos valem por **30 minutos** e compartilham o mesmo registro na tabela `password_reset_tokens`. Nenhum dos dois é gravado em texto puro: o banco guarda o hash SHA-256 de cada um.

O código tem limite de **5 tentativas erradas**. Estourado o limite, ele deixa de funcionar mesmo estando correto, e é preciso pedir um novo.

### Foto de perfil

A imagem é enviada ao Cloudinary e o banco guarda apenas o `public_id` devolvido por ele, na coluna `profile_picture_public_id`. A URL de exibição é montada na hora de responder, pedindo ao Cloudinary uma versão de 256×256 recortada no rosto.

Validação no servidor: no máximo 2 MB, e só JPG, PNG ou WebP.

### Tratamento de erros

| Situação | Resposta |
|---|---|
| Login incorreto | `400` — "E-mail/usuário ou senha incorretos" |
| E-mail já cadastrado | `400` — "Este e-mail já está cadastrado" |
| Senha fora do padrão | `400` — mensagem da validação, por campo |
| Código de recuperação inválido | `400` — "Código inválido ou expirado" |
| Requisição sem autenticação em rota protegida | `401` — "Autenticação necessária" |

O `GlobalExceptionHandler` centraliza a conversão de exceções em JSON. No frontend, o `apiRequest` lê o corpo da resposta de erro e exibe a mensagem do backend na tela.

---

## Decisões técnicas

**Mensagem de erro genérica no login.** Usuário inexistente, senha errada e conta sem senha retornam a mesma mensagem. Mensagens específicas permitiriam descobrir quais e-mails têm conta no Cafélio testando um a um. A perda de clareza é intencional.

**O mesmo vale para a recuperação de senha.** Pedir recuperação para um e-mail não cadastrado responde `202` e não envia nada — da tela, é indistinguível de um pedido bem-sucedido. *Efeito colateral a conhecer:* quando alguém relatar "pedi e não chegou", a primeira hipótese deve ser que o e-mail não tem conta, e não que o envio falhou.

**Falha no envio do e-mail também é silenciosa.** O `requestReset` captura a exceção do `EmailService`. Propagar o erro revelaria que o e-mail existe, já que para e-mail inexistente nada é enviado. O preço é que a falha só aparece no log do servidor, nunca na tela.

**O e-mail é obrigatório junto com o código de 6 dígitos.** Sem ele, seria possível tentar as combinações contra todas as contas ao mesmo tempo; com um milhão de códigos possíveis e muitos pedidos pendentes, alguma casaria. Exigir o e-mail reduz a tentativa a uma conta por vez — e é o que torna o limite de 5 tentativas eficaz.

**O contador de tentativas usa `@Transactional(noRollbackFor = IllegalArgumentException.class)`.** Sem isso, o rollback desfaria o incremento junto com a exceção, e a proteção não existiria na prática. É uma sutileza que não aparece em teste de caminho feliz — por isso há um teste dedicado a ela.

**Sem `UNIQUE` em `code_hash`.** Com apenas 6 dígitos, duas pessoas podem receber o mesmo código legitimamente.

**A foto guarda o `public_id`, não a URL.** O nome da conta do Cloudinary aparece em toda URL de imagem. Gravado dentro de cada registro, trocar de conta um dia quebraria todas as fotos. Guardando o identificador, o nome da conta vive na configuração.

**A troca de foto segue a ordem: sobe a nova → salva no banco → apaga a antiga.** Invertida, uma falha no upload deixaria a pessoa sem foto nenhuma, tendo perdido a que tinha. O pior caso na ordem atual é uma imagem órfã no Cloudinary.

**Validação de imagem no servidor, não só no `accept` do input.** O atributo HTML é conveniência e se burla facilmente. Como os créditos do Cloudinary são pagos, a validação precisa estar no backend.

**Sessão `STATELESS`.** Com JWT, cada requisição carrega a própria identidade.

**401 em vez do 403 padrão.** O padrão do Spring Security para requisição não autenticada é 403 ("proibido"), que significa "sei quem você é, mas você não pode". O correto aqui é 401 ("não autenticado"). Configurado via `authenticationEntryPoint`.

**`.cors(Customizer.withDefaults())` no `SecurityConfig`.** O `CorsConfig` é um `WebMvcConfigurer`, que roda *depois* do Spring Security. Sem essa linha, a requisição `OPTIONS` de preflight era rejeitada com 403 em toda rota protegida. Há um teste de regressão para isso.

---

## Cobertura de testes

**59 testes**, todos passando.

| Classe | Testes |
|---|---|
| `AuthServiceTest` | 13 |
| `AuthControllerTest` | 13 |
| `PasswordResetServiceTest` | 12 |
| `LibraryBookServiceTest` | 8 |
| `ProfileServiceTest` | 5 |
| `JwtAuthenticationFilterTest` | 4 |
| Demais (contexto, health, OpenAPI, busca) | 4 |

Os testes de serviço usam Mockito puro, sem subir o Spring. Os de controller usam `@SpringBootTest` com `@MockitoBean` nas dependências externas.

Dois detalhes que valem registro:

**O MockMvc não passa pela corrente do Spring Security por padrão.** Sem aplicar `springSecurity()` na construção, testes de 401 passariam sem que a segurança estivesse ativa — dando falsa confiança.

**O `ProfileServiceTest` usa `InOrder`.** Ele verifica não só *se* os métodos foram chamados, mas em qual ordem. É o que protege a regra de salvar antes de apagar: um `verify` comum passaria mesmo com a ordem invertida.

---

## Notas de infraestrutura

### Banco compartilhado

As duas desenvolvedoras usam a mesma instância do Supabase, com duas consequências práticas.

*Migrations afetam todo mundo na hora.* Uma migration aplicada a partir de qualquer branch muda o banco de ambas, mesmo antes de ser mesclada. Quem ainda está com a `develop` antiga passa a falhar na partida com erro de validação de schema.

**E rodar a suíte de testes conta como aplicar.** Os testes com `@SpringBootTest` sobem o contexto completo, e o Flyway roda junto. Isso já derrubou o ambiente da outra desenvolvedora uma vez. Regra prática: antes de rodar testes com migration nova pendente, avise — ou mescle primeiro.

*Migration aplicada não se edita.* O Flyway guarda uma assinatura de cada arquivo executado. Alterar um já aplicado faz a aplicação falhar para todas. Correções vêm sempre em migration nova.

### Limite de conexões

O plano gratuito do Supabase limita sessões simultâneas. Manter a aplicação rodando enquanto se executa a suíte pode estourar o limite, e a falha aparece como erro de contexto do Spring, parecendo problema de código. Regra prática: parar a aplicação antes de `./mvnw test`.

### Configuração: três arquivos, um contrato

| Arquivo | Papel | Vai para o Git? |
|---|---|---|
| `application.yaml` | O formato, com `${VARIAVEL}` | sim |
| `.env` | Os valores, lidos pelo **Docker** | não — há um `.env.example` |
| `application-local.yaml` | Os valores, lidos pelo **Spring** | não |

Quem roda pelo Docker (`compose.yaml`) preenche o `.env`. Quem roda direto pelo Maven preenche o `application-local.yaml`, porque **o Spring não lê arquivos `.env`** — essa leitura é do Docker.

Os dois caminhos são válidos e convivem. Importante saber: **se ambos existirem, o `application-local.yaml` vence**, por ser configuração de perfil. Um valor alterado no `.env` que parece não ter efeito costuma ser isso.

O `compose.yaml` publica a porta `8080:8000` — a aplicação roda na 8000 dentro do contêiner e é exposta na 8080. O frontend acomoda essa diferença com o `config.local.js`, individual de cada desenvolvedora.

### Migrations aplicadas nesta etapa

| Versão | O que faz |
|---|---|
| `V3` | Cria `password_reset_tokens` |
| `V4` | Acrescenta `code_hash` e `attempts` |
| `V5` | Renomeia `profile_picture_url` para `profile_picture_public_id` |

---

## Dívidas registradas

Itens conhecidos, deixados para depois de propósito.

**1. Propriedades customizadas não declaradas.** `cafelio`, `jwt` e `cloudinary` são lidas por `@Value` e não possuem classes `@ConfigurationProperties`. O VS Code marca cada uma como "Unknown property". Declarar daria validação de configuração na partida, autocompletar e tipo conferido.

**2. O ID do cliente Google usa namespace do Spring.** A propriedade `spring.security.oauth2.client.registration.google.client-id` *parece* do Spring, mas o Spring não a lê — quem lê é o `GoogleTokenVerifier`, via `@Value`. O nome próprio seria `cafelio.google.client-id`. A renomeação exige acerto prévio entre as duas, porque quebra o ambiente local de quem não ajustar.

**3. Gmail não serve para produção.** O envio atual usa SMTP do Gmail com senha de app. Há teto diário de envio, e e-mail transacional saindo de um domínio que não é o seu cai em spam com frequência. Antes de monetizar, isso precisa virar um serviço de e-mail transacional com domínio próprio e autenticação (SPF/DKIM/DMARC). O `EmailService` já isola essa troca em uma classe.

**4. Falta documentação de setup no README.** Hoje não há texto dizendo "copie o `.env.example` ou crie o `application-local.yaml`". Quem clonar o projeto descobre tentando.
