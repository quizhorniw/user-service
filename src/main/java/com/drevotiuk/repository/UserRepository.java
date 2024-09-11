package com.drevotiuk.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.drevotiuk.model.UserPrincipal;

/**
 * Repository interface for managing {@link UserPrincipal} entities in MongoDB.
 * <p>
 * This interface extends {@link MongoRepository} to provide CRUD operations for
 * {@link UserPrincipal} entities,
 * with additional methods to query by email.
 * </p>
 */
@Repository
public interface UserRepository extends MongoRepository<UserPrincipal, ObjectId> {
  /**
   * Checks if a {@link UserPrincipal} with the specified email exists in the
   * repository.
   *
   * @param email the email to check for existence
   * @return {@code true} if a {@link UserPrincipal} with the specified email
   *         exists, otherwise {@code false}
   */
  boolean existsByEmail(String email);

  /**
   * Finds a {@link UserPrincipal} by its email.
   *
   * @param email the email to search for
   * @return an {@link Optional} containing the {@link UserPrincipal} if found,
   *         otherwise an empty {@link Optional}
   */
  Optional<UserPrincipal> findByEmail(String email);
}
