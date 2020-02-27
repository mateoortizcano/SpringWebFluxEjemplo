package com.co.ceiba.springwebflux.app.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.co.ceiba.springwebflux.app.handler.ProductoHandler;

@Configuration
public class RouterFunctionConfig {

	private static final String ID_PARAM = "{id}";
	@Value("${config.base.endpoint}")
	private String uriBase;

	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
		return route(GET(uriBase).or(GET("/api/v3/productos")), handler::listar)
				.andRoute(GET(uriBase + ID_PARAM), handler::ver).andRoute(POST(uriBase), handler::crear)
				.andRoute(PUT(uriBase + ID_PARAM), handler::actualizar)
				.andRoute(DELETE(uriBase + ID_PARAM), handler::eliminar)
				.andRoute(POST(uriBase + "/upload/{id}"), handler::cargarImagen)
				.andRoute(POST(uriBase + "/v2"), handler::crearCompleto);
	}

}
