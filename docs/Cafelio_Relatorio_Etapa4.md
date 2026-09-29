# Relatório de desenvolvimento — Etapa 4 (Biblioteca pessoal)

**Data:** 28/09/2026  
**Responsável:** Amanda  
**Branch de trabalho:** `feature/library-crud`

---

## Resumo

O foco da Etapa 4 foi a implementação da **Biblioteca Pessoal do Cafélio**, permitindo que cada usuário mantenha sua própria coleção de livros, acompanhe o status de leitura, organize livros por tags, consulte e remova itens da biblioteca e utilize a funcionalidade de **Meta Literária**, com sorteio aleatório de um livro.

A funcionalidade foi integrada ao sistema de autenticação JWT já existente. Todas as operações da biblioteca consideram o usuário autenticado, evitando que uma conta consulte, altere ou exclua diretamente os livros pertencentes a outra.

Durante a etapa foram implementados:

- estrutura da entidade `LibraryBook`;
- migration da biblioteca;
- adição de livros;
- listagem da biblioteca;
- busca individual por ID;
- atualização parcial de status e tags;
- exclusão de livros;
- prevenção de duplicidade;
- tratamento de livros inexistentes;
- isolamento das consultas por usuário;
- integração do JWT com o Swagger;
- sorteio de livros da Meta Literária;
- tratamento do caso em que a Meta Literária está vazia;
- testes manuais dos principais fluxos pelo Swagger e conferência no Supabase.

---

## 1. Modelagem da Biblioteca Pessoal

### Backend

Foi criada a entidade `LibraryBook`, responsável por representar um livro salvo na biblioteca do usuário.

| Campo | Função |
|---|---|
| `id` | UUID gerado para identificar o registro dentro do Cafélio |
| `user` | Usuário proprietário do livro |
| `openLibraryId` | Identificador da obra na Open Library |
| `title` | Título do livro |
| `firstPublishYear` | Ano da primeira publicação |
| `coverUrl` | URL da capa |
| `status` | Status de leitura |
| `authors` | Lista de autores |
| `tags` | Conjunto de tags associadas ao livro |

Foi criada uma restrição de unicidade composta por:

```text
user_id + open_library_id
```

Assim, dois usuários diferentes podem adicionar a mesma obra, mas **a mesmo usuário não pode adicionar o mesmo livro duas vezes**.

### Status de leitura

O enum `ReadingStatus` representa a situação atual do livro dentro da biblioteca:

- `READING`
- `READ`
- `WANT_TO_READ`
- `READING_GOAL`
- `REREADING`
- `ABANDONED`

O status `READING_GOAL` é utilizado para identificar os livros que participam do sorteio da Meta Literária.

### Tags

O enum `BookTag` permite adicionar informações complementares ao livro:

- `FAVORITE`
- `WANTED`
- `OWNED`
- `LENT`

As tags são armazenadas em `Set<BookTag>`, evitando valores duplicados no mesmo livro.

---

## 2. Banco de dados

Foi criada a migration:

```text
V2__create_library_books.sql
```

A migration adicionou as estruturas necessárias para persistir a biblioteca:

- `library_books`;
- `library_book_authors`;
- `library_book_tags`.

Também foram configurados:

- relacionamento entre `LibraryBook` e `User`;
- foreign keys;
- restrição de unicidade entre `user_id` e `open_library_id`;
- remoção em cascata das coleções associadas ao livro.

A migration foi executada e validada no PostgreSQL hospedado no Supabase.

---

## 3. Adição de livros à biblioteca

Foi implementado:

```http
POST /library-books
```

O endpoint recebe um `LibraryBookCreateRequest`, identifica o usuário autenticado e salva o livro em sua biblioteca.

Antes da persistência, o backend verifica:

```text
existsByUserAndOpenLibraryId(...)
```

Caso aquele usuário já tenha o mesmo livro, é lançada:

```text
LibraryBookAlreadyExistsException
```

O `GlobalExceptionHandler` converte a exceção para:

```http
409 Conflict
```

Exemplo de resposta:

```json
{
  "error": "Livro já adicionado na biblioteca"
}
```

A regra foi validada manualmente no Swagger ao tentar cadastrar novamente um livro já existente.

