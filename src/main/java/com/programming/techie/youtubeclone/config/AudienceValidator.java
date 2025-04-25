package com.programming.techie.youtubeclone.config;

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
     * Validates the audience claim of the given JWT token.
     * <p>
     * This method checks whether the JWT token's audience contains the expected audience value.
     * If the audience claim is valid, the validation is successful. Otherwise, it returns a failure result
     * with an error indicating an invalid audience.
     * </p>
     *
     * @param jwt the JWT token to validate
     * @return {@link OAuth2TokenValidatorResult} representing the validation result. 
     *         A success result if the audience is valid, or a failure result with an error if the audience is invalid.
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid audience for the given token"));
    }
}
