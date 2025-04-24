package codepirate.tubelensbe.analysis.service;

import codepirate.tubelensbe.analysis.config.GoogleOAuthProperties;
import codepirate.tubelensbe.analysis.domain.MyVideoTag;
import codepirate.tubelensbe.analysis.dto.TagDto;
import codepirate.tubelensbe.analysis.dto.TagListDto;
import codepirate.tubelensbe.analysis.repository.MyVideoTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final GoogleOAuthProperties oAuthProperties;
    private final YouTubeAPiClient youTubeAPiClient;
    private final MyVideoTagRepository myVideoTagRepository;

    @Transactional
    public TagListDto refreshTagsIfNeeded() throws GeneralSecurityException, IOException {
        String accessToken = oAuthProperties.getAccessToken();
        String refreshToken = oAuthProperties.getRefreshToken();
        boolean alreadyUpdatedToday = myVideoTagRepository.existsByLastUpdated(LocalDate.now());

        if (alreadyUpdatedToday) {
            return getTags();
        }

        List<String> tagsFromApi = youTubeAPiClient.fetchAllTags(accessToken, refreshToken);

        List<String> cleanedTags = tagsFromApi.stream()
                .map(tag -> tag.trim().toLowerCase())
                .collect(Collectors.toList());

        Map<String, Long> tagCounts = cleanedTags.stream()
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));


        for (Map.Entry<String, Long> entry : tagCounts.entrySet()) {
            String tagName = entry.getKey();
            long count = entry.getValue();

            Optional<MyVideoTag> existingTag = myVideoTagRepository.findByTag(tagName);

            if (existingTag.isPresent()) {
                MyVideoTag tag = existingTag.get();
                tag.setCount(tag.getCount() + (int) count);
                tag.setLastUpdated(LocalDate.now());
                myVideoTagRepository.save(tag);

            } else {
                MyVideoTag newTag = new MyVideoTag();
                newTag.setTag(tagName);
                newTag.setCount((int) count);
                newTag.setLastUpdated(LocalDate.now());
                myVideoTagRepository.save(newTag);
            }
        }
        return getTags();
    }

    @Transactional(readOnly = true)
    public TagListDto getTags() {
        List<TagDto> tagDtoList = myVideoTagRepository.findAll().stream()
                .map(tag -> new TagDto(tag.getTag(), tag.getCount()))
                .sorted(Comparator.comparingInt(TagDto::getSize).reversed())
                .collect(Collectors.toList());

        List<String> textList = tagDtoList.stream()
                .map(TagDto::getText)
                .collect(Collectors.toList());

        List<Integer> sizeList = tagDtoList.stream()
                .map(TagDto::getSize)
                .collect(Collectors.toList());

        return new TagListDto(textList, sizeList);

    }
}
