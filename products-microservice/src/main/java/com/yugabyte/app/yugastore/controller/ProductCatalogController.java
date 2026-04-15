package com.yugabyte.app.yugastore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.service.ProductService;
import com.yugabyte.app.yugastore.service.ProductRankingService;

@RestController
@RequestMapping(value = "/products-microservice")
public class ProductCatalogController {

  private static final String TENANT_KEY_HEADER = "X-Tenant-Key";
  private static final String MERCHANT_COMPANY_NAME_HEADER = "X-Merchant-Company-Name";

  // This service is used to lookup metadata of products by their id.
  @Autowired
  ProductService productService;

  // This service is used to lookup the top products by sales rank in a category.
  @Autowired
  ProductRankingService productRankingService;

  @RequestMapping(method = RequestMethod.GET, value = "/product/{asin}", produces = "application/json")
  public ResponseEntity<ProductMetadata> getProductDetails(@PathVariable String asin,
      @RequestHeader(value = TENANT_KEY_HEADER, required = false) String tenantKey,
      @RequestHeader(value = MERCHANT_COMPANY_NAME_HEADER, required = false) String companyName) {
    return productService.findById(asin)
        .map(productMetadata -> new ResponseEntity<>(productMetadata, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/products", produces = "application/json")
  public List<ProductMetadata> getProducts(@Param("limit") int limit,
    @Param("offset") int offset,
    @RequestHeader(value = TENANT_KEY_HEADER, required = false) String tenantKey,
    @RequestHeader(value = MERCHANT_COMPANY_NAME_HEADER, required = false) String companyName) {
    return productService.findAllProductsPageable(limit, offset);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/products/category/{category}", produces = "application/json")
  public List<ProductRanking> getProductsByCategory(@PathVariable String category,
                                                    @Param("limit") int limit,
                                                    @Param("offset") int offset,
                                                    @RequestHeader(value = TENANT_KEY_HEADER, required = false) String tenantKey,
                                                    @RequestHeader(value = MERCHANT_COMPANY_NAME_HEADER, required = false) String companyName) {
    return productRankingService.getProductsByCategory(category, limit, offset);
  }
}
