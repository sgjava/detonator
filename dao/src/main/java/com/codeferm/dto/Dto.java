/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.dto;

import java.io.Serializable;

/**
 * Created by DeTOnator on 07-13-2019 21:09:38.
 */
public interface Dto extends Serializable {

    /**
     * Provides a way to get PK generically.
     *
     * @param <K> Key type.
     * @return DTO key.
     */
    <K> K getKey();
}
