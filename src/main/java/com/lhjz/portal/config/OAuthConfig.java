package com.lhjz.portal.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

@Configuration
public class OAuthConfig {

	@Configuration
	@EnableResourceServer
	protected static class ResourceServer extends ResourceServerConfigurerAdapter {

		private static final String RESOURCE_ID = "tms";

		@Autowired
		DataSource dataSource;

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
	        http.authorizeRequests().antMatchers("/wx/**").authenticated();
	        // @formatter:on
		}

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
			resources.resourceId(RESOURCE_ID);
			resources.tokenStore(tokenStore());
		}

		@Bean
		public TokenStore tokenStore() {
			return new JdbcTokenStore(dataSource);
		}
	}

	@Configuration
	@EnableAuthorizationServer
	protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

		@Autowired
		DataSource dataSource;

		@Autowired
		AuthenticationManager authenticationManager;

		@Bean
		public TokenStore tokenStore() {
			return new JdbcTokenStore(dataSource);
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager);
			endpoints.tokenStore(tokenStore());

			// // 配置TokenServices参数
			// DefaultTokenServices tokenServices = new DefaultTokenServices();
			// tokenServices.setTokenStore(endpoints.getTokenStore());
			// tokenServices.setSupportRefreshToken(false);
			// tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
			// tokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());
			// tokenServices.setAccessTokenValiditySeconds((int)
			// TimeUnit.DAYS.toSeconds(30)); // 30天
			// endpoints.tokenServices(tokenServices);
		}

		// @Bean
		// public ClientDetailsService clientDetails() {
		// return new JdbcClientDetailsService(dataSource);
		// }

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.jdbc(dataSource);
			// clients.withClientDetails(clientDetails());
			// clients.inMemory().withClient("tms").secret("tms").authorizedGrantTypes("authorization_code")
			// .scopes("app");

		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
			// security.checkTokenAccess("isAuthenticated()");
			security.checkTokenAccess("permitAll()");
			security.allowFormAuthenticationForClients();
		}

	}
}
