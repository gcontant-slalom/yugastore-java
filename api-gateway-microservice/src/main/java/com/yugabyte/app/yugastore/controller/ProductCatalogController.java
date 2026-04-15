package com.yugabyte.app.yugastore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import com.yugabyte.app.yugastore.service.ProductCatalogServiceRest;

/**
 * The controller that handles all calls related to the product catalog.
 */
@RestController
@RequestMapping(value = "/api/v1")
public class ProductCatalogController {

  // The REST service handler that interacts with the product catalog microservice.
  private final ProductCatalogServiceRest productCatalogServiceRest;
  private final AuthServiceRest authServiceRest;

  @Autowired
  public ProductCatalogController(ProductCatalogServiceRest productCatalogServiceRest,
      AuthServiceRest authServiceRest) {
    this.productCatalogServiceRest = productCatalogServiceRest;
    this.authServiceRest = authServiceRest;
  }


  /**
   * Return details of a single product.
   */
  @RequestMapping(method = RequestMethod.GET, value = "/product/{asin}", produces = "application/json")
  public @ResponseBody ResponseEntity<ProductMetadata> getProductDetails(@PathVariable("asin") String asin) {
    ProductMetadata productMetadata = productCatalogServiceRest.getProductDetails(asin);
    if (productMetadata == null) {
      return new ResponseEntity<ProductMetadata>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<ProductMetadata>(productMetadata, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/tenant/{tenantKey}/product/{asin}", produces = "application/json")
  public @ResponseBody ResponseEntity<ProductMetadata> getTenantProductDetails(
      @PathVariable("tenantKey") String tenantKey,
      @PathVariable("asin") String asin) {
    MerchantSignupResponse tenantContext = authServiceRest.merchantContextForTenantKey(tenantKey);
    ProductMetadata productMetadata = productCatalogServiceRest.getProductDetails(
        asin,
        tenantContext.getTenantKey(),
        tenantContext.getCompanyName());
    if (productMetadata == null) {
      return new ResponseEntity<ProductMetadata>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<ProductMetadata>(productMetadata, HttpStatus.OK);
  }


  /**
   * Fetch a listing of products, given a limit and offset.
   */
  @RequestMapping(method = RequestMethod.GET, value = "/products", produces = "application/json")
  public @ResponseBody ResponseEntity<List<ProductMetadata>> getProducts(@Param("limit") int limit,
                                                                         @Param("offset") int offset) {
    List<ProductMetadata> products = productCatalogServiceRest.getProducts(limit, offset);
    return new ResponseEntity<List<ProductMetadata>>(products, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/tenant/{tenantKey}/products", produces = "application/json")
  public @ResponseBody ResponseEntity<List<ProductMetadata>> getTenantProducts(
      @PathVariable("tenantKey") String tenantKey,
      @Param("limit") int limit,
      @Param("offset") int offset) {
    MerchantSignupResponse tenantContext = authServiceRest.merchantContextForTenantKey(tenantKey);
    List<ProductMetadata> products = productCatalogServiceRest.getProducts(
        limit,
        offset,
        tenantContext.getTenantKey(),
        tenantContext.getCompanyName());
    return new ResponseEntity<List<ProductMetadata>>(products, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/products/category/{category}", produces = "application/json")
  public @ResponseBody ResponseEntity<List<ProductRanking>> getProductsByCategory(
      @PathVariable("category") String category,
      @Param("limit") int limit,
      @Param("offset") int offset) {
    List<ProductRanking> products = productCatalogServiceRest.getProductsByCategory(category, limit, offset);
    return new ResponseEntity<List<ProductRanking>>(products, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/tenant/{tenantKey}/products/category/{category}", produces = "application/json")
  public @ResponseBody ResponseEntity<List<ProductRanking>> getTenantProductsByCategory(
      @PathVariable("tenantKey") String tenantKey,
      @PathVariable("category") String category,
      @Param("limit") int limit,
      @Param("offset") int offset) {
    MerchantSignupResponse tenantContext = authServiceRest.merchantContextForTenantKey(tenantKey);
    List<ProductRanking> products = productCatalogServiceRest.getProductsByCategory(
        category,
        limit,
        offset,
        tenantContext.getTenantKey(),
        tenantContext.getCompanyName());
    return new ResponseEntity<List<ProductRanking>>(products, HttpStatus.OK);
  }

}
