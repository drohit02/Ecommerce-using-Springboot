package com.ecommerce.ecom.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ecommerce.ecom.payload.CategoryResponse;

@Configuration
public class AppConfiguration {
	
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
	@Bean
	@Scope("prototype")
	public CategoryResponse categoryResponse() {
		return new CategoryResponse();
	}

}
