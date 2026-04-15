package com.yugabyte.app.yugastore.service;

import java.util.List;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;

public interface ProductCatalogServiceRest {
  
  ProductMetadata getProductDetails(String asin);

  ProductMetadata getProductDetails(String asin, String tenantKey, String companyName);

  List<ProductMetadata> getProducts(int limit, int offset);

  List<ProductMetadata> getProducts(int limit, int offset, String tenantKey, String companyName);

  List<ProductRanking> getProductsByCategory(String category, int limit, int offset);

  List<ProductRanking> getProductsByCategory(String category, int limit, int offset, String tenantKey, String companyName);
}
