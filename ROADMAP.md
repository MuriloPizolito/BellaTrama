# BellaTrama — Roadmap

API REST de catálogo para uma loja familiar de bolsas artesanais de crochê.
Projeto de estudo (Java 25, Spring Boot 4) e de portfólio. As vendas acontecem pelo WhatsApp, então o foco é um catálogo confiável, administrável e publicado, e não um checkout completo.

**Legenda:** ✅ feito · 🔄 em andamento · ⬜ planejado · 💤 opcional / depende de necessidade real

---

## Princípios do projeto

1. **Entregar de ponta a ponta antes de ampliar.** Catálogo + login + imagens + frontend + deploy vêm antes de módulos extras.
2. **Regra de negócio com teste.** Toda regra documentada tem um teste que falha quando ela quebra (testado com "quebras" propositais no código).
3. **Simplicidade didática.** Sem biblioteca ou padrão que não reduza complexidade real (ex.: mapeamento manual de DTOs por enquanto).
4. **Decisões registradas.** Cada escolha relevante fica documentada (seção "Decisões" no fim).
5. **Commits pequenos**, uma mudança coerente por commit, suíte sempre verde.

---

## Etapa 1 — Catálogo de produtos ✅

- ✅ Cadastro, listagem, busca por id, atualização e desativação lógica (`ativo = false`)
- ✅ Validações com mensagens em português (Jakarta Validation)
- ✅ Consultas públicas filtram apenas produtos ativos
- ✅ Limites de coluna alinhados com a validação (`nome`, `descricao`, `cor`, `material`)

## Etapa 2 — Fotos dos produtos ✅

- ✅ Cadastro, listagem, busca, atualização e exclusão
- ✅ Regra da foto principal: no máximo uma por produto; a primeira foto sempre vira principal
- ✅ Não é possível desmarcar a principal isoladamente
- ✅ Ao excluir a principal, a **foto mais antiga** entre as restantes assume (`dataCadastro` + desempate por `id`)
- ✅ Foto sempre buscada junto do produto (`findByIdAndProdutoId`), impedindo acesso cruzado

## Etapa 3 — Tratamento de erros e testes 🔄

**Tratamento de erros**
- ✅ `GlobalExceptionHandler` com `ErrorResponse` padronizado
- ✅ Tipos de parâmetro inválidos, JSON malformado, método não permitido (405), mídia não suportada (415), rota inexistente (404)
- ✅ Handler genérico (500) com log e mensagem sem vazar detalhes internos
- ✅ Correção do bug de chave duplicada com várias violações no mesmo campo

**Testes automatizados**
- ✅ `FotoServiceTest` (JUnit 5 + Mockito + AssertJ, organizado com `@Nested`): 18 testes
- ⬜ `GlobalExceptionHandlerTest` (`@WebMvcTest` com controller de teste)
- ⬜ `ProdutoServiceTest` (regras: `ativo = true` no cadastro, inativo não é encontrado, desativação)
- ⬜ `ProdutoControllerTest` e `FotoControllerTest` (status HTTP, rotas, chamada ao service)
- ⬜ Testes de integração com PostgreSQL real (Testcontainers): ordenação da substituta, busca de foto que não cruza produtos, limites de coluna
- ⬜ Exportar a coleção do Postman para o repositório

## Etapa 4 — Base de infraestrutura ⬜

- ⬜ Perfis `dev` / `prod` e credenciais por variáveis de ambiente (nada de senha no repositório)
- ⬜ Flyway: migrações versionadas no lugar de `ddl-auto: update`
- ⬜ Índice único parcial no PostgreSQL: *uma foto principal por produto* (atenção à ordem do flush no Hibernate: `saveAndFlush`/`flush` ao trocar a principal)
- ⬜ Docker Compose com PostgreSQL (e depois a API)
- ⬜ GitHub Actions rodando `mvn verify`
- ⬜ README: como rodar, decisões, estrutura do projeto

## Etapa 5 — Ajustes de API antes de ter consumidor ⬜

