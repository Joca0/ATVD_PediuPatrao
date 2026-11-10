package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PedidoService {

    // Transições válidas de status
    private static final Map<String, Set<String>> TRANSICOES_VALIDAS = Map.of(
            "RECEBIDO",          Set.of("EM_PREPARACAO", "CANCELADO"),
            "EM_PREPARACAO",     Set.of("PRONTO", "CANCELADO"),
            "PRONTO",            Set.of("SAIU_PARA_ENTREGA", "RETIRADO", "CANCELADO"),
            "SAIU_PARA_ENTREGA", Set.of("FINALIZADO"),
            "RETIRADO",          Set.of("FINALIZADO"),
            "FINALIZADO",        Set.of(),
            "CANCELADO",         Set.of()
    );

    // Status que indicam que o pedido já saiu
    private static final Set<String> STATUS_SAIU = Set.of("SAIU_PARA_ENTREGA", "RETIRADO", "FINALIZADO");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.auditoriaService = auditoriaService;
    }

    public Pedido criarPedido(Pedido pedido) {
        String usuario = getUsuarioAtual();

        pedido.setStatus("RECEBIDO");
        pedido.setDataEntrada(LocalDateTime.now());
        pedido.setResponsavelRegistro(usuario);
        pedido.setDesconto(0.0);

        // Enriquecer itens com preço do produto (calculado no servidor)
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                if (item.getPrecoUnitario() <= 0 && item.getProdutoId() != null) {
                    Optional<Produto> prod = produtoRepository.findById(item.getProdutoId());
                    prod.ifPresent(p -> item.setPrecoUnitario(p.getPreco()));
                }
            }
        }

        // Calcular total no servidor - ignora qualquer valor enviado pelo cliente
        pedido.calcularTotal();

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "CRIACAO_PEDIDO", "Pedido", salvo.getId(),
                "status", null, "RECEBIDO"
        );

        return salvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(String id) {
        return pedidoRepository.findById(id);
    }

    public Pedido atualizarStatus(String id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        String statusAtual = pedido.getStatus();

        // Bloquear alteração após cancelamento
        if ("CANCELADO".equals(statusAtual)) {
            throw new IllegalStateException("Pedido cancelado não pode ter status alterado.");
        }

        // Bloquear CANCELADO via este método (usar cancelarPedido)
        if ("CANCELADO".equals(novoStatus)) {
            throw new IllegalArgumentException("Use o endpoint de cancelamento para cancelar um pedido.");
        }

        // Validar transição
        Set<String> permitidos = TRANSICOES_VALIDAS.getOrDefault(statusAtual, Set.of());
        if (!permitidos.contains(novoStatus)) {
            throw new IllegalStateException(
                    String.format("Transição inválida: %s → %s", statusAtual, novoStatus));
        }

        String usuario = getUsuarioAtual();

        // Registrar dataSaida e responsável quando sai para entrega ou retirada
        if ("SAIU_PARA_ENTREGA".equals(novoStatus) || "RETIRADO".equals(novoStatus)) {
            pedido.setDataSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(usuario);
        }

        pedido.setStatus(novoStatus);
        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "ALTERACAO_STATUS", "Pedido", id,
                "status", statusAtual, novoStatus
        );

        return salvo;
    }

    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelarPedido(String id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Justificativa é obrigatória para cancelamento.");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        String statusAtual = pedido.getStatus();

        if ("CANCELADO".equals(statusAtual)) {
            throw new IllegalStateException("Pedido já está cancelado.");
        }

        if (STATUS_SAIU.contains(statusAtual)) {
            throw new IllegalStateException(
                    "Não é possível cancelar um pedido que já saiu para entrega ou foi retirado.");
        }

        String usuario = getUsuarioAtual();
        pedido.setStatus("CANCELADO");
        pedido.setMotivoCancelamento(motivo);
        pedido.setResponsavelCancelamento(usuario);
        pedido.setDataCancelamento(LocalDateTime.now());

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "CANCELAMENTO", "Pedido", id,
                "status", statusAtual, "CANCELADO", motivo
        );

        return salvo;
    }

    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(String id, Double percentual) {
        if (percentual == null || percentual < 0 || percentual > 20) {
            throw new IllegalArgumentException("Desconto deve ser entre 0% e 20%.");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        if ("CANCELADO".equals(pedido.getStatus())) {
            throw new IllegalStateException("Não é possível aplicar desconto em pedido cancelado.");
        }

        if (STATUS_SAIU.contains(pedido.getStatus())) {
            throw new IllegalStateException(
                    "Não é possível aplicar desconto após a saída do pedido.");
        }

        Double descontoAnterior = pedido.getDesconto();
        Double totalAnterior = pedido.getValorTotal();

        pedido.setDesconto(percentual);
        pedido.calcularTotal();

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                "DESCONTO", "Pedido", id,
                "desconto",
                String.format("%.1f%% (R$ %.2f)", descontoAnterior, totalAnterior),
                String.format("%.1f%% (R$ %.2f)", percentual, salvo.getValorTotal())
        );

        return salvo;
    }

    private String getUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "sistema";
    }
}
