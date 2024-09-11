package com.drevotiuk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.drevotiuk.model.JwtSecretKey;

/**
 * Repository interface for managing {@link JwtSecretKey} entities in MongoDB.
 * <p>
 * This interface extends {@link MongoRepository} to provide CRUD operations for
 * {@link JwtSecretKey} entities.
 * </p>
 */
@Repository
public interface SecretKeyRepository extends MongoRepository<JwtSecretKey, String> {
}
