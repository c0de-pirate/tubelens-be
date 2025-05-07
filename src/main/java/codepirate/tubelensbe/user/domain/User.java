package codepirate.tubelensbe.user.domain;

import codepirate.tubelensbe.auth.common.Authority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String googleId; // Google 고유 사용자 ID (`sub`)

    @Column(nullable = false)
    private String name;     // 사용자 이름

    @Column(nullable = false, unique = true)
    private String email;    // 이메일 주소

    @Column(nullable = true)
    private String picture;  // 프로필 사진 URL

    @Column(nullable = true)
    private String gender;

    @Column(nullable = true)
    private java.util.Date hire_date;   // 가입일/입사일

    @Column(nullable = true)
    private String channelId;  // YouTube 채널 ID

    @Column(nullable = true)
    private String etc;

    @Column(unique = true, nullable = true)
    private String jwt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    @Column(nullable = true, length = 2000)
    private String googleAccessToken;

    @Column(nullable = true, length = 2000)
    private String googleRefreshToken;

    @Column(nullable = true)
    private Instant googleTokenExpiryDate;

    public User(String googleId, String name, String email, String picture, Authority authority) {
        this.googleId = googleId;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.authority = authority;
        this.hire_date = new java.util.Date(); // 가입 시 현재 시간 설정
    }

    public User(String googleId, String name, String email, String picture, String gender, java.util.Date hire_date, String channel_id, String etc, Authority authority) {
        this.googleId = googleId;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.gender = gender;
        this.hire_date = hire_date;
        this.channelId = channel_id;
        this.etc = etc;
        this.authority = authority;
    }
}