package br.com.alura.microservice.loja.controller.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class VoucherDTO {

	private Long numero;

	private LocalDate previsaoParaEntrega;

}
