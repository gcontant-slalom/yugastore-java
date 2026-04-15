package com.yugabyte.app.yugastore.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.repo.ProductMetadataRepo;
import com.yugabyte.app.yugastore.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMetadataRepo productRepository;

    @Autowired
    public ProductServiceImpl(ProductMetadataRepo productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<ProductMetadata> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<ProductMetadata> findById(String id, String tenantKey) {
        return productRepository.findById(id)
                .filter(productMetadata -> Objects.equals(tenantKey, productMetadata.getTenantKey()));
    }

	@Override
	public List<ProductMetadata> findAllProductsPageable(int limit, int offset) {
		
		return productRepository.getProducts(limit, offset);
	}

    @Override
    public List<ProductMetadata> findAllProductsPageable(int limit, int offset, String tenantKey) {
        return productRepository.getProductsByTenantKey(tenantKey, limit, offset);
    }
}
