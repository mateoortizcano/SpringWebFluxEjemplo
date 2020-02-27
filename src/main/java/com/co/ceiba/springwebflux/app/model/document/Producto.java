package com.co.ceiba.springwebflux.app.model.document;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

	@Id
	private String id;
	@NotEmpty
	private String nombre;
	@NotNull
	private Double precio;
	@Valid
	@NotNull
	private Categoria categoria;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fechaCreacion;
	private String rutaImagen;

	public Producto(String nombre, Double precio, Categoria categoria) {
		this.nombre = nombre;
		this.precio = precio;
		this.categoria = categoria;
	}

}
