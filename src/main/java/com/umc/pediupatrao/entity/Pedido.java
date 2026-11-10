package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;

    private String clienteId;

    // Itens do pedido
    private List<ItemPedido> itens = new ArrayList<>();

    // Status do fluxo: RECEBIDO, EM_PREPARACAO, PRONTO, SAIU_PARA_ENTREGA, RETIRADO, FINALIZADO, CANCELADO
    private String status;

    // Valores calculados no servidor
    private Double valorBruto;    // soma dos itens sem desconto
    private Double desconto;      // percentual (ex: 10.0 = 10%)
    private Double valorTotal;    // valorBruto * (1 - desconto/100)

    // Datas e responsáveis
    private LocalDateTime dataEntrada;    // quando o pedido foi registrado (RECEBIDO)
    private LocalDateTime dataSaida;      // quando saiu para entrega ou foi retirado
    private String responsavelRegistro;   // usuário que criou o pedido
    private String responsavelSaida;      // usuário que registrou a saída

    // Cancelamento
    private String motivoCancelamento;
    private String responsavelCancelamento;
    private LocalDateTime dataCancelamento;

    // Mantido para compatibilidade com dados antigos
    private String tipoPizza;
    private String sabor;
    private int quantidade;
    private LocalDateTime dataPedido;

    // Calcula valorBruto e valorTotal a partir dos itens e desconto
    public void calcularTotal() {
        this.valorBruto = itens == null ? 0.0 :
                itens.stream().mapToDouble(ItemPedido::getSubtotal).sum();
        double pct = (this.desconto != null && this.desconto > 0) ? this.desconto : 0.0;
        this.valorTotal = this.valorBruto * (1.0 - pct / 100.0);
    }

    // ---- Getters e Setters ----

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getValorBruto() { return valorBruto; }
    public void setValorBruto(Double valorBruto) { this.valorBruto = valorBruto; }

    public Double getDesconto() { return desconto; }
    public void setDesconto(Double desconto) { this.desconto = desconto; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

    public String getResponsavelRegistro() { return responsavelRegistro; }
    public void setResponsavelRegistro(String responsavelRegistro) { this.responsavelRegistro = responsavelRegistro; }

    public String getResponsavelSaida() { return responsavelSaida; }
    public void setResponsavelSaida(String responsavelSaida) { this.responsavelSaida = responsavelSaida; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public String getResponsavelCancelamento() { return responsavelCancelamento; }
    public void setResponsavelCancelamento(String responsavelCancelamento) { this.responsavelCancelamento = responsavelCancelamento; }

    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDateTime dataCancelamento) { this.dataCancelamento = dataCancelamento; }

    // Compatibilidade com dados antigos
    public String getTipoPizza() { return tipoPizza; }
    public void setTipoPizza(String tipoPizza) { this.tipoPizza = tipoPizza; }

    public String getSabor() { return sabor; }
    public void setSabor(String sabor) { this.sabor = sabor; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }
}
