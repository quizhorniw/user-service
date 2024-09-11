package com.drevotiuk.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.drevotiuk.model.ConfirmationToken;

/**
 * Repository interface for managing {@link ConfirmationToken} entities in
 * MongoDB.
 * <p>
 * This interface extends {@link MongoRepository} to provide CRUD operations for
 * {@link ConfirmationToken} entities,
 * with an additional method to query tokens by their value.
 * </p>
 */
@Repository
public interface ConfirmationTokenRepository extends MongoRepository<ConfirmationToken, ObjectId> {
  /**
   * Finds a {@link ConfirmationToken} by its token value.
   *
   * @param token the token value to search for
   * @return an {@link Optional} containing the {@link ConfirmationToken} if
   *         found, otherwise an empty {@link Optional}
   */
  Optional<ConfirmationToken> findByToken(String token);
}
