package com.codigofacilito.peliculas.controllers;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.resource.transaction.spi.TransactionCoordinatorOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.codigofacilito.peliculas.entities.Actor;
import com.codigofacilito.peliculas.entities.Pelicula;
import com.codigofacilito.peliculas.services.IActorService;
import com.codigofacilito.peliculas.services.IArchivoService;
import com.codigofacilito.peliculas.services.IGeneroService;
import com.codigofacilito.peliculas.services.IPeliculaService;

import jakarta.validation.Valid;

@Controller
public class PeliculasController {
	
	private IPeliculaService service;
	private IGeneroService generoService;
	private IActorService actorservice;
	private IArchivoService archivoService;
	
	public PeliculasController(IPeliculaService service,IGeneroService generoService,IActorService actorservice,IArchivoService archivoService) {
		this.service = service;
		this.generoService = generoService;
		this.actorservice = actorservice;
		this.archivoService = archivoService;
	}
	@GetMapping("pelicula")
	public String crear(Model model) {
		Pelicula pelicula = new Pelicula();
		model.addAttribute("pelicula",pelicula);
		model.addAttribute("Titulo", "Nueva pelicula");
		model.addAttribute("generos",generoService.findAll());
		model.addAttribute("actores",actorservice.findAll());
		return "pelicula";
	}
	
	@GetMapping("pelicula/{id}")
	public String editar(@PathVariable(name = "id") Long id,Model model) {
		Pelicula pelicula = service.findById(id);
		String ids = "";
		
		for (Actor actor : pelicula.getProtagonistas()) {
			if("".equals(ids)) {
				ids = actor.getId().toString();
			}else {
				ids = ids + "," + actor.getId().toString();
			}
		}
		
		model.addAttribute("pelicula",pelicula);
		model.addAttribute("ids",ids);
		model.addAttribute("Titulo", "editar pelicula");
		model.addAttribute("generos",generoService.findAll());
		model.addAttribute("actores",actorservice.findAll());
		return "pelicula";
	}
	
	
	@PostMapping("/pelicula")
	public String guardar(@Valid Pelicula pelicula,BindingResult br,@ModelAttribute(name = "ids") String ids,@RequestParam("archivo") MultipartFile imagen) {
		if(br.hasErrors()) {
			return "pelicula";
		}
		
		
		if(!imagen.isEmpty()) {
			String archivo = pelicula.getNombre() + getExtension(imagen.getOriginalFilename());
			pelicula.setImagen(archivo);
			try {
				archivoService.guardar(archivo, imagen.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			pelicula.setImagen("Toro.jpg");
		}
		
		if(ids != null &&  !"".equals(ids)) {
			List<Long> idsProtagonistasList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
			List<Actor> protagonistas = actorservice.findAllByYd(idsProtagonistasList);
			pelicula.setProtagonistas(protagonistas);
		}
		
		
		service.save(pelicula);
		
		return "redirect:home";
		
	}
	
	private String getExtension(String archivo) {
		return archivo.substring(archivo.lastIndexOf("."));
	}
	
	@GetMapping({"/","/home","index"})
	public String home(Model model,String msj,String tipoMsj) {
		
		model.addAttribute("msj","catalogo actualizado al 2023");
		model.addAttribute("peliculas",service.findAll());
		model.addAttribute("tipoMsj","success");
		return "home";
	}
	
	@GetMapping({"/listado"})
	public String listado(Model model,@RequestParam(required = false) String msj,@RequestParam(required = false) String tipoMsj) {
		model.addAttribute("titulo","listado peliculas");
		model.addAttribute("peliculas",service.findAll());
		
		if(!"".equals(tipoMsj) && !"".equals(msj)) {
			model.addAttribute("msj", msj);
			model.addAttribute("tipoMsj", tipoMsj);
		}
		
		return "listado";
	}
	
	
	@GetMapping("pelicula/{id}/delete")
	public String eliminar(@PathVariable(name = "id") Long id,Model model,RedirectAttributes redirectAttributes) {
		service.delete(id);
		
		redirectAttributes.addAttribute("msj","la pelicula fue eliminada");
		redirectAttributes.addAttribute("tipoMsj","success");
		return "redirect:/listado";
	}
	
	
	
	
	
}
