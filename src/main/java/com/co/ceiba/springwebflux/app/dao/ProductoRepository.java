package com.co.ceiba.springwebflux.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.co.ceiba.springwebflux.app.model.document.Producto;

import reactor.core.publisher.Mono;

public interface ProductoRepository extends ReactiveMongoRepository<Producto, String> {

	public Mono<Producto> findByNombre(String nombre);

}
