package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardDTO;
import com.umc.pediupatrao.dto.ProdutoRankingDTO;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public DashboardDTO getDashboardData(LocalDateTime inicio, LocalDateTime fim) {
        List<Pedido> todosPedidos = pedidoRepository.findAll();
        
        List<Pedido> pedidos = todosPedidos.stream()
                .filter(p -> p.getDataEntrada() != null || p.getDataPedido() != null)
                .filter(p -> {
                    LocalDateTime data = p.getDataEntrada() != null ? p.getDataEntrada() : p.getDataPedido();
                    return (inicio == null || !data.isBefore(inicio)) &&
                           (fim == null || !data.isAfter(fim));
                })
                .collect(Collectors.toList());

        // RF01 Indicadores gerais
        long totalPedidos = pedidos.size();
        double receitaTotal = pedidos.stream()
                .filter(p -> p.getValorTotal() != null)
                .mapToDouble(Pedido::getValorTotal)
                .sum();
        long totalClientes = clienteRepository.count();

        // RF03 Situação dos pedidos
        Map<String, Long> pedidosPorStatus = pedidos.stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));

        // RF02 Análise de vendas (vendas por dia)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Double> vendasPorDia = pedidos.stream()
                .filter(p -> (p.getDataEntrada() != null || p.getDataPedido() != null) && p.getValorTotal() != null)
                .collect(Collectors.groupingBy(
                        p -> (p.getDataEntrada() != null ? p.getDataEntrada() : p.getDataPedido()).format(formatter),
                        Collectors.summingDouble(Pedido::getValorTotal)
                ));

        // RF04 Produtos mais vendidos
        List<ProdutoRankingDTO> ranking = pedidos.stream()
                .flatMap(p -> {
                    if (p.getItens() != null && !p.getItens().isEmpty()) {
                        return p.getItens().stream()
                                .map(item -> new ProdutoRankingDTO(item.getNomeProduto(), (long) item.getQuantidade()));
                    } else if (p.getSabor() != null) {
                        return java.util.stream.Stream.of(new ProdutoRankingDTO(p.getSabor(), (long) p.getQuantidade()));
                    }
                    return java.util.stream.Stream.empty();
                })
                .collect(Collectors.groupingBy(ProdutoRankingDTO::nome, Collectors.summingLong(ProdutoRankingDTO::quantidade)))
                .entrySet().stream()
                .map(entry -> new ProdutoRankingDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.quantidade(), a.quantidade()))
                .limit(5)
                .collect(Collectors.toList());

        // Pedidos recentes
        List<Pedido> recentes = todosPedidos.stream()
                .sorted((a, b) -> {
                    LocalDateTime dataA = a.getDataEntrada() != null ? a.getDataEntrada() : a.getDataPedido();
                    LocalDateTime dataB = b.getDataEntrada() != null ? b.getDataEntrada() : b.getDataPedido();
                    if (dataA == null) return 1;
                    if (dataB == null) return -1;
                    return dataB.compareTo(dataA);
                })
                .limit(10)
                .collect(Collectors.toList());

        return new DashboardDTO(
            totalPedidos,
            receitaTotal,
            totalClientes,
            pedidosPorStatus,
            vendasPorDia,
            ranking,
            recentes
        );
    }
}
