package com.bolsadeideas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.service.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication  implements CommandLineRunner {
	
	@Autowired
	private ProductoService service;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electronico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computacion");
		Categoria mueble = new Categoria("Muebles");

		Flux.just(electronico, deporte, computacion, mueble).flatMap(service::saveCategoria).doOnNext(c -> {
			log.info("Categoria creada: " + c + "Id: " + c.getId());
		}).thenMany(Flux.just(new Producto("TV Samsung", 233.44, electronico),
				new Producto("PC MAC", 833.44, computacion), new Producto("AMPLIFICADOR MARSHAL", 2233.44, electronico),
				new Producto("HELADERA GAFA", 1233.44, electronico),
				new Producto("LAPTOP LENOVO", 3233.44, computacion), new Producto("TV LG", 8233.44, electronico),
				new Producto("MICROONDAS GAFA", 22233.44, electronico)).flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				})).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));

	}
}
