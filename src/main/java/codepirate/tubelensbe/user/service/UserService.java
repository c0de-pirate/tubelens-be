// user/service/UserService.java
package codepirate.tubelensbe.user.service;

import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());

        String pictureUrl = user.getPicture();
        if (pictureUrl != null && !pictureUrl.startsWith("http")) {
            pictureUrl = "https://lh3.googleusercontent.com/a/" + pictureUrl;
        }
        userInfo.put("picture", pictureUrl);
        userInfo.put("googleId", user.getGoogleId());
        return userInfo;
    }
}