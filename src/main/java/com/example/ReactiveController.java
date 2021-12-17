package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Controller("/strings")
public class ReactiveController {

    private static final Logger LOG = LoggerFactory.getLogger(ReactiveController.class);
    private final PublisherService publisherService;

    public ReactiveController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @Get(processes = MediaType.APPLICATION_JSON_STREAM)
    Flux<StringDTO> getStrings() {
        LOG.info("Received client request for strings");
        return publisherService.getStrings();
    }
}
