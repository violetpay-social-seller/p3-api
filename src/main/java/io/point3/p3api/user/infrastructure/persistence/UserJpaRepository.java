package io.point3.p3api.user.infrastructure.persistence;

import io.point3.p3api.user.domain.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {

  Optional<User> findByCognitoSub(String cognitoSub);

  Optional<User> findActiveByCognitoSub(String cognitoSub);

  @Query("select user from User user where lower(user.email) = lower(:email)")
  Optional<User> findByEmail(@Param("email") String email);
}
