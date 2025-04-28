package codepirate.tubelensbe.analysis.service;

import codepirate.tubelensbe.analysis.domain.MyVideoTag;
import codepirate.tubelensbe.analysis.dto.TagDto;
import codepirate.tubelensbe.analysis.repository.MyVideoTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final MyVideoTagRepository myVideoTagRepository;
    private final YouTubeAPiClient youTubeAPiClient;

    public List<TagDto> getTags(String channelId, String accessToken, String refreshToken) throws GeneralSecurityException, IOException {
        List<MyVideoTag> todayTags = myVideoTagRepository.findByChannelIdAndLastUpdated(channelId, LocalDate.now());

        // 오늘의 태그가 있으면 반환
        if (!todayTags.isEmpty()) {
            return convertToDtoList(todayTags);
        }

        return refreshTags(channelId, accessToken, refreshToken); // 데이터가 없으면 리프레시 호출
    }

    public List<TagDto> refreshTags(String channelId, String accessToken, String refreshToken) throws GeneralSecurityException, IOException {

        List<MyVideoTag> todayTags = myVideoTagRepository.findByChannelIdAndLastUpdated(channelId, LocalDate.now());

        // 오늘의 태그가 있으면 반환
        if (!todayTags.isEmpty()) {
            return convertToDtoList(todayTags);
        }

        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        List<String> tags = youTubeAPiClient.fetchAllTags(channelId, accessToken, refreshToken);

        Map<String, Integer> tagCounts = new HashMap<>();
        for (String tag : tags) {
            String normalizedTag = tag.toLowerCase();
            tagCounts.put(normalizedTag, tagCounts.getOrDefault(normalizedTag, 0) + 1);
        }

        List<MyVideoTag> savedTags = saveTagsToDatabase(channelId, tagCounts);
        return convertToDtoList(savedTags);
    }

    @Transactional
    private List<MyVideoTag> saveTagsToDatabase(String channelId, Map<String, Integer> tagCounts) {
        LocalDate today = LocalDate.now();

        // 1. 현재 존재하는 모든 태그 조회
        List<MyVideoTag> existingTags = myVideoTagRepository.findByChannelId(channelId);

        // 2. 기존 태그와 새 태그를 비교해 수정/삭제/추가할 태그 파악
        Set<String> existingTagNames = existingTags.stream()
                .map(MyVideoTag::getTag)
                .collect(Collectors.toSet());

        Set<String> newTagNames = tagCounts.keySet();

        // 3. 삭제할 태그 (기존에 있지만 새로운 데이터에 없는 태그)
        Set<String> tagsToDelete = new HashSet<>(existingTagNames);
        tagsToDelete.removeAll(newTagNames);

        // 4. 유지할 태그 (기존에도 있고 새로운 데이터에도 있는 태그)
        Set<String> tagsToUpdate = new HashSet<>(existingTagNames);
        tagsToUpdate.retainAll(newTagNames);

        // 5. 추가할 태그 (기존에 없지만 새로운 데이터에 있는 태그)
        Set<String> tagsToInsert = new HashSet<>(newTagNames);
        tagsToInsert.removeAll(existingTagNames);

        // 6. 기존 태그를 Map으로 변환하여 빠르게 조회할 수 있도록 함
        Map<String, MyVideoTag> existingTagMap = existingTags.stream()
                .collect(Collectors.toMap(MyVideoTag::getTag, Function.identity()));

        // 7. 삭제 처리 - 필요한 경우
        if (!tagsToDelete.isEmpty()) {
            List<MyVideoTag> tagsToRemove = tagsToDelete.stream()
                    .map(existingTagMap::get)
                    .collect(Collectors.toList());
            myVideoTagRepository.deleteAll(tagsToRemove);
        }

        // 8. 결과를 담을 리스트
        List<MyVideoTag> resultList = new ArrayList<>();

        // 9. 업데이트 처리
        for (String tagName : tagsToUpdate) {
            MyVideoTag tag = existingTagMap.get(tagName);
            tag.setCount(tagCounts.get(tagName));
            tag.setLastUpdated(today);
            resultList.add(tag);
            // dirty checking으로 저장됨
        }

        // 10. 추가 처리 - 새로운 항목을 일괄 등록
        List<MyVideoTag> newTags = new ArrayList<>();
        for (String tagName : tagsToInsert) {
            MyVideoTag newTag = new MyVideoTag(channelId, tagName, tagCounts.get(tagName), today);
            newTags.add(newTag);
        }

        // 11. 새 태그를 저장하고 결과 리스트에 추가
        if (!newTags.isEmpty()) {
            List<MyVideoTag> savedTags = myVideoTagRepository.saveAll(newTags);
            resultList.addAll(savedTags);
        }

        // 12. 저장된 모든 태그를 반환
        return resultList;
    }

    private List<TagDto> convertToDtoList(List<MyVideoTag> tags) {
        return tags.stream()
                .map(tag -> new TagDto(tag.getTag(), tag.getCount()))
                .toList();
    }
}
