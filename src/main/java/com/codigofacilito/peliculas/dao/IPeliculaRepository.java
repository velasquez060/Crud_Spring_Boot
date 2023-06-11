package com.codigofacilito.peliculas.dao;


import org.springframework.data.repository.CrudRepository;

import com.codigofacilito.peliculas.entities.Pelicula;

public interface IPeliculaRepository extends CrudRepository<Pelicula, Long>{
	
}
