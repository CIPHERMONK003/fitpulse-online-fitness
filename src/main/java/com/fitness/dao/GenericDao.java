package com.fitness.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO Interface demonstrating Java Generics & Collections.
 *
 * @param <T> Entity type
 * @param <ID> Primary key identifier type
 */
public interface GenericDao<T, ID> {
    T create(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean update(T entity);
    boolean delete(ID id);
}
