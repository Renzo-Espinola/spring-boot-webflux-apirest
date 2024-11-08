package com.bolsadeideas.springboot.webflux.app;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.service.ProductoService;

import io.netty.util.internal.StringUtil;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductoService productoService;
	
	@Value("${config.base.endpoint}")
	private String url;
	
	@Test
	public void listarTest() {
		client
		.get()
		.uri(url)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBodyList(Producto.class)
		//.hasSize(7);
		.consumeWith(response -> {
			List<Producto> productos = response.getResponseBody();
			productos.forEach(p -> {
				System.out.println(p.getNombre());
			});
			Assertions.assertTrue(productos.size()>0);
		});
	}
	
	@Test
	public void verTest() {
		Producto producto = productoService.findByNombre("TV Samsung").block();
		
		client
		.get()
		.uri(url.concat("/{id}"), Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.exchange()
		.expectStatus()
		.isOk()
		.expectHeader()
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto p = response.getResponseBody();
			Assertions.assertTrue(p.getId().length()>0);
			Assertions.assertEquals("TV Samsung", p.getNombre());
		});
//		.expectBody()
//		.jsonPath("$.id").isNotEmpty()
//		.jsonPath("$.nombre").isEqualTo("TV Samsung");	
	}
	
	@Test
	public void crearTest() {
		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();
		Producto producto = new Producto("Mesa comedor", 100.0, categoria);
		client
		.post()
		.uri(url)
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBody()
		.jsonPath("$.producto.id").isNotEmpty()
		.jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
		.jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}
	
	@Test
	public void crear2Test() {
		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();
		Producto producto = new Producto("Mesa comedor", 100.0, categoria);
		client
		.post()
		.uri(url)
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		.consumeWith(response -> {
			Object o = response.getResponseBody().get("producto");
			Producto p = new ObjectMapper().convertValue(o, Producto.class);
			Assertions.assertTrue(!StringUtil.isNullOrEmpty(p.getId()));
			Assertions.assertEquals(p.getNombre(), "Mesa comedor");
			Assertions.assertTrue(!StringUtil.isNullOrEmpty(p.getCategoria().getNombre()));
		});
	}
	
	@Test
	public void editarTest() {
		Producto producto = productoService.findByNombre("PC MAC").block();		
		Categoria categoria = productoService.findCategoriaByNombre("Electronico").block();		
		Producto productoEditado = new Producto("Asus notebook", 400.0, categoria);
		client.put()
		.uri(url.concat("/{id}"),Collections.singletonMap("id", producto.getId()))
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.body(Mono.just(productoEditado), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Asus notebook")
		.jsonPath("$.categoria.nombre").isEqualTo("Electronico");
	}
	
	@Test
	public void eliminarTest() {
		Producto producto = productoService.findByNombre("MICROONDAS GAFA").block();
		client.delete()
		.uri(url.concat("/{id}"),Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus()
		.isNoContent()
		.expectBody()
		.isEmpty();
		
		client.get()
		.uri(url.concat("/{id}"),Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus()
		.isNotFound()
		.expectBody()
		.isEmpty();		
	}
}
