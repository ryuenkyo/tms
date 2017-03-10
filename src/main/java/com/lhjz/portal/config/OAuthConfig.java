package com.lhjz.portal.config;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.vote.ScopeVoter;

@Configuration
@EnableAuthorizationServer
public class OAuthConfig extends WebSecurityConfigurerAdapter implements AuthorizationServerConfigurer {

	@Autowired
	DataSource dataSource;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// @formatter:off
		http.antMatcher("/oauth/**")
			.authorizeRequests()
			.anyRequest()
			.permitAll()
//        	.hasAnyRole("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER")
//        	.antMatchers("/**").anonymous()
//	        .and()
//	        .exceptionHandling().accessDeniedPage("/admin/login?error")
	        .and()
	        .csrf()
//	        .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
	        .disable();
		// @formatter:on
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.expressionHandler(new OAuth2WebSecurityExpressionHandler());
	}

	@Bean(name = "clientDetailsService")
	public ClientDetailsService clientDetailsService() {
		return new JdbcClientDetailsService(dataSource);
	}

	@Bean(name = "tokenStore")
	public TokenStore tokenStore() {
		return new JdbcTokenStore(dataSource);
	}

	@Bean(name = "approvalStore")
	public ApprovalStore approvalStore() {
		return new JdbcApprovalStore(dataSource);
	}

	@Bean(name = "oAuth2RequestFactory")
	public OAuth2RequestFactory oAuth2RequestFactory() {
		return new DefaultOAuth2RequestFactory(clientDetailsService());
	}

	@Bean(name = "oauthUserApprovalHandler")
	public UserApprovalHandler oauthUserApprovalHandler() {
		ApprovalStoreUserApprovalHandler userApprovalHandler = new ApprovalStoreUserApprovalHandler();
		userApprovalHandler.setApprovalStore(approvalStore());
		userApprovalHandler.setClientDetailsService(clientDetailsService());
		userApprovalHandler.setRequestFactory(oAuth2RequestFactory());
		return userApprovalHandler;
	}

	@Bean(name = "jdbcAuthorizationCodeServices")
	public AuthorizationCodeServices jdbcAuthorizationCodeServices() {
		return new JdbcAuthorizationCodeServices(dataSource);
	}

	@Bean(name = "oauth2AuthenticationEntryPoint")
	public OAuth2AuthenticationEntryPoint oauth2AuthenticationEntryPoint() {
		return new OAuth2AuthenticationEntryPoint();
	}

	@Bean(name = "oauth2ClientDetailsUserService")
	public ClientDetailsUserDetailsService oauth2ClientDetailsUserService(ClientDetailsService clientDetailsService) {
		return new ClientDetailsUserDetailsService(clientDetailsService);
	}

	@Bean(name = "oauth2AccessDecisionManager")
	public UnanimousBased oauth2AccessDecisionManager() {
		return new UnanimousBased(Arrays.asList(new ScopeVoter(), new RoleVoter(), new AuthenticatedVoter()));
	}

	@Bean(name = "oauth2AccessDeniedHandler")
	public OAuth2AccessDeniedHandler oauth2AccessDeniedHandler() {
		return new OAuth2AccessDeniedHandler();
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.authenticationEntryPoint(oauth2AuthenticationEntryPoint());
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		// clients.jdbc(dataSource);// .passwordEncoder(passwordEncoder);
		clients.withClientDetails(clientDetailsService());
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.setClientDetailsService(clientDetailsService());

		endpoints.authenticationManager(authenticationManager).approvalStore(approvalStore())
				.authorizationCodeServices(jdbcAuthorizationCodeServices())
				.userApprovalHandler(oauthUserApprovalHandler()).userDetailsService(userDetailsService);
	}

}
