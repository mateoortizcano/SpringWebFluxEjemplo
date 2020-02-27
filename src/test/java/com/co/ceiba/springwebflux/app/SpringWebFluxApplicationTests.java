package com.co.ceiba.springwebflux.app;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.co.ceiba.springwebflux.app.model.document.Categoria;
import com.co.ceiba.springwebflux.app.model.document.Producto;
import com.co.ceiba.springwebflux.app.service.ServicioProducto;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringWebFluxApplicationTests {

	@Value("${config.base.endpoint}")
	private String uriBase;

	private static final String ID_PARAM = "{id}";

	@Autowired
	private WebTestClient cliente;

	@Autowired
	private ServicioProducto servicio;

	@Test
	public void listarTest() {
		cliente.get().uri(uriBase).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBodyList(Producto.class).consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
					Assertions.assertThat(productos.size() > 0).isTrue();
				});
//				.hasSize(7);
	}

	@Test
	public void verTest() {
		Producto producto = this.servicio.litarPorNombre("producto1").block();
		cliente.get().uri(uriBase + ID_PARAM, Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody(Producto.class).consumeWith(response -> {
					Producto prod = response.getResponseBody();
					Assertions.assertThat(prod.getNombre().equals(producto.getNombre()));
				});
//				.expectBody().jsonPath("$.id").isNotEmpty().jsonPath("$.nombre").isEqualTo(producto.getNombre());
	}

	@Test
	public void crearTest() {
		Categoria categoria = this.servicio.buscarCategoriaPorNombre("Categoria").block();
		Producto producto = new Producto("productoTest", 700.4, categoria);
		cliente.post().uri(uriBase).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto), Producto.class).exchange().expectStatus().isCreated().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.nombre").isNotEmpty().jsonPath("$.id")
				.isNotEmpty().jsonPath("$.categoria.nombre").isNotEmpty().jsonPath("$.nombre")
				.isEqualTo(producto.getNombre()).jsonPath("$.categoria.nombre")
				.isEqualTo(producto.getCategoria().getNombre());
	}

	@Test
	public void editarTest() {
		Categoria categoria = this.servicio.buscarCategoriaPorNombre("Categoria").block();
		Producto productoEditado = new Producto("productoTest", 700.4, categoria);
		Producto productoDesdeBD = this.servicio.litarPorNombre("producto2").block();
		cliente.put().uri(uriBase + ID_PARAM, Collections.singletonMap("id", productoDesdeBD.getId()))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productoEditado), Producto.class).exchange().expectStatus().isCreated().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.nombre").isNotEmpty().jsonPath("$.id")
				.isNotEmpty().jsonPath("$.categoria.nombre").isNotEmpty().jsonPath("$.nombre")
				.isEqualTo(productoEditado.getNombre()).jsonPath("$.categoria.nombre")
				.isEqualTo(productoEditado.getCategoria().getNombre());

	}

	@Test
	public void eliminarTest() {
		Producto productoDesdeBD = this.servicio.litarPorNombre("producto3").block();
		cliente.delete().uri(uriBase + ID_PARAM, Collections.singletonMap("id", productoDesdeBD.getId())).exchange()
				.expectStatus().isNoContent().expectBody().isEmpty();

		cliente.get().uri(uriBase + ID_PARAM, Collections.singletonMap("id", productoDesdeBD.getId())).exchange()
				.expectStatus().isNotFound();
	}

}