---

## 4. Listagem da biblioteca

Foi implementado:

```http
GET /library-books
```

O endpoint retorna somente os livros pertencentes ao usuário autenticado.

O repository utiliza:

```text
findByUser(User user)
```

A própria consulta ao banco já é limitada à usuário proprietário dos registros.

Cada entidade encontrada é convertida para `LibraryBookResponse` antes de ser enviada ao cliente.

---

## 5. Busca de livro por ID

Foi implementado:

```http
GET /library-books/{id}
```

O UUID do livro é recebido através de `@PathVariable`.

A consulta utiliza:

```text
findByIdAndUser(UUID id, User user)
```

A escolha garante que não basta o registro existir: **ele também precisa pertencer ao usuário autenticada**.

Caso o livro não seja encontrado, é lançada:

```text
LibraryBookNotFoundException
```

O `GlobalExceptionHandler` converte essa exceção para:

```http
404 Not Found
```

Exemplo:

```json
{
  "error": "Livro não encontrado na biblioteca"
}
```

Foram testados manualmente os dois cenários:

- UUID existente → `200 OK`;
- UUID válido, porém inexistente → `404 Not Found`.

Também foi observado que um UUID com formato inválido é rejeitado antes de chegar à regra de negócio.

---

## 6. Atualização de status e tags

A atualização foi inicialmente implementada com:

```http
PUT /library-books/{id}
```

Durante a revisão da funcionalidade, foi identificado que somente alguns campos do recurso são modificáveis:

- `status`;
- `tags`.

Dados originados da Open Library, como título, autores, ano e identificador externo, permanecem inalterados.

Como a alteração é parcial, o endpoint foi ajustado para:

```http
PATCH /library-books/{id}
```

O DTO utilizado é:

```text
LibraryBookUpdateRequest
```

com os campos:

```text
ReadingStatus status
Set<BookTag> tags
```

No service, cada campo é atualizado somente quando foi enviado:

```text
status != null → atualiza status
tags != null   → atualiza tags
```

Assim, é possível alterar apenas o status sem apagar as tags existentes, ou alterar somente as tags sem modificar o status.

Foram realizados testes separados no Swagger:

- alteração somente do status;
- alteração somente das tags.

Nos dois casos, o campo não enviado permaneceu inalterado.

As alterações também foram conferidas diretamente no Supabase.

---

## 7. Exclusão de livros

Foi implementado:

```http
DELETE /library-books/{id}
```

Antes da exclusão, o backend consulta:

```text
findByIdAndUser(...)
```

garantindo que a usuário só possa remover livros pertencentes à própria biblioteca.

O service executa a exclusão através do método `delete()` herdado do `JpaRepository`.

Após uma exclusão bem-sucedida, o controller retorna:

```http
200 OK
```

com a mensagem:

```text
Livro deletado com sucesso
```

O fluxo foi validado de três formas:

1. resposta `200 OK` no Swagger;
2. o livro deixou de aparecer em `GET /library-books`;
3. o registro deixou de existir no Supabase.

---

## 8. Autenticação e isolamento por usuário

Os endpoints da biblioteca utilizam o usuário autenticado presente no `Authentication`.

O `JwtAuthenticationFilter` coloca o UUID do usuário no principal da autenticação.

Nos controllers, o UUID é recuperado através de:

```text
authentication.getPrincipal()
```

e enviado ao service como `userId`.

No service, é utilizada a busca:

```text
authService.findById(userId)
```

para recuperar a entidade `User`.

A propriedade dos dados é garantida por consultas como:

```text
findByUser(...)
findByIdAndUser(...)
existsByUserAndOpenLibraryId(...)
findByUserAndStatus(...)
```

Com isso, as operações da biblioteca sempre consideram o usuário autenticado.

Durante o desenvolvimento, a leitura do `SecurityContext` foi retirada do `LibraryBookService` e mantida no controller, deixando o service focado nas regras de negócio.

---

## 9. Integração do JWT com o Swagger

Foi configurado o esquema Bearer JWT no OpenAPI.

O Swagger passou a disponibilizar o botão:

```text
Authorize
```

permitindo inserir o token gerado no login e testar os endpoints protegidos diretamente pela interface.

