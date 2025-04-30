package codepirate.tubelensbe.user.repository;

import codepirate.tubelensbe.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByName(String name);
    Optional<User> findByChannelId(String channelId);

}