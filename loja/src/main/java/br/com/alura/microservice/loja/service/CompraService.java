package br.com.alura.microservice.loja.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.alura.microservice.loja.CompraRepository;
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

	@Autowired
	private CompraRepository compraRepository;

	/**
	 * Adicionando o paramâmetro threadPoolKey, o Hystrix ativa o BulkHead e deixa
	 * um pool de threads para esse método. Por padrão são 10 threads para cada
	 * método que tem o threadsPoolKey. Como aqui temos dois métodos com ele, cada
	 * método recebe 10 threads.
	 *
	 * @param id
	 * @return
	 */
	@HystrixCommand(threadPoolKey = "getByIdThreadPool")
	public Compra getById(Long id) {
		return compraRepository.findById(id).orElse(new Compra());
	}

	/**
	 * O @HystrixCommand habilita o hystrix para fazer todo o gerenciamento do
	 * método, com o circuit breaker e fallback. Com isso ele passa a monitor os
	 * tempos de respostas, executa o método numa thread separada, pois uma outra
	 * thread fica monitorando para ver se está demorando muito para responder, etc.
	 * No fallback, passamos o método que deve ser executado em caso de problema.
	 *
	 * Por default, o Hystrix espera 1 segundo e retorna erro 500.
	 *
	 * @param compra
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "realizaCompraFallback")
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
		compraRepository.save(compraSalva);

		return compraSalva;
	}

	public Compra realizaCompraFallback(CompraDTO compra) {
		Compra compraFallback = new Compra();
		compraFallback.setEnderecoDestino(compra.getEndereco().toString());
		return compraFallback;
	}

}
