package codepirate.tubelensbe.auth.oauth2.service;

import codepirate.tubelensbe.user.repository.UserRepository;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo;
import codepirate.tubelensbe.auth.oauth2.user.OAuth2UserInfo;
import codepirate.tubelensbe.auth.common.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        Optional<User> userOptional = userRepository.findByGoogleId(oAuth2UserInfo.getId());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getEmail().equals(oAuth2UserInfo.getEmail())) {
                throw new OAuth2AuthenticationException("Email mismatch with existing account.");
            }
            user = updateUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserInfo);
        }

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                Collections.singletonList(() -> Authority.ROLE_USER.name()),
                oAuth2User.getAttributes(),
                "sub" // Google의 사용자 고유 ID를 nameAttributeKey로 사용
        );
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setGoogleId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setPicture(oAuth2UserInfo.getPictureUrl());
        user.setAuthority(Authority.ROLE_USER);
        return userRepository.save(user);
    }

    private User updateUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setPicture(oAuth2UserInfo.getPictureUrl());
        return userRepository.save(existingUser);
    }
}