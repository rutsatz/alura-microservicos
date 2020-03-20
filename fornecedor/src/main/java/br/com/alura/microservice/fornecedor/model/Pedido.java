package br.com.alura.microservice.fornecedor.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import br.com.alura.microservice.fornecedor.dto.PedidoStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer tempoDePreparo;

	@Enumerated(EnumType.STRING)
	private PedidoStatus status;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "pedidoId")
	private List<PedidoItem> itens;

	public Pedido(List<PedidoItem> itens) {
		this.itens = itens;
		this.status = PedidoStatus.RECEBIDO;
	}

}
