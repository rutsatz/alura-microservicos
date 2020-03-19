package br.com.alura.microservice.loja.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.alura.microservice.loja.controller.dto.CompraDTO;
import br.com.alura.microservice.loja.controller.dto.InfoFornecedorDTO;

@Service
public class CompraService {

	@Autowired
	private RestTemplate client;

	public void realizaCompra(CompraDTO compra) {

		// Coloca o nome da aplicação (fornecedor), que o Ribbon vai resolver esse nome,
		// para não precisar usar o ip e porta hardcoded. O Ribbon é integrada com o
		// Eureka, então ele consulta o eureka para ver os serviços disponíveis e pega o
		// serviço que tenha o nome fornecedor.
		ResponseEntity<InfoFornecedorDTO> exchange = client.exchange(
				"http://fornecedor/info/" + compra.getEndereco().getEstado(), HttpMethod.GET, null,
				InfoFornecedorDTO.class);

		System.out.println(exchange.getBody().getEndereco());

	}

}
