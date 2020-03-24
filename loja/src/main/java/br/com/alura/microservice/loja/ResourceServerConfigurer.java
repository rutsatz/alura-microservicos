package br.com.alura.microservice.loja;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * Classe que configura o security para proteger o serviço.
 *
 * @author rafael.rutsatz
 *
 */
@Configuration
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

	/**
	 * Qualquer requisição que for feita para o fornecedor, o usuário precisa estar
	 * autenticado.
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		/* Sobre os requests que vão receber autorização, */
		http.authorizeRequests()
//				/* todos os requests */
//				.anyRequest()
//				/* o usuário precisa estar autenticado */
//				.authenticated();
				/*
				 * Para consultar pedidos, eu não preciso estar autenticado. Preciso estar
				 * autenticado somente quando for realizar a compra, ou seja, fazer um pedido.
				 * Por isso, somente peço autorização para o /pedido.
				 */
				.antMatchers(HttpMethod.POST, "/compra")
				/* E ele também precisa ter a permissão USER. */
				.hasRole("USER");
	}

}
