package com.example;

import reactor.core.publisher.Sinks.EmitResult;

public class PublisherJob implements Runnable {
    private static long stringNumber = 1;
    private final PublisherService publisherService;
    private EmitResult emitResult = null;

    PublisherJob(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @Override
    public void run() {
        if (stringNumber < 7) {
            publisherService.publishString(new StringDTO("String Number: " + stringNumber++));
        } else {
            if (emitResult == null) {
                emitResult = publisherService.publishComplete();
            }
        }
    }
}
