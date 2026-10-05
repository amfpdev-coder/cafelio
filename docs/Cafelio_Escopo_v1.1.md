# Cafélio

*Planner literário digital - vintage e aconchegante*

Versão 1.1 - Stack atualizada

| Autora | Plataforma | Versão |
|---|---|---|
| Angelica Mariana | Web + Mobile | 1.1 - 2026 |

> **Nota de versão (v1.0 → v1.1):** o backend do Cafélio começou em Python + FastAPI. A stack foi migrada para Java + Spring Boot porque as duas desenvolvedoras do projeto já têm mais domínio nessa tecnologia, o que acelera o desenvolvimento em dupla. Funcionalidades, identidade visual, plataforma e etapas de desenvolvimento permanecem as mesmas definidas na v1.0 - a mudança é exclusivamente na stack técnica do backend.

## Sobre o projeto

Cafélio é um planner literário digital que une o clima de diário de viagem com a aconchegante atmosfera de um café literário. O usuário monta sua estante virtual, cria cadernos personalizados, participa de desafios de leitura e acompanha suas estatísticas - tudo numa identidade visual vintage em tons de creme, marrom café e verde musgo.

## Plataforma

| Plataforma | Abordagem |
|---|---|
| Web app | Frontend responsivo - layout desktop com sidebar |
| Mobile (responsivo) | Mesmo código, layout adaptado com navegação inferior |

## Autenticação e conta

| Recurso | Descrição |
|---|---|
| Cadastro próprio | Criar conta com nome de usuário único, e-mail e senha (com confirmação e validação de força) |
| Login com Google | Entrada rápida via OAuth |
| Login tradicional | Entrar com e-mail/usuário e senha |
| Recuperação de senha | Link por e-mail e código de 6 dígitos por e-mail |
| Alterar senha | Trocar a senha a qualquer momento nas configurações |
| Foto de perfil | Upload de foto do usuário, armazenada no Cloudinary |
| Tratamento de erros | Mensagens claras: login incorreto, e-mail já cadastrado, senha inválida |

## Stack técnica (atualizada)

| Camada | Tecnologia |
|---|---|
| Backend | Java 25 (LTS) + Spring Boot (Maven) |
| Banco de dados | PostgreSQL (hospedado no Supabase) |
| Frontend | HTML + CSS + JavaScript + Chart.js |
| Autenticação | Spring Security + JWT (biblioteca JJWT) + Google OAuth |
| Hashing de senha | BCrypt |
| Migrations de banco | Flyway |
| Testes | JUnit 5 + MockMvc |
| E-mail | Spring Mail (SMTP) |
| Imagens | Cloudinary |
| Busca de livros | Open Library API |
| Pagamentos | Stripe |
| Deploy | Vercel (frontend) + Railway (backend) |

## Funcionalidades principais

| # | Funcionalidade | Descrição |
|---|---|---|
| 01 | Cadernos literários | Capa, figurinhas e temas visuais (Diário de viagem, Café literário, Cottagecore, Minimalista vintage) |
| 02 | Biblioteca pessoal | CRUD completo com 6 status coloridos e etiquetas |
| 03 | Status dos livros | Lendo, Lido, Quero ler, Meta literária, Relendo, Abandonei |
| 04 | Etiquetas | Favorito, Desejado, Tenho, Emprestei (independentes do status) |
| 05 | Opções do livro | Avaliar, data, meta, tempo, resenha, histórico, citações, excluir |
| 06 | Resenha | Postar na comunidade ou vincular a um caderno |
| 07 | Sorteio de livro | Sorteia aleatoriamente um livro da meta literária |
| 08 | Busca de livros | Open Library API |
| 09 | Desafio Allumez! | Velas se acendem a cada livro concluído |
| 10 | Desafio Flamme | Dias seguidos de leitura com grade visual |
| 11 | Desafios temáticos | Volta ao mundo, Autoras em foco, Brasil que lê, Fora da zona |
| 12 | Ranking global | Mais lidos, avaliados, em alta, literatura BR |
| 13 | Estatísticas | Gráficos de livros por mês, gêneros, páginas e mood |
| 14 | Mood de leitura | Viciante, denso, leve, chocante, emocionante |
| 15 | Calendário | Planejar e registrar sessões de leitura |
| 16 | Compteur | Total de páginas lidas no dashboard |
| 17 | Recomendação | Próximo livro com base no histórico (regra de negócio server-side, evolui pra IA numa fase futura) |
| 18 | Relatório anual | Tela compartilhável estilo Spotify Wrapped |

