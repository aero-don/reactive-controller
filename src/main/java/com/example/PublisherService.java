package com.example;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.util.concurrent.Queues;

@Singleton
public class PublisherService {
    private static final Logger LOG = LoggerFactory.getLogger(PublisherService.class);
    private Sinks.Many<StringDTO> stringSink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);
    private Flux<StringDTO> stringFlux = stringSink.asFlux();

    public final Flux<StringDTO> getStrings() {
        return stringFlux;
    }

    public void publishString(StringDTO stringDTO) {

        stringSink.emitNext(stringDTO,
                (SignalType signalType, Sinks.EmitResult emitResult) -> {
                    if (emitResult != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
                        LOG.error("Failed: stringSink.emitNext(stringDTO): ${stringDTO}, signalType=${signalType}, emitResult=${emitResult}");
                    }
                    return false;
                });

        if (LOG.isInfoEnabled()) {
            LOG.info("Published stringDTO: {}", stringDTO);
        }
    }

    public EmitResult publishComplete() {
        EmitResult emitResult = stringSink.tryEmitComplete();
        stringSink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);
        stringFlux = stringSink.asFlux();
        return emitResult;
    }

}
