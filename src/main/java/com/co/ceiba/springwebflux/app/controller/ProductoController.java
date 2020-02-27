package com.co.ceiba.springwebflux.app.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.co.ceiba.springwebflux.app.model.document.Producto;
import com.co.ceiba.springwebflux.app.service.ServicioProducto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

	private static final String API_V1_PRODUCTOS = "/api/v1/productos";
	@Autowired
	private ServicioProducto servicioProducto;
	@Value("${config.uploads.path}")
	private String rutaImagen;

	@GetMapping
	public Mono<ResponseEntity<Flux<Producto>>> listar() {
		return Mono
				.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(this.servicioProducto.listar()));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> listarPorId(@PathVariable String id) {
		return servicioProducto.listarPorId(id)
				.map(producto -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(producto))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/v2")
	public Mono<ResponseEntity<Map<String, Object>>> guardar(@Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		return monoProducto.flatMap(producto -> {
			if (producto.getFechaCreacion() == null) {
				producto.setFechaCreacion(new Date());
			}

			return this.servicioProducto.guardar(producto).map(product -> {
				respuesta.put("producto", product);
				return ResponseEntity.created(URI.create(API_V1_PRODUCTOS.concat(product.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(respuesta);
			});
		}).onErrorResume(error -> Mono.just(error).cast(WebExchangeBindException.class)
				.flatMap(e -> Mono.just(e.getFieldErrors())).flatMapMany(Flux::fromIterable)
				.map(campoError -> "El campo " + campoError.getField() + " " + campoError.getDefaultMessage())
				.collectList().flatMap(lista -> {
					respuesta.put("errors", lista);
					return Mono.just(ResponseEntity.badRequest().body(respuesta));
				}));

	}

	@PostMapping
	public Mono<ResponseEntity<Producto>> guardar(@RequestBody Producto producto) {
		if (producto.getFechaCreacion() == null) {
			producto.setFechaCreacion(new Date());
		}

		return this.servicioProducto.guardar(producto)
				.map(product -> ResponseEntity.created(URI.create(API_V1_PRODUCTOS.concat(product.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(product));
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Producto>> actualizar(@RequestBody Producto producto, @PathVariable String id) {
		return this.servicioProducto.listarPorId(id).flatMap(product -> {
			product.setCategoria(producto.getCategoria());
			product.setId(id);
			product.setNombre(producto.getNombre());
			product.setPrecio(producto.getPrecio());
			return this.servicioProducto.guardar(product);
		}).map(product -> ResponseEntity.created(URI.create(API_V1_PRODUCTOS.concat(product.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(product))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
		return this.servicioProducto.listarPorId(id)
				.flatMap(prodct -> this.servicioProducto.eliminar(prodct)
						.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart archivoImagen) {
		return this.servicioProducto.listarPorId(id).flatMap(prod -> {
			prod.setRutaImagen(UUID.randomUUID().toString() + "-"
					+ archivoImagen.filename().replace(" ", "").replace(":", "").replace("\\", ""));
			return archivoImagen.transferTo(new File(rutaImagen + prod.getRutaImagen()))
					.then(this.servicioProducto.guardar(prod));
		}).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
	}

}
