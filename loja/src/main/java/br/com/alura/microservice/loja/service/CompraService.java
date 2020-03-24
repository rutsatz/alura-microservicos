package br.com.alura.microservice.loja.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.alura.microservice.loja.client.FornecedorClient;
import br.com.alura.microservice.loja.client.TransportadorClient;
import br.com.alura.microservice.loja.controller.dto.CompraDTO;
import br.com.alura.microservice.loja.controller.dto.InfoEntregaDTO;
import br.com.alura.microservice.loja.controller.dto.InfoFornecedorDTO;
import br.com.alura.microservice.loja.controller.dto.InfoPedidoDTO;
import br.com.alura.microservice.loja.controller.dto.VoucherDTO;
import br.com.alura.microservice.loja.model.Compra;
import br.com.alura.microservice.loja.model.CompraState;
import br.com.alura.microservice.loja.repository.CompraRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CompraService {

//	@Autowired
//	private RestTemplate client;

	@Autowired
	private FornecedorClient fornecedorClient;

	@Autowired
	private TransportadorClient transportadorClient;

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

	public Compra reprocessaCompra(Long id) {
		return null;
	}

	public Compra cancelaCompra(Long id) {
		return null;
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
	 * Ele também captura as exceções do método. Então se o método lançar alguma
	 * exceção, ele não vai retornar aquele erro do Spring, o Hystrix é que vai
	 * capturar essa exception e chamar o método de fallback. Quando ele chama o
	 * método de fallback, o parâmetro que é recebido no método original, também é
	 * enviado para o método de fallback.
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

		// Salva os dados do pedido recebido
		Compra compraSalva = new Compra();
		compraSalva.setState(CompraState.RECEBIDO);
		compraSalva.setEnderecoDestino(compra.getEndereco().toString());
		compraRepository.save(compraSalva);
		// Salva o ID para caso caior no fallback.
		compra.setCompraId(compraSalva.getId());

		// Salva os dados do pedido realizado.
		log.info("buscando informações do fornecedor de {}", estado);
		InfoFornecedorDTO info = fornecedorClient.getInfoPorEstado(estado);
		log.info("realizando um pedido");
		InfoPedidoDTO pedido = fornecedorClient.realizaPedido(compra.getItens());
		compraSalva.setState(CompraState.PEDIDO_REALIZADO);

		compraSalva.setPedidoId(pedido.getId());
		compraSalva.setTempoDePreparo(pedido.getTempoDePreparo());
		compraRepository.save(compraSalva);

		// Salva os dados da entrega realizada.
		InfoEntregaDTO entregaDTO = new InfoEntregaDTO();
		entregaDTO.setPedidoId(pedido.getId());
		entregaDTO.setDataParaEntrega(LocalDate.now().plusDays(pedido.getTempoDePreparo()));
		entregaDTO.setEnderecoOrigem(info.getEndereco());
		entregaDTO.setEnderecoDestino(compra.getEndereco().toString());
		VoucherDTO voucher = transportadorClient.reservaEntrega(entregaDTO);
		compraSalva.setState(CompraState.RESERVA_ENTREGA_REALIZADA);
		compraSalva.setDataParaEntrega(voucher.getPrevisaoParaEntrega());
		compraSalva.setVoucher(voucher.getNumero());
		compraRepository.save(compraSalva);

		return compraSalva;
	}

	public Compra realizaCompraFallback(CompraDTO compra) {
		// Se deu problema no meio, ele já vai ter o ID e retorna esses dados.
		if (compra.getCompraId() != null) {
			return compraRepository.findById(compra.getCompraId()).get();
		}

		Compra compraFallback = new Compra();
		compraFallback.setEnderecoDestino(compra.getEndereco().toString());
		return compraFallback;
	}

}
