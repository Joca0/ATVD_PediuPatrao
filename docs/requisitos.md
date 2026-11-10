# Requisitos do Sistema - PediuPatrão

Este documento descreve os requisitos funcionais e não funcionais do sistema PediuPatrão.

## 1. Requisitos Funcionais (RF)

| ID | Nome | Descrição | Ator |
|:---|:---|:---|:---|
| **RF01** | Controle de Usuários e Permissões | O sistema deve gerenciar três perfis de acesso: **ADMIN** (gestão de usuários, perfis e auditoria), **GERENTE** (gestão de pedidos, descontos, cancelamentos e auditoria) e **ATENDENTE** (gestão de clientes e pedidos). | Sistema |
| **RF02** | Gestão de Clientes | Permite cadastrar, consultar, atualizar e excluir informações de clientes, incluindo endereço e telefone. | ATENDENTE, GERENTE |
| **RF03** | Gestão de Produtos | Permite o cadastro e manutenção do catálogo de produtos (pizzas, bebidas, etc). | ADMIN, GERENTE |
| **RF04** | Registro de Pedidos | Registrar pedidos com itens, quantidades e preços. O cálculo do valor total deve ser obrigatoriamente realizado no servidor. | ATENDENTE |
| **RF05** | Fluxo de Status de Pedido | O sistema deve impor a transição de estados: RECEBIDO -> EM PREPARAÇÃO -> PRONTO -> SAIU PARA ENTREGA/RETIRADO -> FINALIZADO. | ATENDENTE, GERENTE |
| **RF06** | Aplicação de Descontos | O GERENTE pode aplicar descontos percentuais de no máximo 20% antes do pedido sair para entrega. | GERENTE |
| **RF07** | Cancelamento de Pedidos | O GERENTE pode cancelar pedidos antes da saída, fornecendo obrigatoriamente uma justificativa. | GERENTE |
| **RF08** | Auditoria de Alterações | Registro persistente de inclusões/alterações em pedidos e clientes, contendo usuário, data/hora, campos alterados (valor antigo/novo) e justificativa. | Sistema |
| **RF09** | Consulta de Auditoria | Visualização dos registros de auditoria para fins de conferência e segurança. | ADMIN, GERENTE |
| **RF10** | Dashboard Administrativo | Painel centralizado para acompanhamento de desempenho e tendências da operação em tempo real. | ADMIN, GERENTE |

## 2. Requisitos do Dashboard Administrativo (RFD)

| ID | Nome | Descrição |
|:---|:---|:---|
| **RFD01** | Indicadores gerais de vendas | O dashboard deve exibir indicadores consolidados: **Total de Pedidos**, **Receita Total** (formatada em R$) e **Total de Clientes** cadastrados na base. |
| **RFD02** | Análise de vendas (gráficos) | Apresentação de um gráfico de linha demonstrando a evolução do faturamento (Receita) por dia, permitindo identificar padrões de consumo. |
| **RFD03** | Situação dos pedidos | Exibição de uma listagem dos pedidos recentes com seus respectivos status e contagem de pedidos em estado "Pendente" para ação imediata. |
| **RFD04** | Produtos mais vendidos | Ranking visual dos 5 produtos mais vendidos (por sabor/nome), auxiliando na tomada de decisão sobre estoque e promoções. |
| **RFD05** | Filtros de consulta | Interface que permite ao usuário filtrar todos os dados e gráficos do dashboard por um intervalo de datas (Início e Fim). |
| **RFD06** | Integração com a API | O dashboard deve ser alimentado exclusivamente por uma API REST (`/api/dashboard`), consumindo dados em formato JSON. |
| **RFD07** | Atualização e tratamento de dados | O sistema deve realizar o "auto-refresh" dos dados a cada 5 minutos e tratar possíveis falhas de comunicação com o servidor, garantindo a continuidade da operação. |

## 3. Requisitos Não Funcionais (RNF)

| ID | Categoria | Descrição |
|:---|:---|:---|
| **RNF01** | Segurança | A autenticação e autorização devem ser implementadas no backend utilizando **Spring Security**, com controle de acesso baseado em Roles (ADMIN, GERENTE, ATENDENTE). |
| **RNF02** | Proteção de Dados | Implementação de proteção contra ataques **CSRF** e filtragem de inputs para evitar injeções maliciosas. |
| **RNF03** | Persistência | Utilização de banco de dados NoSQL **MongoDB** para armazenamento flexível e escalável de pedidos e auditoria. |
| **RNF04** | Integridade | Garantia de imutabilidade dos registros de auditoria, impedindo alterações retroativas nos logs do sistema. |
| **RNF05** | Privacidade | Criptografia de senhas (BCrypt) e conformidade com princípios de proteção de dados, ocultando informações sensíveis em logs. |
| **RNF06** | Disponibilidade/Usabilidade | Interface responsiva baseada em **AdminLTE/Bootstrap**, garantindo acessibilidade em diferentes dispositivos. |
| **RNF07** | Desempenho | O processamento das métricas do dashboard deve ser eficiente, utilizando agregações ou filtros otimizados no MongoDB para evitar lentidão. |
| **RNF08** | Padronização | Adoção da arquitetura em camadas (Controller, Service, Repository) e uso de DTOs para transferência de dados entre API e Frontend. |
