package com.co.ceiba.springwebflux.app.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.co.ceiba.springwebflux.app.dao.CategoriaRepository;
import com.co.ceiba.springwebflux.app.dao.ProductoRepository;
import com.co.ceiba.springwebflux.app.model.document.Categoria;
import com.co.ceiba.springwebflux.app.model.document.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ServicioProducto {

	@Autowired
	public ProductoRepository productoRepository;
	@Autowired
	public CategoriaRepository categoriaRepository;
	public static final Logger logger = LoggerFactory.getLogger(ServicioProducto.class);

	public void insertarProductos() {
		Categoria categoria = new Categoria("Categoria");
		Categoria categoria2 = new Categoria("Categoria2");
		Categoria categoria3 = new Categoria("Categoria3");
		Categoria categoria4 = new Categoria("Categoria4");
		Flux.just(categoria, categoria2, categoria3, categoria4).flatMap(categoriaRepository::save)
				.thenMany(Flux.just(new Producto("producto1", 1.2345, categoria2),
						new Producto("producto2", 2.2345, categoria3), new Producto("producto3", 3.2345, categoria),
						new Producto("producto4", 4.2345, categoria3), new Producto("producto5", 5.2345, categoria4),
						new Producto("producto6", 6.2345, categoria), new Producto("producto7", 7.2345, categoria2))
						.flatMap(producto -> {
							producto.setFechaCreacion(new Date());
							return productoRepository.save(producto);
						}))
				.subscribe();
	}

	public Flux<Producto> listar() {
		Flux<Producto> productos = productoRepository.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});
		productos.subscribe(producto -> logger.info(producto.getNombre()));
		return productos;
	}

	public Mono<Producto> listarPorId(String id) {
		return this.productoRepository.findById(id);
	}

	public Mono<Producto> guardar(Producto producto) {
		return this.productoRepository.save(producto);
	}
	
	public Mono<Void> eliminar(Producto producto) {
		return this.productoRepository.delete(producto);
	}

	public Mono<Producto> litarPorNombre(String nombre) {
		return this.productoRepository.findByNombre(nombre);
	}

	public Mono<Categoria> buscarCategoriaPorNombre(String nombre) {
		return this.categoriaRepository.findByNombre(nombre);
	}

}
