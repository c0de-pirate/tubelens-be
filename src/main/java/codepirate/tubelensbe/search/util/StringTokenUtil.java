package codepirate.tubelensbe.search.util;

import java.util.Arrays;
import java.util.List;

public class StringTokenUtil {

    /**
     * 문자열을 공백 및 특수문자 기준으로 분리 후 필터링
     *
     * @param input 입력 문자열
     * @param minLength 최소 토큰 길이
     * @param maxLength 최대 토큰 길이
     * @param limit 반환할 최대 토큰 수
     * @return 필터링된 토큰 리스트
     */
    public static List<String> extractTokens(String input, int minLength, int maxLength, int limit) {
        if (input == null || input.isBlank()) return List.of();

        return Arrays.stream(input.split("\\s+|[^가-힣a-zA-Z0-9]"))
                .filter(token -> token.length() >= minLength && token.length() <= maxLength)
                .distinct()
                .limit(limit)
                .toList();
    }
}
