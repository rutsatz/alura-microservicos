package br.com.alura.microservice.loja.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.alura.microservice.loja.controller.dto.InfoFornecedorDTO;
import br.com.alura.microservice.loja.controller.dto.InfoPedidoDTO;
import br.com.alura.microservice.loja.controller.dto.ItemDaCompraDTO;

/**
 * Para o @FeignClient passamos o id da aplicação que queremos acessar. E na
 * interface implementamos os serviços que queremos acessar.
 *
 * @author rafael.rutsatz
 *
 */
@FeignClient("fornecedor")
public interface FornecedorClient {

	@RequestMapping("/info/{estado}")
	InfoFornecedorDTO getInfoPorEstado(@PathVariable String estado);

	@PostMapping("/pedido")
	InfoPedidoDTO realizaPedido(List<ItemDaCompraDTO> itens);

}
