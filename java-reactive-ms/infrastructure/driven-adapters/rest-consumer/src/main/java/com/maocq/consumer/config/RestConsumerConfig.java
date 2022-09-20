package com.maocq.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class RestConsumerConfig {

    @Value("${adapter.restconsumer.url}")
    private String url;
    @Value("${adapter.restconsumer.timeout}")
    private int timeout;

    @Value("${adapter.restconsumer.pool}")
    private int poolSize;

    @Bean(name = "noPoolHttp")
    public WebClient getWebClient() {
        return WebClient.builder()
            .baseUrl("http://" + url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .clientConnector(getClientHttpConnector())
            .build();
    }

    @Bean(name = "noPoolHttps")
    public WebClient getWebClientHttps() {
        return WebClient.builder()
                .baseUrl("https://" + url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnector())
                .build();
    }

    @Bean(name = "pool")
    public WebClient getWebClientConnectionPool() {
        return WebClient.builder()
                .baseUrl("https://" + url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnectorConnectionPool())
                .build();
    }

    @Bean(name = "http2")
    public WebClient getWebClientConnectionHttp2() {
        return WebClient.builder()
                .baseUrl("https://" + url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttp2ConnectorConnectionPool())
                .build();
    }

    private ClientHttpConnector getClientHttpConnector() {
        /* IF YO REQUIRE APPEND SSL CERTIFICATE SELF SIGNED
        SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        */

        return new ReactorClientHttpConnector(HttpClient.create(ConnectionProvider.newConnection())
                .compress(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }

    private ClientHttpConnector getClientHttpConnectorConnectionPool() {
        ConnectionProvider provider =
                ConnectionProvider.builder("custom")
                        .maxConnections(poolSize)
                        .pendingAcquireMaxCount(-1)
                        .build();

        return new ReactorClientHttpConnector(HttpClient.create(provider)
                //.secure(sslContextSpec -> sslContextSpec.sslContext(sslContext))
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }

    private ClientHttpConnector getClientHttp2ConnectorConnectionPool() {
        ConnectionProvider provider =
                ConnectionProvider.builder("custom")
                        .maxConnections(poolSize)
                        .pendingAcquireMaxCount(-1)
                        .build();

        return new ReactorClientHttpConnector(HttpClient.create(provider)
                .compress(true)
                .keepAlive(true)                
                .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)
                .secure()    
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));                 
    }
}
