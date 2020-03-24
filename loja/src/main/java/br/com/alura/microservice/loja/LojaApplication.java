package br.com.alura.microservice.loja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.client.RestTemplate;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@SpringBootApplication
@EnableFeignClients
@EnableCircuitBreaker
@EnableResourceServer
public class LojaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LojaApplication.class, args);
	}

	/**
	 * Ensinamos o feign client a pegar o token que ele precisa repassar as demais
	 * requisições. Por padrão, o feign implementa os interceptos que são chamados
	 * pelo spring, nesse caso, ele vai chamar esse interceptor antes de cada
	 * requisição que ele receber.
	 *
	 * @return
	 */
	@Bean
	public RequestInterceptor getInterceptorDeAutenticacao() {
		return new RequestInterceptor() {
			/**
			 * Nesse RequestTemplate, estão os dados do request que o Feign vai fazer.
			 */
			@Override
			public void apply(RequestTemplate template) {
				/*
				 * Recupero os dados do usuário que está logado. Como o service usa o hystrix,
				 * para poder recuperar essa info aqui, eu preciso habilitar o compartilhamento
				 * do contexto de segurança no hystrix, pois como ele está com o pool de
				 * threads, ele vai executar o service numa outra thread, diferente da thread do
				 * controller que recebeu a requisição.
				 */
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				// Se o usuário não estiver autenticado, interrompo o método.
				if (authentication == null) {
					return;
				}

				OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
				// Pegamos o token do usuário.
				String token = details.getTokenValue();
				// Adiciono o token no request que o feign vai fazer.
				template.header("Authorization", "Bearer" + token);

			}
		};
	}

	/**
	 * Adiciona o @Bean para o Spring passar a gerenciar o RestTemplate. A
	 * anotação @LoadBalanced que dá a inteligência para o RestTemplate conseguir
	 * resolver o nome da aplicação lá no Eureka.
	 *
	 * @return
	 */
	@Bean
	@LoadBalanced
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
