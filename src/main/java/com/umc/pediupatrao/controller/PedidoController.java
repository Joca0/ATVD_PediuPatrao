package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    public PedidoController(PedidoService pedidoService,
                            ClienteService clienteService,
                            ProdutoService produtoService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    @GetMapping
    public String listar(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        Map<String, String> nomesClientes = new HashMap<>();
        clienteService.listarClientes().forEach(c -> nomesClientes.put(c.getId(), c.getNome()));
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("nomesClientes", nomesClientes);
        model.addAttribute("content", "pedidos/lista :: content");
        return "layout";
    }

    @GetMapping("/novo")
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public String formNovo(Model model) {
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("produtos", produtoService.listarProdutos());
        model.addAttribute("content", "pedidos/form :: content");
        return "layout";
    }

    @PostMapping("/salvar")
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public String salvar(@ModelAttribute Pedido pedido,
                         @RequestParam(value = "produtoIds", required = false) List<String> produtoIds,
                         @RequestParam(value = "quantidades", required = false) List<Integer> quantidades,
                         RedirectAttributes redirectAttributes) {
        try {
            List<ItemPedido> itens = new ArrayList<>();
            if (produtoIds != null) {
                for (int i = 0; i < produtoIds.size(); i++) {
                    String prodId = produtoIds.get(i);
                    int qtd = (quantidades != null && i < quantidades.size()) ? quantidades.get(i) : 1;
                    if (prodId != null && !prodId.isBlank() && qtd > 0) {
                        Produto prod = produtoService.buscarPorId(prodId).orElse(null);
                        if (prod != null) {
                            itens.add(new ItemPedido(prodId, prod.getNome(), qtd, prod.getPreco()));
                        }
                    }
                }
            }
            pedido.setItens(itens);
            pedidoService.criarPedido(pedido);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable String id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
        model.addAttribute("pedido", pedido);
        clienteService.buscarPorId(pedido.getClienteId())
                .ifPresent(c -> model.addAttribute("cliente", c));
        model.addAttribute("content", "pedidos/detalhe :: content");
        return "layout";
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable String id,
                                  @RequestParam String status,
                                  RedirectAttributes redirectAttributes) {
        try {
            pedidoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("sucesso", "Status atualizado para: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @GetMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('GERENTE')")
    public String formCancelar(@PathVariable String id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
        model.addAttribute("pedido", pedido);
        model.addAttribute("content", "pedidos/cancelar :: content");
        return "layout";
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('GERENTE')")
    public String cancelar(@PathVariable String id,
                           @RequestParam String motivo,
                           RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cancelarPedido(id, motivo);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido cancelado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @GetMapping("/{id}/desconto")
    @PreAuthorize("hasRole('GERENTE')")
    public String formDesconto(@PathVariable String id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
        model.addAttribute("pedido", pedido);
        model.addAttribute("content", "pedidos/desconto :: content");
        return "layout";
    }

    @PostMapping("/{id}/desconto")
    @PreAuthorize("hasRole('GERENTE')")
    public String aplicarDesconto(@PathVariable String id,
                                  @RequestParam Double percentual,
                                  RedirectAttributes redirectAttributes) {
        try {
            pedidoService.aplicarDesconto(id, percentual);
            redirectAttributes.addFlashAttribute("sucesso",
                    String.format("Desconto de %.1f%% aplicado com sucesso.", percentual));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    // ---- API REST (compatibilidade) ----

    @GetMapping("/api")
    @ResponseBody
    public List<Pedido> listarApi() {
        return pedidoService.listarPedidos();
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Pedido> criarApi(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(pedidoService.criarPedido(pedido));
    }

    @PutMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<?> atualizarStatusApi(@PathVariable String id,
                                                @RequestParam String status) {
        try {
            return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/api/{id}/cancelar")
    @ResponseBody
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> cancelarApi(@PathVariable String id,
                                         @RequestParam String motivo) {
        try {
            return ResponseEntity.ok(pedidoService.cancelarPedido(id, motivo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/api/{id}/desconto")
    @ResponseBody
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> descontoApi(@PathVariable String id,
                                          @RequestParam Double percentual) {
        try {
            return ResponseEntity.ok(pedidoService.aplicarDesconto(id, percentual));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
