package br.com.alura.microservice.loja.controller.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class CompraDTO {

	/**
	 * Adicionado somente para pode recuperar dentro do método de fallback.
	 */
	@JsonIgnore
	private Long compraId;

	private List<ItemDaCompraDTO> itens;

	private EnderecoDTO endereco;
}
