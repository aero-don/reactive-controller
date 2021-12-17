package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import reactor.core.publisher.Flux;

@Client("http://localhost:8080")
public interface ReactiveClient {
    @Get(uri = "/strings", processes = MediaType.APPLICATION_JSON_STREAM)
    Flux<StringDTO> getStrings();
}
