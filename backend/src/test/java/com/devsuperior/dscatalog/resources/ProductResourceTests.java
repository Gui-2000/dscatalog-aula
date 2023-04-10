package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private Long existingId;
	private Long nonExistingId;
	private ProductDTO productDTO;
	private PageImpl<ProductDTO> page;
	private Long DependentId;
	
	@BeforeEach
	void setUp() throws Exception {

		existingId = 1L;
		nonExistingId = 2L;
	    DependentId = 3L;
		
		productDTO = Factory.createProductDTO();
	    page = new PageImpl<>(List.of(productDTO));
	    
	    Mockito.when(service.findAllPaged(any())).thenReturn(page);
	    
	    Mockito.when(service.findById(existingId)).thenReturn(productDTO);
	    Mockito.when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
	
	    Mockito.when(service.insert(any())).thenReturn(productDTO);
	    
	    Mockito.when(service.update(eq(existingId), any())).thenReturn(productDTO);
	    Mockito.when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
	
	    Mockito.doNothing().when(service).delete(existingId);
		Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(service).delete(DependentId);
	}
	
	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		
		mockMvc.perform(delete("/products/{id}", nonExistingId)
		.accept(MediaType.APPLICATION_JSON))
		
		.andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenIdExist() throws Exception {
		
		mockMvc.perform(delete("/products/{id}", existingId)).andExpect(status().isNoContent());
	}
	
	@Test
	public void insertShouldReturnProductDTOCreate() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		mockMvc.perform(post("/products")
		.content(jsonBody)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON))
		
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.name").exists())
		.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExist() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		mockMvc.perform(put("/products/{id}", existingId)
		.content(jsonBody)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON))
		
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.name").exists())
		.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		
        String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		mockMvc.perform(put("/products/{id}", nonExistingId)
		.content(jsonBody)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON))
		
		.andExpect(status().isNotFound());
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception {
		mockMvc.perform(get("/products")).andExpect(status().isOk());
	}
	
	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		mockMvc.perform(get("/products/{id}", existingId))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.name").exists())
		.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void findByIdShoulReturnNotFoundWhenIdDoesNotExist() throws Exception {
		mockMvc.perform(get("/products/{id}", nonExistingId)).andExpect(status().isNotFound());
	}
}
