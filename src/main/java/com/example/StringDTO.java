package com.example;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record StringDTO(String string) {
}
