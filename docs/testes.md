# Evidências de Testes - PediuPatrão

Este documento apresenta as evidências de testes realizados no sistema PediuPatrão, garantindo a qualidade e o funcionamento dos requisitos implementados.

## 1. Testes Automatizados

Foram implementados testes unitários utilizando **JUnit 5** e **Mockito** para validar as regras de negócio críticas.

### Teste de Cálculo de Valor Total de Pedido
- **Classe:** `PedidoServiceTest`
- **Descrição:** Verifica se o sistema calcula corretamente o valor total do pedido multiplicando o preço do produto pela quantidade, buscando o preço no repositório de produtos.
- **Resultado:** Sucesso.

**Log de execução:**
```text
[INFO] Running com.umc.pediupatrao.service.PedidoServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.449 s -- in com.umc.pediupatrao.service.PedidoServiceTest
[INFO] Results:
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 2. Testes de API (Manuais)

Os endpoints foram validados através de requisições via cliente REST.

### Fluxo de Criação de Pedido
1. **Listagem de Produtos:** Validado o endpoint `/api/produtos` retornando o catálogo.
2. **Criação de Pedido:** Enviado POST para `/api/pedidos` com sucesso, confirmando a persistência no MongoDB.
3. **Atualização de Status:** Validado a transição de status via PUT `/api/pedidos/{id}/status`.

### Segurança e Permissões
- Verificado que tentativas de acesso sem autenticação retornam **401 Unauthorized**.
- Verificado que usuários com papel inferior ao solicitado pelo endpoint retornam **403 Forbidden**.

---

## 3. Testes de Banco de Dados (MongoDB)

- **Persistência:** Validada a criação de documentos nas coleções `clientes`, `pedidos`, `produtos` e `usuarios`.
- **Relacionamentos:** Verificado que o `clienteId` no pedido associa corretamente o cliente cadastrado.

---

## 4. Dashboard Administrativo

Foram realizados testes manuais de integração para validar os requisitos funcionais do painel (RF01 a RF07).

| Caso de Teste | Procedimento | Resultado Esperado | Status |
|:---|:---|:---|:---|
| **T01 - Carga de Indicadores** | Acessar `/admin` e aguardar o carregamento inicial. | Total de Pedidos, Receita e Clientes devem refletir os dados reais do MongoDB. | ✅ Sucesso |
| **T02 - Gráfico de Vendas** | Verificar se o gráfico de linha exibe pontos de dados correspondentes aos dias com pedidos. | Gráfico renderizado com valores de faturamento diário. | ✅ Sucesso |
| **T03 - Filtro por Período** | Selecionar um intervalo de datas e clicar no botão de filtro. | Os indicadores e o gráfico devem ser atualizados para refletir apenas o período selecionado. | ✅ Sucesso |
| **T04 - Ranking de Produtos** | Conferir se a lista de "Produtos Mais Vendidos" ordena corretamente por quantidade. | Exibição do Top 5 produtos mais vendidos. | ✅ Sucesso |
| **T05 - Pedidos Recentes** | Validar se a tabela de pedidos recentes mostra os últimos registros inseridos. | Tabela preenchida com ID, Sabor, Status e Quantidade. | ✅ Sucesso |
| **T06 - Atualização Automática** | Permanecer na página por mais de 5 minutos. | O sistema deve disparar uma nova requisição AJAX para `/api/dashboard`. | ✅ Sucesso |
| **T07 - Tratamento de Erros** | Simular queda do servidor e observar o console/interface. | A interface deve lidar com o erro de requisição sem travar o navegador. | ✅ Sucesso |

---

## 5. Resumo Geral de Cobertura

O plano de testes cobriu 100% dos requisitos funcionais (RF01-RF10) e validou a integração fim-a-fim entre o Frontend (Thymeleaf/AJAX), Backend (Spring Boot API) e Banco de Dados (MongoDB).
