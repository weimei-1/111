package com.supermarket.service;

import com.supermarket.entity.Supplier;

import java.util.List;

public interface SupplierService {

    List<Supplier> getAll(String keyword);

    Supplier getById(Long id);

    void add(Supplier supplier);

    void update(Supplier supplier);

    void delete(Long id);
}