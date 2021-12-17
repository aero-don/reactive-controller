package com.example;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Singleton
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final PublisherService publisherService;
    private final TaskScheduler taskScheduler;

    public Application(PublisherService publisherService,
                TaskScheduler taskScheduler) {
        this.publisherService = publisherService;
        this.taskScheduler = taskScheduler;
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

    @EventListener
    void startup(StartupEvent startupEvent) {
        // Start publisher job
        ScheduledFuture<PublisherJob> publisherJobFuture = (ScheduledFuture<PublisherJob>) taskScheduler.scheduleAtFixedRate(
                Duration.ofMillis(1000L),
                Duration.ofMillis(2000L),
                new PublisherJob(publisherService));

        if (publisherJobFuture != null) {
            LOG.info("started PublisherJob");
        } else {
            LOG.error("publisherJobFuture is null");
        }
    }

}
