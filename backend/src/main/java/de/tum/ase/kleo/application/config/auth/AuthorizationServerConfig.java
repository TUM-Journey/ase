package de.tum.ase.kleo.application.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import de.tum.ase.kleo.application.auth.ReferenceOAuth2RequestValidator;
import de.tum.ase.kleo.application.auth.UserPrincipalAuthenticationConverter;
import lombok.val;


@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${security.jwt.signingKey}")
    private String jwtSigningKey;

    @Value("${security.accessTokenValidity:3600}")
    private int accessTokenValidity;

    @Value("${security.oauth2.clientId}")
    private String oauth2ClientId;
    @Value("${security.oauth2.grandTypes}")
    private String[] oauth2GrandTypes;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.tokenKeyAccess("permitAll()")
                .allowFormAuthenticationForClients()
                .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter())
                .requestValidator(new ReferenceOAuth2RequestValidator())
                .authenticationManager(authenticationManager);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(oauth2ClientId)
                .authorizedGrantTypes(oauth2GrandTypes)
                .accessTokenValiditySeconds(accessTokenValidity);
    }

    @Bean
    TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    JwtAccessTokenConverter accessTokenConverter() {
        val internalAccessTokenConverter = new DefaultAccessTokenConverter();
        internalAccessTokenConverter.setUserTokenConverter(userPrincipleAuthenticationConverter());

        val converter = new JwtAccessTokenConverter();
        converter.setAccessTokenConverter(internalAccessTokenConverter);
        converter.setSigningKey(jwtSigningKey);
        return converter;
    }

    @Bean
    @Primary
    DefaultTokenServices tokenServices() {
        val defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    @Bean
    UserAuthenticationConverter userPrincipleAuthenticationConverter() {
        return new UserPrincipalAuthenticationConverter();
    }
}
