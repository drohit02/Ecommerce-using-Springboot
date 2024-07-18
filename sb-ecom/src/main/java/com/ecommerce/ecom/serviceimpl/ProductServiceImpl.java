package com.ecommerce.ecom.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.ecom.custom_exception.ResourceNotFoundException;
import com.ecommerce.ecom.dto.ProductDTO;
import com.ecommerce.ecom.model.Category;
import com.ecommerce.ecom.model.Product;
import com.ecommerce.ecom.payload.ProductResponse;
import com.ecommerce.ecom.repository.CategoryRepository;
import com.ecommerce.ecom.repository.ProductRepository;
import com.ecommerce.ecom.service.FileService;
import com.ecommerce.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private FileService fileService;
	
	/* locate the path for the upload image and fetching the value from application.properties */
	@Value("${project.image}")
	String path;

	@Override
	public ProductResponse getAllProducts() {

		List<Product> products = this.productRepository.findAll();
		if (products.isEmpty())
			throw new ResourceNotFoundException("Product List is empty");
		return mapProductResponse(products);
	}
	
	@Override
	public ProductResponse getProductWithCategoryId(Long categoryId) {
		Category category = this.categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));
		List<Product> products= this.productRepository.findAllByCategory(category);
		if (products.isEmpty())
			throw new ResourceNotFoundException("Product list is empty");
		return mapProductResponse(products);
	}
	
	@Override
	public ProductResponse getProductWithKeyword(String keyword) {
		List<Product> products = this.productRepository.findAllByProductNameLikeIgnoreCase('%'+keyword+'%');
		if(products.isEmpty())
			throw new ResourceNotFoundException("Product with the "+keyword+" is not found!!!");
		return mapProductResponse(products);
	}

	@Override
	public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
		Category category = findCategoryById(categoryId);
		Product product = productDtoToProduct(productDTO);
		
		product.setCategory(category);
		product.setSpecialPrice(calculateSpeciaPrize(product));
		product.setImage("default.png");
		Product saveProduct = this.productRepository.save(product);
		
		return productToProductDTO(saveProduct);
	}
	
	@Override
	public ProductDTO updateExitingProductData(ProductDTO productDTO, Long productId) {
		Product savedProduct = this.productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
		Product updatedProduct = updateProductData(savedProduct, productDtoToProduct(productDTO));
		return productToProductDTO(updatedProduct);
	}
	

	@Override
	public ProductDTO deleteProductById(Long productId) {
		Product product = this.productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product","Productid",productId));
		this.productRepository.deleteById(product.getProductId());
		return productToProductDTO(product);
	}
	
	
	@Override
	public ProductDTO updateProductWithImage(Long productId, MultipartFile image) throws IOException {
		/* Find the product using productId */
		Product savedProduct = this.productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
		
		/* Get the filename when image get uploaded.Below method will return the filename after the image gets uploaded */
		String fileName = this.fileService.uploadImage(path,image);
		
		/*updating the image name and save the updated details*/
		savedProduct.setImage(fileName);
		
		Product updatedProduct = this.productRepository.save(savedProduct);
		return productToProductDTO(updatedProduct);
	}



	/*-----------------------------------HELPER METHOD AREA-----------------------------------------*/

	private Category findCategoryById(Long categoryId) {
		Category category = this.categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
		return category;
	}

	private double calculateSpeciaPrize(Product product) {
		double specialPrize = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
		return specialPrize;
	}

	private ProductResponse mapProductResponse(List<Product> products) {
		List<ProductDTO> productList = products.stream().map(product -> modelMapper.map(product, ProductDTO.class))
				.toList();
		ProductResponse productResponse = modelMapper.map(productList, ProductResponse.class);
		productResponse.setProducts(productList);
		return productResponse;
	}
	
	private Product updateProductData(Product savedProduct,Product product) {
		savedProduct.setProductName(product.getProductName());
		savedProduct.setDescription(product.getDescription());
		savedProduct.setPrice(product.getPrice());
		savedProduct.setDiscount(product.getDiscount());
		savedProduct.setQuantity(product.getQuantity());
		savedProduct.setSpecialPrice(calculateSpeciaPrize(product));
		Product updatedProduct = this.productRepository.save(savedProduct);
		return updatedProduct;
	}
	
	private ProductDTO productToProductDTO(Product product) {
		return modelMapper.map(product, ProductDTO.class);
	}
	private Product productDtoToProduct(ProductDTO productDTO) {
		return modelMapper.map(productDTO, Product.class);
	}

}
