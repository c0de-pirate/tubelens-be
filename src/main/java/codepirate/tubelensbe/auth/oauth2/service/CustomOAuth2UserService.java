package codepirate.tubelensbe.auth.oauth2.service;

import codepirate.tubelensbe.user.repository.UserRepository;
import codepirate.tubelensbe.user.domain.User;
import codepirate.tubelensbe.auth.oauth2.user.GoogleOAuth2UserInfo;
import codepirate.tubelensbe.auth.oauth2.user.OAuth2UserInfo;
import codepirate.tubelensbe.auth.common.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserProcessingService processingService;

    public CustomOAuth2UserService(OAuth2UserProcessingService processingService) {
        this.processingService = processingService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        processingService.processOAuth2User(oAuth2User); // 사용자 처리만 하고 결과는 무시

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(Authority.ROLE_USER.name())),
                oAuth2User.getAttributes(),
                "sub"
        );
    }
}