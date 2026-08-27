# Guia de Git e GitHub — Cafélio

Este documento define como vamos trabalhar com **Git e GitHub** no projeto **Cafélio** usando um **Git Flow simplificado**.

A ideia é manter o projeto organizado sem criar burocracia desnecessária, já que o desenvolvimento é feito por uma equipe pequena.

---

## 1. Estrutura de branches

Vamos trabalhar principalmente com três tipos de branch:

```text
main
  ↑
develop
  ↑
feature/*
```

### `main`

A branch `main` representa a versão mais estável do projeto.

Regras:

- Não desenvolver diretamente nela.
- Não fazer commits comuns diretamente nela.
- Receber alterações por Pull Request.
- Usar para versões consideradas estáveis.

Exemplo:

```text
develop → Pull Request → main
```

---

### `develop`

A branch `develop` reúne as funcionalidades que já foram concluídas e revisadas, mas que ainda fazem parte do desenvolvimento atual.

Regras:

- Não desenvolver diretamente nela.
- Cada nova tarefa deve começar em uma branch própria.
- Features concluídas entram em `develop` por Pull Request.

Exemplo:

```text
feature/login
       ↓
Pull Request
       ↓
develop
```

---

### `feature/*`

Cada funcionalidade ou tarefa deve possuir uma branch própria.

Exemplos:

```text
feature/register-tests
feature/login
feature/jwt-authentication
feature/book-search
feature/user-profile
```

A branch deve ser criada a partir da `develop`.

```bash
git switch develop
git pull origin develop
git switch -c feature/nome-da-feature
```

---

## 2. Outros tipos de branch

Além de `feature/*`, podemos usar:

### Correção de bug

```text
fix/nome-do-problema
```

Exemplos:

```text
fix/register-validation
fix/cors-error
fix/token-expiration
```

### Refatoração

```text
refactor/nome-da-refatoracao
```

Exemplos:

```text
refactor/auth-service
refactor/user-validation
```

### Documentação

```text
docs/nome-da-alteracao
```

Exemplos:

```text
docs/update-readme
docs/git-workflow
```

---

## 3. Fluxo de trabalho diário

Antes de começar uma nova tarefa:

```bash
git switch develop
git pull origin develop
```

Depois crie sua branch:

```bash
git switch -c feature/nome-da-feature
```

Exemplo:

```bash
git switch -c feature/register-tests
```

Agora você pode desenvolver normalmente.

---

## 4. Conferindo suas alterações

Durante o desenvolvimento:

```bash
git status
```

Para visualizar alterações:

```bash
git diff
```

Antes do commit, confira sempre quais arquivos serão enviados.

---

## 5. Adicionando arquivos

Adicionar um arquivo específico:

```bash
git add caminho/do/arquivo
```

Exemplo:

```bash
git add src/test/java/com/cafelio/api/AuthControllerTests.java
```

Adicionar todas as alterações:

```bash
git add .
```

Use `git add .` com atenção para evitar enviar arquivos que não deveriam estar no repositório.

---

## 6. Commits

Os commits devem ser pequenos e representar uma alteração coerente.

Evitar commits como:

```text
arrumei coisa
mudanças
teste
agora vai
final
```

Preferir mensagens que expliquem o que foi feito.

Exemplos:

```text
feat: add user login endpoint
test: add registration validation tests
fix: handle duplicated email on registration
refactor: simplify authentication service
docs: add git workflow guide
```

### Prefixos recomendados

| Prefixo | Uso |
|---|---|
| `feat:` | Nova funcionalidade |
| `fix:` | Correção de bug |
| `test:` | Criação ou alteração de testes |
| `refactor:` | Refatoração sem mudar comportamento |
| `docs:` | Documentação |
| `chore:` | Configurações e tarefas auxiliares |

Exemplo:

```bash
git commit -m "test: add registration validation tests"
```

---

## 7. Enviando a branch para o GitHub

Na primeira vez que enviar uma branch:

```bash
git push -u origin feature/nome-da-feature
```

Exemplo:

```bash
git push -u origin feature/register-tests
```

Depois disso, normalmente basta:

```bash
git push
```

---

## 8. Pull Request

Quando a tarefa estiver pronta:

```text
feature/*
    ↓
Pull Request
    ↓
develop
```

No GitHub:

1. Abra o repositório.
2. Vá em **Pull Requests**.
3. Clique em **New Pull Request**.
4. Configure:

```text
base: develop
compare: feature/sua-feature
```

5. Escreva um título claro.
6. Explique resumidamente o que foi feito.
7. Peça revisão da outra desenvolvedora.

Exemplo de título:

```text
Add registration validation tests
```

Exemplo de descrição:

```text
## O que foi feito

- Adicionados testes de cadastro válido
- Adicionado teste de e-mail inválido
- Adicionado teste de senha fraca
- Adicionado teste de username duplicado

## Como testar

Executar:

./mvnw test
```

---

## 9. Code Review

Antes de fazer o merge, a outra desenvolvedora deve revisar o Pull Request quando possível.

