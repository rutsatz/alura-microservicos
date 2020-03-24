package br.com.alura.microservice.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;

/**
 * Configuração relacionada ao Spring Cloud Oauth2.
 *
 * @author rafael.rutsatz
 *
 */
@Configuration
public class AuthorizationServerConfigurer extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService detailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Configura os clientes que podem acessar o servidor de autenticação e se
	 * logarem.
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()/**/
				.withClient("loja")/**/
				.secret(passwordEncoder.encode("lojapwd"))/**/
				.authorizedGrantTypes("password")/**/
				.scopes("web", "mobile");
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(authenticationManager)
				/**/
				.userDetailsService(detailsService);
	}

}
