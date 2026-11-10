# PediuPatrão — Sistema de Pedidos

Sistema de gerenciamento de pedidos para pizzaria, desenvolvido com Java, Spring Boot, Spring Security, MongoDB e Thymeleaf.

## Requisitos

- Java 21+
- Maven 3.8+
- MongoDB rodando em `localhost:27017`

## Como executar

```bash
# 1. Suba o MongoDB (via Docker, se preferir)
docker run -d -p 27017:27017 --name mongodb mongo:7

# 2. Clone e entre no diretório do projeto
cd ATVD_PediuPatrao

# 3. Compile e execute
./mvnw spring-boot:run
```

Acesse: [http://localhost:8080](http://localhost:8080)

## Usuários iniciais (criados automaticamente)

| Usuário     | Senha           | Perfil     |
|-------------|-----------------|------------|
| `admin`     | `admin123`      | ADMIN      |
| `gerente`   | `gerente123`    | GERENTE    |
| `atendente` | `atendente123`  | ATENDENTE  |

## Requisitos implementados

### RF01 — Controle de usuários e permissões
- Três perfis: **ADMIN**, **GERENTE**, **ATENDENTE**
- `UsuarioDetailsService` usa o role real do banco de dados
- `SecurityConfig` com `@EnableMethodSecurity` e proteção de rotas por perfil
- Formulário de usuário restrito ao ADMIN com seleção dos 3 perfis
- Sidebar exibe Auditoria apenas para ADMIN/GERENTE e Usuários apenas para ADMIN

### RF02 — Evolução do cadastro de pedidos
- Entidade `Pedido` evoluída com `ItemPedido` (produto, quantidade, preço unitário)
- Campos adicionados: `valorBruto`, `desconto`, `valorTotal`, `dataEntrada`, `responsavelRegistro`
- Valor total **calculado no servidor** via `calcularTotal()` — cliente não pode definir o total
- Preços dos produtos buscados no banco de dados durante o registro do pedido

### RF03 — Controle de entrada, saída e transições
- Fluxo obrigatório: `RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA/RETIRADO → FINALIZADO`
- Transições inválidas bloqueadas com `IllegalStateException`
- `dataEntrada` e `responsavelRegistro` registrados ao criar o pedido
- `dataSaida` e `responsavelSaida` registrados ao avançar para `SAIU_PARA_ENTREGA` ou `RETIRADO`

### RF04 — Cancelamentos e descontos (exclusivos do GERENTE)
- `cancelarPedido()` exige justificativa obrigatória e é restrito com `@PreAuthorize("hasRole('GERENTE')")`
- Cancelamento bloqueado após saída para entrega/retirada
- `aplicarDesconto()` limitado a 20%, bloqueado após saída, restrito ao GERENTE
- Ambas as operações registradas na auditoria

### RF05 — Auditoria
- Entidade `RegistroAuditoria` persistida no MongoDB (coleção `auditoria`)
- `AuditoriaService` captura automaticamente usuário autenticado e perfil do `SecurityContextHolder`
- Auditoria integrada em:
  - `PedidoService`: criação, mudança de status, cancelamento, desconto
  - `ClienteService`: criação, alterações de cada campo (nome, telefone, endereço etc.), exclusão
- Consulta de auditoria restrita a ADMIN e GERENTE (`/auditoria`)
- Registros **não podem ser alterados ou excluídos** pelas interfaces comuns
- Campos registrados: usuário, perfil, operação, entidade, campo, valor anterior, novo valor, justificativa, data/hora

## Principais alterações realizadas

| Componente | Alteração |
|---|---|
| `UsuarioDetailsService` | Usa role real do banco (não mais fixo em "USER") |
| `SecurityConfig` | 3 perfis, proteção de rotas, `@EnableMethodSecurity`, CSRF habilitado para forms |
| `Pedido` | Adicionados: itens, valorBruto, desconto, valorTotal, dataEntrada, dataSaida, responsáveis, cancelamento |
| `ItemPedido` | Nova entidade embutida com produto, quantidade, preço unitário e subtotal |
| `PedidoService` | Transições validadas, cálculo no servidor, cancelamento/desconto com `@PreAuthorize` |
| `ClienteService` | Auditoria de cada campo alterado |
| `RegistroAuditoria` | Nova entidade de auditoria persistente |
| `AuditoriaService` | Captura contexto de segurança e persiste registros |
| `AuditoriaController` | Consulta restrita a ADMIN/GERENTE |
| `PedidoController` | Reescrito como MVC com todas as rotas de pedidos |
| Templates | Novos: pedidos/lista, form, detalhe, cancelar, desconto, auditoria/lista, acesso-negado |
| `sidebar.html` | Links com controle de visibilidade por perfil |

## Executar testes

```bash
./mvnw test
```
