package com.AgenticAi.AIProject.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.AgenticAi.AIProject.Entity.User;
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
