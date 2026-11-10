package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    public void deveCalcularValorTotalAoCriarPedido() {
        // Cenário: pedido com 2 itens de pizza a R$50
        ItemPedido item = new ItemPedido(null, "Pizza Calabresa", 2, 50.0);

        Pedido pedido = new Pedido();
        pedido.setClienteId("cliente-01");
        pedido.setItens(List.of(item));

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> {
            Pedido p = (Pedido) i.getArguments()[0];
            p.setId("pedido-01");
            return p;
        });

        doNothing().when(auditoriaService).registrar(any(), any(), any(), any(), any(), any());

        // Ação
        Pedido pedidoCriado = pedidoService.criarPedido(pedido);

        // Verificações
        assertEquals(100.0, pedidoCriado.getValorTotal(), "O valor total deve ser preço * quantidade");
        assertEquals(100.0, pedidoCriado.getValorBruto(), "O valor bruto deve ser a soma dos itens");
        assertEquals(0.0, pedidoCriado.getDesconto(), "Desconto deve ser zero por padrão");
        assertEquals("RECEBIDO", pedidoCriado.getStatus(), "Status inicial deve ser RECEBIDO");
        assertNotNull(pedidoCriado.getDataEntrada(), "Data de entrada deve ser registrada");
    }

    @Test
    public void deveBloquearTransicaoInvalida() {
        Pedido pedido = new Pedido();
        pedido.setId("pedido-02");
        pedido.setStatus("RECEBIDO");

        when(pedidoRepository.findById("pedido-02")).thenReturn(Optional.of(pedido));

        // Tentar ir de RECEBIDO para FINALIZADO diretamente deve lançar exceção
        assertThrows(IllegalStateException.class, () ->
                pedidoService.atualizarStatus("pedido-02", "FINALIZADO"));
    }

    @Test
    public void deveBloquearDescontoAcimaDe20Porcento() {
        // A validação do percentual ocorre antes de consultar o repositório
        assertThrows(IllegalArgumentException.class, () ->
                pedidoService.aplicarDesconto("pedido-03", 25.0));
    }
}
