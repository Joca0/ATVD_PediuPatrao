package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getDataPedido() == null) {
            pedido.setDataPedido(java.time.LocalDateTime.now());
        }
        
        if (pedido.getValorTotal() == null || pedido.getValorTotal() == 0) {
            produtoRepository.findByNome(pedido.getSabor()).ifPresent(p -> {
                pedido.setValorTotal(p.getPreco() * pedido.getQuantidade());
            });
        }
        
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido atualizarStatus(String id, String status) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }

}
