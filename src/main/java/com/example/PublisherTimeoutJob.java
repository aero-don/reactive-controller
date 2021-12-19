package com.example;

public class PublisherTimeoutJob implements Runnable {
    private static long stringNumber = 1;
    private final PublisherService publisherService;

    PublisherTimeoutJob(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @Override
    public void run() {
        // Publish the 6 times, then stop publishing to cause the client to issue a read timeout error.
        if (stringNumber < 7) {
            publisherService.publishString(new StringDTO("String Number: " + stringNumber++));
        }
    }
}
