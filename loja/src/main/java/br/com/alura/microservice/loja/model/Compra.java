package br.com.alura.microservice.loja.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Compra {

	@Id
	private Long pedidoId;

	private Integer tempoDePreparo;

	private String enderecoDestino;

}
