package com.co.ceiba.springwebflux.app.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.co.ceiba.springwebflux.app.model.dto.ProductoDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoClienteService {

	private static final String ID_PARAM = "/{id}";
	@Autowired
	private WebClient webClient;

	public Flux<ProductoDto> listar() {
		return webClient.get().accept(MediaType.APPLICATION_JSON).exchange()
				.flatMapMany(response -> response.bodyToFlux(ProductoDto.class));
	}

	public Mono<ProductoDto> listarPorId(String id) {
		Map<String, String> parametrosUrl = Collections.singletonMap("id", id);
		return webClient.get().uri(ID_PARAM, parametrosUrl).accept(MediaType.APPLICATION_JSON)
//				.exchange().flatMap(response -> response.bodyToMono(ProductoDto.class));
				.retrieve().bodyToMono(ProductoDto.class);
	}

	public Mono<ProductoDto> crear(ProductoDto producto) {
		return webClient.post().accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(producto).retrieve().bodyToMono(ProductoDto.class);
	}

	public Mono<ProductoDto> modificar(ProductoDto producto, String id) {
		Map<String, String> parametrosUrl = Collections.singletonMap("id", id);
		return webClient.put().uri(ID_PARAM, parametrosUrl).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).bodyValue(producto).retrieve().bodyToMono(ProductoDto.class);
	}

	public Mono<Void> eliminar(String id) {
		Map<String, String> parametrosUrl = Collections.singletonMap("id", id);
		return webClient.delete().uri(ID_PARAM, parametrosUrl).exchange().then();
	}
}
