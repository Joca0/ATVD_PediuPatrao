# Documentação da API - PediuPatrão

Esta documentação detalha os endpoints da API REST do sistema PediuPatrão.

## Base URL
`http://localhost:8080/api`

---

## 1. Usuários (`/usuarios`)

### Listar Usuários
- **URL:** `/usuarios`
- **Método:** `GET`
- **Resposta (200 OK):**
```json
[
  {
    "id": "60d5f9b2f1d2b23a1c8b4567",
    "username": "admin",
    "role": "ADMIN"
  }
]
```

### Buscar Usuário por ID
- **URL:** `/usuarios/{id}`
- **Método:** `GET`

### Criar Usuário
- **URL:** `/usuarios`
- **Método:** `POST`
- **Corpo da Requisição:**
```json
{
  "username": "joao",
  "password": "123",
  "role": "ATENDENTE"
}
```

### Atualizar Usuário
- **URL:** `/usuarios/{id}`
- **Método:** `PUT`
- **Corpo da Requisição:**
```json
{
  "username": "joao_editado",
  "password": "new_password"
}
```

### Deletar Usuário
- **URL:** `/usuarios/{id}`
- **Método:** `DELETE`

---

## 2. Clientes (`/clientes`)

### Listar Clientes
- **URL:** `/clientes`
- **Método:** `GET`

### Criar Cliente
- **URL:** `/clientes`
- **Método:** `POST`
- **Corpo da Requisição:**
```json
{
  "nome": "Fulano de Tal",
  "telefone": "11999999999",
  "cep": "01234-567",
  "logradouro": "Rua Exemplo",
  "numero": "123",
  "bairro": "Centro",
  "cidade": "Mogi das Cruzes",
  "estado": "SP"
}
```
- **Resposta (201 Created):** Retorna o objeto criado com o `id` gerado.

### Atualizar Cliente
- **URL:** `/clientes/{id}`
- **Método:** `PUT`
- **Corpo da Requisição:** Mesmo do POST.

### Excluir Cliente
- **URL:** `/clientes/{id}`
- **Método:** `DELETE`

---

## 3. Pedidos (`/pedidos`)

### Listar Pedidos
- **URL:** `/pedidos`
- **Método:** `GET`

### Criar Pedido
- **URL:** `/pedidos`
- **Método:** `POST`
- **Corpo da Requisição:**
```json
{
  "clienteId": "60d5f9b2f1d2b23a1c8b4567",
  "tipoPizza": "Grande",
  "quantidade": 1,
  "sabor": "Calabresa"
}
```
- **Resposta (200 OK):** Retorna o pedido com `id`, `dataPedido` e `valorTotal` calculado.

### Atualizar Status do Pedido
- **URL:** `/pedidos/{id}/status`
- **Método:** `PUT`
- **Parâmetros:** `status` (String)
- **Exemplo:** `/api/pedidos/123/status?status=EM PREPARAÇÃO`

---

## 4. Produtos (`/produtos`)

### Listar Produtos
- **URL:** `/produtos`
- **Método:** `GET`

### Criar Produto
- **URL:** `/produtos`
- **Método:** `POST`
- **Corpo da Requisição:**
```json
{
  "nome": "Pizza Margherita",
  "tipo": "Pizza",
  "preco": 45.0,
  "ativo": true
}
```
- **Resposta (201 Created):** Retorna o produto com o `id` gerado.

### Atualizar Produto
- **URL:** `/produtos/{id}`
- **Método:** `PUT`
- **Corpo da Requisição:** Mesmo do POST.

### Excluir Produto
- **URL:** `/produtos/{id}`
- **Método:** `DELETE`

---

## 5. Dashboard (`/dashboard`)

### Obter Dados do Dashboard
Retorna métricas consolidadas para o painel administrativo, permitindo filtragem por período.

- **URL:** `/dashboard`
- **Método:** `GET`
- **Parâmetros Opcionais:** 
  - `inicio` (ISO Date Time): Data inicial para o filtro (ex: `2023-10-01T00:00:00`)
  - `fim` (ISO Date Time): Data final para o filtro (ex: `2023-10-31T23:59:59`)
- **Resposta (200 OK):**
```json
{
  "totalPedidos": 25,
  "receitaTotal": 1500.50,
  "totalClientes": 12,
  "pedidosPorStatus": {
    "RECEBIDO": 5,
    "EM PREPARAÇÃO": 10,
    "FINALIZADO": 10
  },
  "vendasPorDia": {
    "2023-10-25": 450.00,
    "2023-10-26": 1050.50
  },
  "produtosMaisVendidos": [
    { "nome": "Calabresa", "quantidade": 15 },
    { "nome": "Margherita", "quantidade": 8 }
  ],
  "pedidosRecentes": [
    {
      "id": "60d5f9b2f1d2b23a1c8b4567",
      "sabor": "Calabresa",
      "tipoPizza": "Grande",
      "quantidade": 2,
      "status": "RECEBIDO",
      "dataPedido": "2023-10-26T14:30:00",
      "valorTotal": 90.00
    }
  ]
}
```

---

## Possíveis Erros

| Código | Descrição |
|:---|:---|
| **400 Bad Request** | Requisição malformada ou parâmetros inválidos. |
| **401 Unauthorized** | Usuário não autenticado. |
| **403 Forbidden** | Usuário autenticado mas sem permissão para o recurso (ex: ATENDENTE acessando auditoria). |
| **404 Not Found** | Recurso não encontrado. |
| **500 Internal Server Error** | Erro inesperado no servidor. |
