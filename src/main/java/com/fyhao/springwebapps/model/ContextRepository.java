package com.fyhao.springwebapps.model;

import java.util.List;

import com.fyhao.springwebapps.entity.Context;

import org.springframework.data.repository.CrudRepository;

public interface ContextRepository extends CrudRepository<Context, Long> {
    
    public List<Context> findByKey(String key);
}