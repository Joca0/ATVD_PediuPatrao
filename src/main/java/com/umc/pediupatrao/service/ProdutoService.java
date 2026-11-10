package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    public java.util.Optional<Produto> buscarPorId(String id) {
        return produtoRepository.findById(id);
    }

    // Método para excluir produto
    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }


    // Método para salvar um novo produto ou atualizar um produto existente
    public Produto salvar(Produto produto) {
        // Se o produto não tem ID (novo produto), salva como novo
        if (produto.getId() == null || produto.getId().isBlank()) {
            produto.setId(null); // Garante que o ID seja nulo para o MongoDB gerar um novo
            return produtoRepository.save(produto);  // Cria um novo produto
        } // Se já tem ID (produto existente), atualiza
        else {
            // Verifica se o produto existe antes de atualizar
            if (produtoRepository.existsById(produto.getId())) {
                return produtoRepository.save(produto);  // Atualiza o produto existente
            } else {
                throw new IllegalArgumentException("Produto não encontrado para atualização.");
            }
        }
    }
}
