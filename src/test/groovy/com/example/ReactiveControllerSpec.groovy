package com.example

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledFuture

class ReactiveControllerSpec extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(ReactiveControllerSpec.class)

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ['spec.name': 'ReactiveControllerSpec']) as EmbeddedServer

    @Shared
    ApplicationContext context = embeddedServer.applicationContext

    @Shared
    ExecutorService executorService = context.getBean(ExecutorService, Qualifiers.byName(TaskExecutors.IO))

    @Shared
    TaskScheduler taskScheduler = context.getBean(TaskScheduler)

    @Shared
    PublisherService publisherService = context.getBean(PublisherService)

    void 'test it works'() {
        expect:
        embeddedServer.running
    }

    void 'test reactive read'() {
        setup:
        // Start publisher job
        ScheduledFuture<PublisherJob> publisherJobFuture = (ScheduledFuture<PublisherJob>) taskScheduler.scheduleAtFixedRate(
                Duration.ofMillis(1000L),
                Duration.ofMillis(2000L),
                new PublisherJob(publisherService))

        if (publisherJobFuture != null) {
            LOG.info("started PublisherJob")
        } else {
            LOG.error("publisherJobFuture is null")
        }

        when:
        ReactiveClient reactiveClient = context.createBean(ReactiveClient)
        Flux<StringDTO> stringSource = reactiveClient.strings
        stringSource
                .subscribeOn(Schedulers.fromExecutorService(executorService))
                .doOnNext(stringDTO -> LOG.info("Received ${stringDTO}", stringDTO))
                .doOnComplete(() -> LOG.info("Received onComplete"))
                .doOnError(throwable -> LOG.error("Error ${throwable.message}"))
                .onBackpressureLatest()
                .subscribe()

        then:
        StepVerifier
                .create(stringSource)
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 1')
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 2')
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 3')
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 4')
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 5')
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 6')
                .expectComplete()
                .verify()

        cleanup:
        publisherService.publishComplete()
    }
}
