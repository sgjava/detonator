/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * This class can validate beans using Bean Validation 2.0 (JSR 380).
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class ValidateBean {
    
    /**
     * Bean validator.
     */
    private final Validator validator;    
    /**
     * Default constructor. Initialize validator.
     */
    public ValidateBean() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    /**
     * Throws exception if bean validation fails.
     *
     * @param bean Bean to validate.
     */
    public void valid(final Object bean) {
        var violations = validator.validate(bean);
        if (violations.size() > 0) {
            var message = "";
            // Build exception message
            message = violations.stream().map(violation -> String.format("%s.%s %s | ", violation.getRootBeanClass().
                    getSimpleName(), violation.getPropertyPath(), violation.getMessage())).reduce(message, String::concat);
            // Trim last seperator
            message = message.substring(0, message.length() - 3);
            throw new RuntimeException(String.format("Bean violations: %s", message));
        }
    }    
}
