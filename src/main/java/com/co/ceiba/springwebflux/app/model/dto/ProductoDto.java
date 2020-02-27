package com.co.ceiba.springwebflux.app.model.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoDto {

	private String id;
	private String nombre;
	private Double precio;
	private CategoriaDto categoria;
	private Date fechaCreacion;
	private String rutaImagen;

}
