package com.co.ceiba.springwebflux.app.handler;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.co.ceiba.springwebflux.app.model.document.Categoria;
import com.co.ceiba.springwebflux.app.model.document.Producto;
import com.co.ceiba.springwebflux.app.service.ServicioProducto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Value("${config.base.endpoint}")
	private  String uriBase;
	@Autowired
	private ServicioProducto servicio;
	@Autowired
	private Validator validator;
	@Value("${config.uploads.path}")
	private String rutaImagen;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(servicio.listar(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return this.servicio.listarPorId(id).flatMap(producto -> ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(producto)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		return producto.flatMap(prod -> {
			Errors errors = new BeanPropertyBindingResult(prod, Producto.class.getName());
			validator.validate(prod, errors);
			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(error -> "El campo " + error.getField() + " " + error.getDefaultMessage()).collectList()
						.flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromValue(list)));
			} else {
				if (prod.getFechaCreacion() == null) {
					prod.setFechaCreacion(new Date());
				}
				return this.servicio.guardar(prod)
						.flatMap(prodDB -> ServerResponse.created(URI.create(uriBase.concat(prodDB.getId())))
								.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(prodDB)));
			}
		});
	}

	public Mono<ServerResponse> actualizar(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");

		return this.servicio.listarPorId(id).zipWith(producto, (db, req) -> {
			db.setCategoria(req.getCategoria());
			db.setPrecio(req.getPrecio());
			db.setNombre(req.getNombre());
			return db;
		}).flatMap(prod -> ServerResponse.created(URI.create(uriBase.concat(prod.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(servicio.guardar(prod), Producto.class))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");
		return this.servicio.listarPorId(id)
				.flatMap(producto -> this.servicio.eliminar(producto).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> cargarImagen(ServerRequest request) {
		String id = request.pathVariable("id");
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> servicio.listarPorId(id).flatMap(p -> {

					p.setRutaImagen(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
					return file.transferTo(new File(rutaImagen + p.getRutaImagen())).then(servicio.guardar(p));
				}))
				.flatMap(p -> ServerResponse.created(URI.create(uriBase.concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crearCompleto(ServerRequest request) {
		Mono<Producto> producto = request.multipartData().map(multipart -> {
			FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

			Categoria categoria = new Categoria(categoriaNombre.value());
			categoria.setId(categoriaId.value());
			return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
		});

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> producto.flatMap(p -> {

					p.setRutaImagen(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "-").replace(":", "").replace("\\", ""));

					p.setFechaCreacion(new Date());

					return file.transferTo(new File(rutaImagen + p.getRutaImagen())).then(servicio.guardar(p));
				})).flatMap(p -> ServerResponse.created(URI.create(uriBase.concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(p)));
	}
}
