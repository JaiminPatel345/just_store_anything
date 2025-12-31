package com.jaimin.justStore.repository;

import com.jaimin.justStore.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    
    Optional<OAuthToken> findByProvider(String provider);
    
    void deleteByProvider(String provider);
}
