package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final Sinks.Many<Customer> sink;

    // A 요청 -> Flux -> Stream
    // B 요청 -> Flux -> Stream
    // -> Flux.merge -> sink

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        // mutlication : 새로 추가된 데이터만 구독자에게 푸시하는 방식
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        // just : 데이터를 순차적으로 꺼내서 onNext하면서 던져준다.
        return Flux.just(1, 2, 3, 4, 5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> fluxstream() {
        // just : 데이터를 순차적으로 꺼내서 onNext하면서 던져준다.
        return Flux.just(1, 2, 3, 4, 5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/customer", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Customer> findAll() {
        return customerRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer/{id}")
    // 데이터가 한건이면 Mono 여러건이면 Flux
    public Mono<Customer> findById(@PathVariable Long id) {
        return customerRepository.findById(id).log();
    }

    // produces = MediaType.TEXT_EVENT_STREAM_VALUE 생략 가능 ServerSentEvent 하면 자동으로 걸린다.
    @GetMapping(value = "/customer/sse")
    public Flux<ServerSentEvent<Customer>> findAllSSE() {
        return sink.asFlux().map(customer -> ServerSentEvent.builder(customer).build()).doOnCancel(() -> {
            // 브라우저에서 연결을 끊어도 다시 접속했을 경우 연결할 수 있다.
            sink.asFlux().blockLast();
        });
    }

    @PostMapping(value = "/customer")
    public Mono<Customer> save() {
        return customerRepository.save(new Customer("gildong", "Hong")).doOnNext(customer -> {
            sink.tryEmitNext(customer);
        });
    }
}
