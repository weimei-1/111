package com.supermarket.service;

import com.supermarket.entity.Promotion;

import java.util.List;

public interface PromotionService {

    List<Promotion> getAll(String keyword, Integer status);

    Promotion getById(Long id);

    void add(Promotion promotion);

    void update(Promotion promotion);

    void delete(Long id);
}