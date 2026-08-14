package com.openclassrooms.starterjwt.testsupport;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.util.function.Consumer;

public final class ParameterizedValidationSupport {

    private ParameterizedValidationSupport() {
    }

    public static <T> Arguments invalidCase(String name, Consumer<T> mutation) {
        return Arguments.of(Named.of(name, mutation));
    }
}
