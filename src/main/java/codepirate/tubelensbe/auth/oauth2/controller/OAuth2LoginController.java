package codepirate.tubelensbe.auth.oauth2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OAuth2LoginController {

    // 오류 페이지만 유지하고 나머지 엔드포인트는 제거
    @GetMapping("/social/login/error")
    @ResponseBody
    public String loginError(HttpServletRequest request) {
        String error = request.getParameter("error");
        String message = request.getParameter("message");
        return "로그인 실패: " + error + " - " + message;
    }

}