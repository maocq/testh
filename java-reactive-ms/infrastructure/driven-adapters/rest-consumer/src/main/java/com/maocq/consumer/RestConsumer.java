package com.maocq.consumer;

import com.maocq.model.hello.gateways.HelloRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RestConsumer implements HelloRepository {

    private final WebClient clientNPHttp;
    private final WebClient clientNPHttps;
    private final WebClient clientPool;

    public RestConsumer(
            @Qualifier("noPoolHttp") WebClient clientNPHttp,
            @Qualifier("noPoolHttps") WebClient clientNPHttps,
            @Qualifier("pool")  WebClient clientPool) {
        this.clientNPHttp = clientNPHttp;
        this.clientNPHttps = clientNPHttps;
        this.clientPool = clientPool;
    }
    @Override
    public Mono<String> helloHttp(int latency) {
        return clientNPHttp
                .get()
                .uri("/{latency}", latency)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> helloHttps(int latency) {
        return clientNPHttps
                .get()
                .uri("/{latency}", latency)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> helloConnectionPoolHttp1(int latency) {
        return clientPool
                .get()
                .uri("/{latency}", latency)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> helloConnectionPoolHttp2(int latency) {
        return clientPool
                .get()
                .uri("/{latency}", latency)
                .retrieve()
                .bodyToMono(String.class);
    }
}