Na revisão, observar:

- O código está fácil de entender?
- Existem nomes claros?
- Há código duplicado?
- A alteração realmente resolve a tarefa?
- Os testes passam?
- Algum segredo ou senha foi enviado?
- Existem mudanças não relacionadas à tarefa?

Se forem necessárias alterações, elas devem ser feitas na mesma branch.

Depois:

```bash
git add .
git commit -m "fix: address pull request review"
git push
```

O Pull Request será atualizado automaticamente.

---

## 10. Merge

Depois da aprovação:

```text
feature/*
    ↓
develop
```

O merge deve ser feito pelo GitHub.

Após o merge, a branch da feature pode ser excluída.

Isso evita acumular dezenas de branches antigas.

---

## 11. Atualizando sua `develop`

Depois que um Pull Request for integrado:

```bash
git switch develop
git pull origin develop
```

Agora sua `develop` local estará atualizada.

---

## 12. Começando outra feature

Sempre parta da `develop` atualizada:

```bash
git switch develop
git pull origin develop
git switch -c feature/nova-feature
```

Não crie uma nova feature a partir de outra feature antiga.

Evitar:

```text
feature/login
     ↓
feature/book-search
```

Preferir:

```text
develop
 ├── feature/login
 └── feature/book-search
```

---

## 13. Mantendo sua feature atualizada

Se enquanto você trabalha outra feature for integrada em `develop`, sua branch pode ficar desatualizada.

Atualize primeiro a `develop`:

```bash
git switch develop
git pull origin develop
```

Volte para sua branch:

```bash
git switch feature/sua-feature
```

Depois traga as alterações:

```bash
git merge develop
```

Se não houver conflitos, continue normalmente.

---

## 14. Conflitos de merge

Um conflito acontece quando Git não consegue decidir sozinho qual alteração deve permanecer.

Exemplo:

```text
<<<<<<< HEAD
seu código
=======
código vindo da develop
>>>>>>> develop
```

Não apague aleatoriamente.

As duas desenvolvedoras devem entender as duas alterações e decidir qual código deve permanecer.

Depois de resolver:

```bash
git add .
git commit
```

---

## 15. O que nunca deve ir para o GitHub

Não enviar:

```text
.env
senhas
tokens
JWT secrets
credenciais do Supabase
application-local.yaml
chaves privadas
arquivos de configuração pessoais da IDE
```

Antes de fazer commit:

```bash
git status
```

Sempre confira a lista.

Se aparecer alguma credencial ou arquivo sensível, não faça commit.

---

## 16. Fluxo completo de uma feature

Exemplo: implementar login.

### Atualizar `develop`

```bash
git switch develop
git pull origin develop
```

### Criar branch

```bash
git switch -c feature/login
```

### Desenvolver

Alterar os arquivos necessários.

### Conferir

```bash
git status
git diff
```

### Commit

```bash
git add .
git commit -m "feat: add user login"
```

### Enviar

```bash
git push -u origin feature/login
```

### GitHub

Criar:

```text
feature/login → develop
```

### Revisão

A outra desenvolvedora revisa.

### Merge

Depois da aprovação, fazer merge para `develop`.

### Atualizar localmente

```bash
git switch develop
git pull origin develop
```

---

## 17. Quando usar `main`

Durante o desenvolvimento normal:

```text
feature/* → develop
```

Quando houver uma versão considerada estável:

```text
develop → main
```

Esse merge também deve ser feito por Pull Request.

Exemplo:

```text
develop
   ↓
Pull Request
   ↓
main
```

---

## 18. Resumo visual

```text
                    feature/register-tests
                           ↓
                           ↓ Pull Request
                           ▼
main ←──────────────── develop
                           ▲
                           ↑ Pull Request
                           ↑
                       feature/login
```

Fluxo normal:

```text
develop
   ↓
criar branch
   ↓
feature/*
   ↓
commits
   ↓
push
   ↓
Pull Request
   ↓
code review
   ↓
merge
   ↓
develop
```

Quando uma versão estiver estável:

```text
develop
   ↓
Pull Request
   ↓
main
```

---

## 19. Regras do Cafélio

Para manter o projeto organizado, vamos seguir estas regras:

1. Não desenvolver diretamente em `main`.
2. Não desenvolver diretamente em `develop`.
3. Uma tarefa = uma branch.
4. Branches devem ter nomes claros.
5. Commits devem explicar o que foi alterado.
6. Features entram em `develop` por Pull Request.
7. Sempre que possível, uma desenvolvedora revisa o PR da outra.
8. Antes de criar uma branch, atualizar a `develop`.
9. Nunca enviar credenciais ou segredos para o GitHub.
10. `main` deve representar uma versão estável do Cafélio.

---

## Estrutura adotada

```text
main
└── develop
    ├── feature/*
    ├── fix/*
    ├── refactor/*
    └── docs/*
```

Esse fluxo mantém o projeto simples, organizado e próximo das práticas utilizadas em equipes de desenvolvimento, sem a complexidade do Git Flow tradicional.