## Etapas de desenvolvimento

| Etapa | O que fazer | Detalhes |
|---|---|---|
| 1 | Setup | Spring Boot + PostgreSQL + estrutura de pacotes |
| 2 | Autenticação completa | Cadastro próprio, Google, recuperação por e-mail |
| 3 | Busca de livros | Integração Open Library API |
| 4 | Biblioteca pessoal | CRUD + status + etiquetas + sorteio |
| 5 | Cadernos | Capa + figurinhas + temas + Cloudinary |
| 6 | Citações e resenhas | CRUD + vínculo com caderno ou comunidade |
| 7 | Desafios | Allumez!, Flamme e temáticos |
| 8 | Ranking global | Mais lidos, avaliados, em alta, lit. BR |
| 9 | Estatísticas + Mood | Chart.js + mapa emocional |
| 10 | Calendário + Compteur | Sessões de leitura + total de páginas |
| 11 | Recomendação | Regra de negócio baseada em gêneros/autores mais lidos e avaliados |
| 12 | Relatório anual | Tela estilo Spotify Wrapped |
| 13 | Polish visual | CSS, responsividade, animações |
| 14 | Deploy beta | Vercel + Railway + Cloudinary |
| 15 | Monetização | Stripe - plano Cafélio Plus |

## Monetização

| Plano | O que inclui | Preço |
|---|---|---|
| Gratuito | Estante, 1 caderno, desafios básicos, ranking | R$ 0 |
| Cafélio Plus | Cadernos ilimitados, temas visuais, relatório anual, recomendação | R$ 14,90/mês |

## Fora do escopo

Itens avaliados e deliberadamente **não** incluídos, com o motivo registrado para evitar retrabalho.

### Recuperação de senha por SMS — removida em 05/10/2026

A versão 1.1 previa três mecanismos de recuperação: link por e-mail, código por e-mail e código por SMS. Os dois primeiros estão implementados; o terceiro foi retirado.

**Motivo.** Não existe envio programático de SMS gratuito — a cobrança vem da operadora, não do intermediário, e nenhum fornecedor a absorve. Todos os provedores (Twilio, Vonage, Zenvia, Amazon SNS) cobram por mensagem, em dólar, além de mensalidade do número remetente. Para um produto que ainda não tem receita, isso significa despesa fixa em moeda estrangeira sem retorno correspondente.

**O que o substitui.** A recuperação por e-mail — link e código de 6 dígitos, enviados juntos — atende integralmente a necessidade: a pessoa que esqueceu a senha recupera o acesso. O SMS seria conveniência adicional, não capacidade nova.

**Se for reavaliado no futuro**, duas observações valem registro:

- O código, o hash, a expiração e o limite de tentativas já estão implementados e são agnósticos de canal. Adicionar SMS exigiria apenas o envio em si, mais um campo de telefone na tabela `users` com fluxo de confirmação — nada da lógica de segurança precisaria ser refeito.
- No Brasil, a taxa de leitura do WhatsApp é muito superior à do SMS, e o TOTP (código gerado no próprio aparelho, via aplicativo autenticador) tem custo marginal zero e é imune a *SIM swap*. Qualquer reavaliação deveria comparar os três canais, não assumir o SMS.

## Resumo

Cafélio é um planner literário digital onde você organiza sua estante, cria cadernos personalizados, participa de desafios de leitura, acompanha suas estatísticas e recebe um relatório anual estilo Spotify Wrapped. O sistema recomenda o próximo livro com base no seu histórico e sorteia qual livro da sua meta você vai ler a seguir. Disponível em web e mobile, com identidade visual vintage e aconchegante que une o clima de diário de viagem com a atmosfera de um café literário. O backend é construído em Java com Spring Boot, aproveitando o domínio técnico das duas desenvolvedoras do projeto para acelerar a entrega.

*Cafélio - Escopo v1.1 - 2026 - Angelica Mariana Feitosa Pinto*