Os endpoints da biblioteca utilizam:

```text
@SecurityRequirement(name = "bearerAuth")
```

Essa configuração serve para documentar a necessidade do token no Swagger.

A autenticação real continua sendo realizada pelo Spring Security e pelo `JwtAuthenticationFilter`.

---

## 10. Sorteio da Meta Literária

Foi implementado:

```http
GET /library-books/reading-goal
```

O endpoint não recebe ID de livro, pois sua responsabilidade é selecionar automaticamente uma obra da Meta Literária do usuário.

Foi criado no repository:

```text
findByUserAndStatus(User user, ReadingStatus status)
```

O método retorna somente os livros:

- pertencentes ao usuário autenticada;
- com status `READING_GOAL`.

No service, o fluxo é:

1. buscar o usuário autenticado;
2. buscar seus livros com status `READING_GOAL`;
3. verificar se a lista está vazia;
4. gerar um índice aleatório utilizando `Random.nextInt(books.size())`;
5. obter o livro correspondente através de `books.get(indice)`;
6. converter o livro sorteado para `LibraryBookResponse`;
7. retornar o resultado.

A utilização de `nextInt(books.size())` garante que o índice gerado esteja sempre dentro das posições válidas da lista.

### Meta Literária vazia

Foi criada:

```text
NoReadingGoalBooksException
```

Caso a usuário não possua nenhum livro com status `READING_GOAL`, a exceção é lançada com uma mensagem informando que não há livros na Meta Literária.

O `GlobalExceptionHandler` converte essa situação para:

```http
404 Not Found
```

Exemplo:

```json
{
  "error": "Nenhum livro encontrado na meta literária"
}
```

---

## 11. Testes manuais realizados

Os fluxos da biblioteca foram testados através do Swagger com autenticação JWT ativa.

### Adição

- cadastro de livro novo;
- tentativa de cadastrar o mesmo livro novamente;
- retorno `409 Conflict` confirmado para duplicidade.

### Listagem

- consulta da biblioteca do usuário;
- confirmação dos registros persistidos no Supabase.

### Busca por ID

- busca com UUID existente;
- busca com UUID válido, porém inexistente;
- retorno `404 Not Found` confirmado.

### Atualização parcial

- alteração somente de `status`;
- alteração somente de `tags`;
- confirmação de que o campo não enviado permaneceu inalterado;
- conferência dos valores no Supabase.

### Exclusão

- exclusão pelo UUID;
- retorno `200 OK`;
- nova listagem para confirmar a remoção;
- conferência no Supabase.

### Meta Literária

Primeiro foi testado o cenário sem nenhum livro em `READING_GOAL`:

```text
404 Not Found
Nenhum livro encontrado na meta literária
```

Depois foram cadastrados dois livros para o teste:

- **Dom Casmurro**, de Machado de Assis;
- **A Hora da Estrela**, de Clarice Lispector.

Ambos foram adicionados com status:

```text
READING_GOAL
```

O endpoint de sorteio foi executado repetidas vezes e retornou livros diferentes entre as opções disponíveis, confirmando o funcionamento da seleção aleatória.

---

## 12. Tratamento de erros

Foram adicionados tratamentos específicos para regras da biblioteca.

| Exceção | Situação | HTTP |
|---|---|---|
| `LibraryBookAlreadyExistsException` | Livro já existe na biblioteca do usuário | `409 Conflict` |
| `LibraryBookNotFoundException` | Livro não existe ou não pertence ao usuário | `404 Not Found` |
| `NoReadingGoalBooksException` | Meta Literária não possui livros para sorteio | `404 Not Found` |

As exceções são centralizadas no `GlobalExceptionHandler`, mantendo os controllers e services focados em suas responsabilidades.

---

## 13. Decisões técnicas tomadas

**UUID como identificador interno.** Cada `LibraryBook` possui um UUID próprio no Cafélio, independente do `openLibraryId`. O identificador interno é utilizado nas operações de consulta, edição e exclusão.

**Usuário final não precisa manipular UUID manualmente.** O UUID aparece no Swagger porque a API está sendo testada diretamente. No frontend, ele será utilizado internamente quando a usuário clicar em ações como editar ou excluir.

