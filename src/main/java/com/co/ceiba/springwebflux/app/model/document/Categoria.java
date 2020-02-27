package com.co.ceiba.springwebflux.app.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

	@Id
	private String id;
	private String nombre;

	public Categoria(String nombre) {
		this.nombre = nombre;
	}

}
