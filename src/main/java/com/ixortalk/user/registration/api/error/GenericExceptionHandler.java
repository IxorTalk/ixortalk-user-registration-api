/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.user.registration.api.error;

import com.auth0.exception.APIException;
import com.ixortalk.autoconfigure.oauth2.auth0.mgmt.api.Auth0RuntimeException;
import com.ixortalk.user.registration.api.UserRegistrationApiApplication;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice(basePackageClasses = {UserRegistrationApiApplication.class})
public class GenericExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionHandler.class);

    @ExceptionHandler(value = FeignException.class)
    public ResponseEntity handleFeignException(FeignException e) {
        String errorUUID = logError(e);
        return status(e.status()).body("Error - " + errorUUID);
    }

    @ExceptionHandler(value = Auth0RuntimeException.class)
    public ResponseEntity handleAuth0RuntimeException(Auth0RuntimeException e) {
        if (e.getCause() instanceof APIException) {
            APIException apiException = (APIException) e.getCause();

            StringBuilder errorMessage =
                    new StringBuilder()
                            .append("Error - ")
                            .append(logError(e));
            if (apiException.getStatusCode() == BAD_REQUEST.value()) {
                errorMessage
                        .append(": ")
                        .append(apiException.getDescription());
            }

            return status(apiException.getStatusCode()).body(errorMessage.toString());
        } else {
            return handleException(e);
        }
    }


    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handleException(Exception e) {
        String errorUUID = logError(e);
        return badRequest().body("Error - " + errorUUID);
    }

    private static String logError(Exception e) {
        String errorUUID = randomUUID().toString();
        LOGGER.error("Invalid request - {}: {}", errorUUID, e.getMessage(), e);
        return errorUUID;
    }
}