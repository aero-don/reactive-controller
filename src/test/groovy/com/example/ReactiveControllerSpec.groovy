package com.example

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
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

class ReactiveControllerSpec extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(ReactiveControllerSpec.class)

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ['spec.name': 'ReactiveControllerSpec'])

    @Shared
    ApplicationContext context = embeddedServer.applicationContext

    @Shared
    ReactiveClient reactiveClient = context.createBean(ReactiveClient);

    @Shared
    ExecutorService executorService = context.getBean(ExecutorService, Qualifiers.byName(TaskExecutors.IO));

    void 'test it works'() {
        expect:
        embeddedServer.running
    }

    void 'test reactive read'() {
        when:
        Flux<StringDTO> stringSource = reactiveClient.strings
        stringSource
                .subscribeOn(Schedulers.fromExecutorService(executorService))
                .doOnNext(stringDTO -> LOG.info("Received {}", stringDTO))
                .subscribe()

        then:
        StepVerifier
                .create(stringSource)
                .thenAwait(Duration.ofSeconds(2))
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 1')
                .thenAwait(Duration.ofSeconds(2))
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 2')
                .thenAwait(Duration.ofSeconds(2))
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 3')
                .thenAwait(Duration.ofSeconds(2))
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 4')
                .thenAwait(Duration.ofSeconds(2))
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 5')
                .thenAwait(Duration.ofSeconds(2))
                .expectNextMatches(stringDTO -> stringDTO.string() == 'String Number: 6')
                .thenAwait(Duration.ofSeconds(2))
                .verifyComplete()
    }

}
