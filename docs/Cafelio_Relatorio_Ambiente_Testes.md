# Mini relatório — correções para destravar a `develop` e executar os testes

## Contexto inicial

A `develop` estava quebrada e a aplicação não subia porque faltava a dependência:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId>
</dependency>
```

O problema acontecia porque o `OpenLibraryClient` recebe um `RestClient.Builder` no construtor, mas esse bean não estava sendo criado com as dependências que estavam no projeto.

Depois de adicionar o `spring-boot-starter-restclient` ao `pom.xml`, foi executado:

```bash
./mvnw test
```

A partir daí, começaram a aparecer outros erros que não estavam mais relacionados diretamente à dependência do `RestClient`, mas sim ao ambiente local usado para executar a suíte de testes.

---

## 1. Incompatibilidade de versão do Java

O projeto está configurado para usar **Java 25**, mas o Maven executado pelo terminal estava usando **Java 21**.

O erro apresentado era semelhante a:

```text
class file version 69.0
this version of the Java Runtime only recognizes class file versions up to 65.0
```

Ou seja:

- `69.0` = classes compiladas com Java 25;
- `65.0` = runtime Java 21.

No IntelliJ o projeto já estava configurado com JDK 25, por isso a aplicação conseguia rodar por lá, mas o terminal ainda utilizava outra versão.

Para evitar depender da configuração global de Java de cada máquina, decidimos usar Docker para executar o backend com um ambiente padronizado em Java 25.

---

## 2. Variáveis de ambiente

Depois que o Java foi corrigido, os testes começaram a falhar ao carregar o `ApplicationContext` do Spring.

O motivo era que várias configurações usadas pela aplicação estavam cadastradas apenas na Run Configuration do IntelliJ, como:

- dados de conexão com o Supabase/PostgreSQL;
- configuração do JWT;
- configuração do Google Auth.

Quando os testes eram executados pelo terminal ou pelo Docker, essas variáveis não existiam.

Foi criado um arquivo:

```text
.env
```

com as variáveis necessárias para o ambiente local.

O `compose.yaml` foi configurado para carregar esse arquivo automaticamente.

Também adicionamos o `.env` ao `.gitignore` para impedir que credenciais e segredos sejam enviados ao repositório:

```gitignore
.env
.env.*
!.env.example
```

A ideia é manter somente um `.env.example` versionado, contendo os nomes das variáveis, mas sem valores reais.

---

## 3. Docker para padronizar o ambiente

Foi configurado um serviço `backend` no `compose.yaml` usando Java 25.

Com isso, os testes deixam de depender da versão de Java configurada diretamente no Linux de cada pessoa.

Exemplo de execução:

```bash
docker compose run --rm backend ./mvnw test
```

Esse comando:

- cria um container temporário para o backend;
- utiliza o Java 25 definido no ambiente Docker;
- carrega as variáveis do `.env`;
- executa a suíte Maven;
- remove o container ao terminar.

---

## 4. Erro do Mockito/Byte Buddy no Java 25

Depois que Java e variáveis de ambiente foram corrigidos, os testes chegaram a executar mais longe, mas alguns falharam com erros do Mockito, como:

```text
Could not initialize inline Byte Buddy mock maker
```

e:

```text
AttachNotSupportedException
```

O Mockito estava tentando anexar dinamicamente o Byte Buddy à JVM durante os testes, e esse mecanismo estava falhando no ambiente Java 25 dentro do container.

Para resolver de forma estável, configuramos o Mockito como Java Agent diretamente pelo Maven.

No bloco `<properties>` do `pom.xml` ficou:

```xml
<properties>
    <java.version>25</java.version>
    <argLine></argLine>
</properties>
```

E foram adicionados os plugins:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>@{argLine} -javaagent:${org.mockito:mockito-core:jar}</argLine>
    </configuration>
</plugin>
```

Com isso, o Mockito passa a carregar o agente explicitamente durante a execução dos testes, sem depender do mecanismo de self-attach da JVM.

---

## Resultado final

A sequência completa foi:

1. A `develop` não subia porque faltava `spring-boot-starter-restclient`.
2. A dependência foi adicionada ao `pom.xml`.
3. Ao executar `./mvnw test`, apareceu incompatibilidade entre Java 25 do projeto e Java 21 do terminal.
4. Passamos a executar o backend/testes em Docker com Java 25.
5. Os testes passaram a encontrar problemas de variáveis de ambiente que existiam apenas no IntelliJ.
6. Criamos um `.env` carregado pelo Docker e protegido pelo `.gitignore`.
7. O Mockito/Byte Buddy apresentou problema de attach no Java 25.
8. O `pom.xml` foi ajustado para carregar o Mockito como `javaagent`.
9. A suíte passou a executar corretamente no ambiente Docker.

O comando final para validar o backend ficou:

```bash
docker compose run --rm backend ./mvnw clean test
```

## Arquivos alterados/configurados

- `pom.xml`
  - adição de `spring-boot-starter-restclient`;
  - configuração do `maven-dependency-plugin`;
  - configuração do `maven-surefire-plugin`;
  - uso do Mockito como `javaagent`.

- `compose.yaml`
  - ambiente padronizado com Java 25;
  - carregamento das variáveis do `.env`.

- `.env`
  - variáveis locais necessárias para o backend.

- `.gitignore`
  - proteção do `.env` para evitar commit de segredos.

✅ Resultado: a `develop` foi destravada e os testes passaram a rodar em um ambiente mais reproduzível e consistente entre as máquinas.
