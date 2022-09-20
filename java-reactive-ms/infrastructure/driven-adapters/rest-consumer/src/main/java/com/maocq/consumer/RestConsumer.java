package com.maocq.consumer;

import com.maocq.model.hello.gateways.HelloRepository;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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

        var rc = new ReactorClientHttpConnector(HttpClient.create()
            .protocol(HttpProtocol.H2) 
            .secure()
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(90, MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(90, MILLISECONDS));
            }));
            

        var cc = WebClient.builder()
                .baseUrl("https://n1.apidevopsteam.xyz")
                .clientConnector(rc)
                .build();

        return cc
                .get()
                .uri("/{latency}", latency)
                .exchangeToMono(responseHandler -> {
                    return responseHandler.bodyToMono(String.class);
                });
    
    }
}