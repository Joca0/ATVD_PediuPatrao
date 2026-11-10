package com.umc.pediupatrao.dto;

import com.umc.pediupatrao.entity.Pedido;

import java.util.List;
import java.util.Map;

public record DashboardDTO(
    long totalPedidos,
    double receitaTotal,
    long totalClientes,
    Map<String, Long> pedidosPorStatus,
    Map<String, Double> vendasPorDia,
    List<ProdutoRankingDTO> produtosMaisVendidos,
    List<Pedido> pedidosRecentes
) {
}