- ⬜ Rotas no plural (`/produtos`, `/produtos/{id}/fotos`)
- ⬜ Desativação mais explícita (ex.: `PATCH /produtos/{id}/desativar`)
- ⬜ Rotas públicas vs. administrativas definidas (público só vê produtos ativos; admin vê e reativa inativos)
- ⬜ Fotos de produto inativo: decidir comportamento nas rotas públicas
- ⬜ Paginação apenas se o catálogo crescer

## Etapa 6 — Segurança mínima ⬜

- ⬜ Spring Security: `GET` público, escrita apenas para o administrador
- ⬜ Um único admin (HTTP Basic ou JWT simples); evitar refresh token e perfis múltiplos
- ⬜ Testes de segurança (anônimo recebe 401/403 nas rotas de escrita)

## Etapa 7 — Documentação da API ⬜

- ⬜ OpenAPI/Swagger com respostas de sucesso e de erro documentadas

## Etapa 8 — Armazenamento de imagens ⬜

- ⬜ Decisão registrada: serviço de armazenamento (Cloudinary, Cloudflare R2 ou volume local)
- ⬜ Interface `ArmazenamentoImagem` para trocar a implementação sem mexer no domínio
- ⬜ Upload com `MultipartFile`, validação de tipo e tamanho, retorno da URL
- ⬜ Substituir URLs digitadas manualmente

## Etapa 9 — Frontend ⬜

- ⬜ Catálogo público: lista, detalhe, galeria de fotos, preço e disponibilidade
- ⬜ Botão "Comprar pelo WhatsApp" com mensagem pronta (`wa.me`), sem checkout
- ⬜ Painel administrativo: produtos e fotos, com validação e mensagens de erro
- ⬜ Tecnologia a definir (frontend simples; o foco do portfólio é o backend)

## Etapa 10 — Deploy ⬜

- ⬜ Dockerfile da API e Compose de produção
- ⬜ HTTPS, variáveis de ambiente, logs
- ⬜ Backup e recuperação do banco
- ⬜ Publicação da API e do frontend

---

## Etapa 11 — Pedidos 💤 (só se a operação precisar)

Entra depois do deploy, quando houver necessidade real. Pelo escopo atual (vendas no WhatsApp), não bloqueia o uso da loja.

- 💤 `Pedido` (nome e telefone do cliente, observação, data, status, itens) e `ItemPedido` (produto, quantidade, preço unitário)
- 💤 Sem entidade `Cliente` por enquanto (decisão: vendas pelo WhatsApp)
- 💤 Preço unitário gravado no item para preservar o histórico
- 💤 Status: `PENDENTE`, `CONFIRMADO`, `CANCELADO`
- 💤 Estoque diminui apenas ao confirmar; pedidos pendentes não reservam estoque
- 💤 **Antes de codar:** especificar por escrito cancelamento, devolução ao estoque e transições de status válidas
- 💤 Concorrência: dois pedidos confirmando o mesmo estoque (lock otimista ou atualização condicional)

---

## Melhorias opcionais (quando houver tempo)

- 💤 Observabilidade: logs estruturados, métricas básicas
- 💤 Reativação de produtos pelo admin
- 💤 Revisar comparação por `equals` nas entidades se algum dia usar `@EqualsAndHashCode`

---

## Decisões já tomadas

| Tema | Decisão |
|---|---|
| Exclusão de produto | Lógica (`ativo = false`), nunca física |
| Estoque zero | Não desativa o produto automaticamente |
| Foto principal | Exatamente uma quando há fotos; troca por promoção de outra |
| Substituta ao excluir a principal | A mais antiga (`dataCadastro`, desempate por `id`) |
| Imagens (hoje) | Apenas URLs; upload real na Etapa 8 |
| DTOs | Mapeamento manual (sem MapStruct) |
| Testes de service | Unitários com Mockito; o que o mock não prova vai para integração |
| Pedidos | Adiados; sem entidade `Cliente` |

## O que testes com mock **não** provam (por isso há testes de integração)

- Que a consulta `findFirstBy...OrderBy...` realmente ordena certo
- Que `findByIdAndProdutoId` não devolve foto de outro produto
- Que as colunas aceitam o tamanho que a validação permite
- Que o `@Transactional` persiste as mudanças (dirty checking)
