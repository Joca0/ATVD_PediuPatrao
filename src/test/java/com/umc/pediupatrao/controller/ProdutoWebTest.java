package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProdutoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @WithMockUser
    public void deveCarregarListaDeProdutosSemErro500() throws Exception {
        when(produtoService.listarProdutos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attributeExists("produtos"))
                .andExpect(model().attribute("content", "produtos/lista :: content"));
    }

    @Test
    @WithMockUser
    public void deveSalvarProdutoComSucesso() throws Exception {
        mockMvc.perform(post("/produtos/salvar")
                        .param("nome", "Pizza Calabresa")
                        .param("tipo", "Pizza")
                        .param("preco", "45.90")
                        .param("ativo", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("sucesso"));

        verify(produtoService).salvar(any(Produto.class));
    }
}
