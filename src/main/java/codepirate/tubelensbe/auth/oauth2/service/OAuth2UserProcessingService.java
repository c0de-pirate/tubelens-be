package codepirate.tubelensbe.auth.oauth2.service;

import codepirate.tubelensbe.auth.common.Authority;
import codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.user.repository.UserRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserProcessingService {

    private final UserRepository userRepository;

    public OAuth2UserProcessingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User processOAuth2User(OAuth2User oAuth2User) {
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        return userRepository.findByGoogleId(userInfo.getId())
                .map(user -> updateExistingUser(user, userInfo))
                .orElseGet(() -> createNewUser(userInfo));
    }

    private User updateExistingUser(User user, GoogleOAuth2UserInfo userInfo) {
        if (!user.getEmail().equals(userInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email mismatch with existing account");
        }

        user.setName(userInfo.getName());
        user.setPicture(userInfo.getPictureUrl());
        return userRepository.save(user);
    }

    private User createNewUser(GoogleOAuth2UserInfo userInfo) {
        User user = new User();
        user.setGoogleId(userInfo.getId());
        user.setName(userInfo.getName());
        user.setEmail(userInfo.getEmail());
        user.setPicture(userInfo.getPictureUrl());
        user.setAuthority(Authority.ROLE_USER);
        return userRepository.save(user);
    }
}
