package com.yugabyte.app.yugastore.service;

import java.util.List;
import java.util.Optional;

import com.yugabyte.app.yugastore.domain.*;

public interface ProductService {

    Optional<ProductMetadata> findById(String id);

    Optional<ProductMetadata> findById(String id, String tenantKey);

    List<ProductMetadata> findAllProductsPageable(int limit, int offset);

    List<ProductMetadata> findAllProductsPageable(int limit, int offset, String tenantKey);

}
