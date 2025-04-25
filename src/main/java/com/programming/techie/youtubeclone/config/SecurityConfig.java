package com.programming.techie.youtubeclone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Value("${auth0.audience}")
    private String audience;

	@Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .anyRequest().authenticated()
            )
            .sessionManagement(sessionManagement ->
                sessionManagement
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer
                    .jwt()
            );
    }
	
	@Bean
	JwtDecoder jwtDecoder() {
	    // 1. Obtains a JwtDecoder based on the issuer location (OIDC issuer)
	    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);
	    
	    // 2. Creates an audience validator
	    OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);

	    // 3. Creates a default validator that validates the issuer of the token
	    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

	    // 4. Creates a composite validator that validates both the issuer and the audience
	    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

	    // 5. Sets the composite validator on the JwtDecoder
	    jwtDecoder.setJwtValidator(withAudience);

	    return jwtDecoder;
	}


	
	
}
