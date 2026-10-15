package com.fitness.model;

/**
 * Generic interface representing any entity with a unique identifier.
 * Demonstrates Generics and Interface usage.
 *
 * @param <ID> the type of identifier (e.g. Integer, Long, String)
 */
public interface Identifiable<ID> {
    ID getId();
    void setId(ID id);
}