**Consultas por ID também filtram pelo usuário.** Foi utilizado `findByIdAndUser` em vez de apenas `findById`. Isso impede que conhecer o UUID de um livro seja suficiente para acessar ou modificar um registro de outra conta.

**PATCH em vez de PUT para status e tags.** Como somente partes específicas do recurso são alteradas e cada campo pode ser enviado separadamente, `PATCH` representa melhor o comportamento implementado.

**Dados da Open Library não são editáveis pela biblioteca.** Título, autores, ano, capa e identificador externo são tratados como dados da obra selecionada. As informações controladas pelo usuário são principalmente status e tags.

**`Set<BookTag>` para etiquetas.** A escolha evita duplicação de tags no mesmo livro.

**Sorteio limitado a `READING_GOAL`.** Livros com `WANT_TO_READ` representam apenas intenção futura. Somente livros explicitamente colocados na Meta Literária participam do sorteio.

**Repository responsável pelo recorte de dados.** As buscas são feitas diretamente por usuário e, quando necessário, por status, evitando carregar registros de outras contas para depois filtrá-los em memória.

---

## 14. Situação da Etapa 4

| Recurso do escopo | Estado |
|---|---|
| Estrutura `LibraryBook` | Concluído |
| Migration da biblioteca | Concluído |
| Adicionar livro | Concluído |
| Impedir duplicidade por usuário | Concluído |
| Listar biblioteca | Concluído |
| Buscar livro por ID | Concluído |
| Atualizar status | Concluído |
| Atualizar tags | Concluído |
| Atualização parcial com PATCH | Concluído |
| Excluir livro | Concluído |
| Isolamento dos livros por usuário | Concluído na implementação |
| Tratamento de livro inexistente | Concluído |
| Sorteio da Meta Literária | Concluído |
| Tratamento de Meta Literária vazia | Concluído |
| Testes manuais via Swagger | Concluído |
| Testes automatizados específicos da Etapa 4 | **Pendente** |

---

## 15. Próximos passos

A implementação funcional da Biblioteca Pessoal está concluída.

O próximo passo recomendado é adicionar **testes automatizados específicos da Etapa 4**, principalmente para as regras de segurança e negócio.

Prioridades:

1. garantir por teste que a usuário A não consegue acessar um livro pertencente ao usuário B;
2. garantir que adicionar o mesmo livro duas vezes pela mesmo usuário retorna `409 Conflict`;
3. testar `GET /library-books/{id}`;
4. testar atualização parcial com `PATCH`;
5. testar exclusão;
6. testar sorteio da Meta Literária;
7. testar o comportamento quando a Meta Literária estiver vazia.

Os dois primeiros são especialmente importantes por cobrirem as principais regras de isolamento e integridade da biblioteca.

---

## Endpoints entregues na Etapa 4

```text
POST    /library-books
GET     /library-books
GET     /library-books/{id}
PATCH   /library-books/{id}
DELETE  /library-books/{id}
GET     /library-books/reading-goal
```

Todos os endpoints da biblioteca dependem de autenticação JWT.

---

## Principais arquivos envolvidos

```text
model/LibraryBook.java
model/ReadingStatus.java
model/BookTag.java

dto/request/LibraryBookCreateRequest.java
dto/request/LibraryBookUpdateRequest.java
dto/response/LibraryBookResponse.java

repository/LibraryBookRepository.java

service/LibraryBookService.java

controller/LibraryBookController.java

exception/LibraryBookAlreadyExistsException.java
exception/LibraryBookNotFoundException.java
exception/NoReadingGoalBooksException.java
exception/GlobalExceptionHandler.java

db/migration/V2__create_library_books.sql
```

---

## Commits principais da etapa

Entre os commits realizados durante o desenvolvimento estão alterações relacionadas a:

```text
feat: adiciona listagem de livros e tratamento de duplicidade
refactor: move autenticação para o controller
feat: atualiza status e tags do livro
feat: cria endpoint e trata exceção na busca de livro por id
feat: cria endpoint para deletar livro por id
refactor: altera atualização de livro de PUT para PATCH
feat: implementa sorteio de livro da meta literária
```

Os hashes não foram incluídos neste relatório porque não foram levantados durante a elaboração do documento.
