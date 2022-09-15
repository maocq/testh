package com.maocq.api;

import com.maocq.model.account.Account;
import com.maocq.usecase.db.DBUseCase;
import com.maocq.usecase.gethello.GetHelloUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {

    private final DBUseCase DBUseCase;
    private final GetHelloUseCase helloUseCase;

    @GetMapping(path = "/hello")
    public String hello() {
        //return "Hello";
        return primes(1000);
    }

    private String primes(int N)
    {
        boolean[] isPrime = new boolean[N + 1];
        for (int i = 2; i <= N; i++)
            isPrime[i] = true;

        for (int i = 2; i*i <= N; i++)
        {
            if (isPrime[i])
            {
                for (int j = i; i*j <= N; j++)
                    isPrime[i*j] = false;
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 2; i <= N; i++)
        {
            if (isPrime[i])
                builder.append(i).append(" ");
        }
        return builder.toString();
    }

    @GetMapping(path = "/get-hello")
    public String getHello(@RequestParam(required = false) Integer latency) {
        var latencyValue = latency == null ? 0 : latency;
        return helloUseCase.hello(latencyValue);
    }

    @GetMapping(path = "/get-hello-pool")
    public String getHelloConnectionPool(@RequestParam(required = false) Integer latency) {
        var latencyValue = latency == null ? 0 : latency;
        return helloUseCase.helloConnectionPool(latencyValue);
    }

    @GetMapping(path = "/db")
    public Optional<Account> db() {
        return DBUseCase.query();
    }
}
