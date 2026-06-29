package com.supermarket.service;

import com.supermarket.entity.MemberLevel;

import java.util.List;

public interface MemberLevelService {

    List<MemberLevel> getAll(String name);

    MemberLevel getById(Long id);

    void add(MemberLevel memberLevel);

    void update(MemberLevel memberLevel);

    void delete(Long id);
}