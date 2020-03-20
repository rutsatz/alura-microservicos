package br.com.alura.microservice.loja.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.alura.microservice.loja.client.FornecedorClient;
import br.com.alura.microservice.loja.controller.dto.CompraDTO;
import br.com.alura.microservice.loja.controller.dto.InfoFornecedorDTO;
import br.com.alura.microservice.loja.controller.dto.InfoPedidoDTO;
import br.com.alura.microservice.loja.model.Compra;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CompraService {

//	@Autowired
//	private RestTemplate client;

	@Autowired
	private FornecedorClient fornecedorClient;

	public Compra realizaCompra(CompraDTO compra) {

		String estado = compra.getEndereco().getEstado();
		// Coloca o nome da aplicação (fornecedor), que o Ribbon vai resolver esse nome,
		// para não precisar usar o ip e porta hardcoded. O Ribbon é integrada com o
		// Eureka, então ele consulta o eureka para ver os serviços disponíveis e pega o
		// serviço que tenha o nome fornecedor.
//		ResponseEntity<InfoFornecedorDTO> exchange = client.exchange(
//				"http://fornecedor/info/" + compra.getEndereco().getEstado(), HttpMethod.GET, null,
//				InfoFornecedorDTO.class);

		log.info("buscando informações do fornecedor de {}", estado);
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(estado);

		log.info("realizando um pedido");
		InfoPedidoDTO pedido = fornecedorClient.realizaPedido(compra.getItens());

		Compra compraSalva = new Compra();
		compraSalva.setPedidoId(pedido.getId());
		compraSalva.setTempoDePreparo(pedido.getTempoDePreparo());
		compraSalva.setEnderecoDestino(compra.getEndereco().toString());

		return compraSalva;
	}

}
