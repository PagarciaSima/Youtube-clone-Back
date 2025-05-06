package com.programming.pgs.youtubeclone.config;


import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }
    
    /**
     * Validates the audience of the given JWT token.
     * <p>
     * This method checks if the JWT token's audience contains the expected audience.
     * If it does, the validation is successful, otherwise, it returns an error indicating an invalid audience.
     * </p>
     *
     * @param jwt The JWT token to be validated.
     * @return The result of the validation, which will be either a success or failure based on the audience check.
     * @throws OAuth2Error If the audience does not match the expected audience.
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid audience for the given token"));
    }
}