package com.co.ceiba.springwebflux.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.co.ceiba.springwebflux.app.model.document.Categoria;

import reactor.core.publisher.Mono;

public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String> {

	Mono<Categoria> findByNombre(String nombre);

}
