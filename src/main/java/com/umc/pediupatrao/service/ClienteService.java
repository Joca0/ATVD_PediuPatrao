package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    public ClienteService(ClienteRepository clienteRepository, AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null || cliente.getId().isBlank()) {
            // Novo cliente
            cliente.setId(null);
            Cliente salvo = clienteRepository.save(cliente);
            auditoriaService.registrar(
                    "CRIACAO_CLIENTE", "Cliente", salvo.getId(),
                    "nome", null, salvo.getNome()
            );
            return salvo;
        } else {
            // Atualização: comparar campos e auditar alterações
            Cliente existente = clienteRepository.findById(cliente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado para atualização."));

            auditarAlteracao(existente.getId(), "nome", existente.getNome(), cliente.getNome());
            auditarAlteracao(existente.getId(), "telefone", existente.getTelefone(), cliente.getTelefone());
            auditarAlteracao(existente.getId(), "logradouro", existente.getLogradouro(), cliente.getLogradouro());
            auditarAlteracao(existente.getId(), "numero", existente.getNumero(), cliente.getNumero());
            auditarAlteracao(existente.getId(), "complemento", existente.getComplemento(), cliente.getComplemento());
            auditarAlteracao(existente.getId(), "bairro", existente.getBairro(), cliente.getBairro());
            auditarAlteracao(existente.getId(), "cidade", existente.getCidade(), cliente.getCidade());
            auditarAlteracao(existente.getId(), "estado", existente.getEstado(), cliente.getEstado());
            auditarAlteracao(existente.getId(), "cep", existente.getCep(), cliente.getCep());

            return clienteRepository.save(cliente);
        }
    }

    public void excluir(String id) {
        clienteRepository.findById(id).ifPresent(c ->
                auditoriaService.registrar(
                        "EXCLUSAO_CLIENTE", "Cliente", id,
                        "nome", c.getNome(), null
                )
        );
        clienteRepository.deleteById(id);
    }

    // Método antigo mantido para compatibilidade
    public Cliente novoCliente(Cliente cliente) {
        return salvar(cliente);
    }

    private void auditarAlteracao(String entidadeId, String campo, String anterior, String novo) {
        if (!Objects.equals(anterior, novo)) {
            auditoriaService.registrar(
                    "ALTERACAO_CLIENTE", "Cliente", entidadeId,
                    campo, anterior, novo
            );
        }
    }
}
