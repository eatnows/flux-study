package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

// 통합 테스트
//@SpringBootTest
//@AutoConfigureWebTestClient

@WebFluxTest
public class CustomerControllerTest {

    // webFluxTest를 걸어주면 bean으로 등록됨
    @Autowired
    private WebTestClient webClient;    // 비동기로 http 요청
    @MockBean
    private CustomerRepository customerRepository;

    @Test
    public void 한건찾기_테스트() {
//        Flux<Customer> customer = customerRepository.findAll();
//        customer.subscribe(customer1 -> System.out.println("데이터 : " + customer1));

        // stub -> 행동 지시
        when(customerRepository.findById(1L)).thenReturn(Mono.just(new Customer("Jack", "Bauer")));

        webClient.get().uri("/customer/{id}", 1L)
                .exchange()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Jack")
                .jsonPath("$.lastName").isEqualTo("Bauer");
    }
}
