package org.buaa.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.service.EsService;
import org.buaa.project.toolkit.RedisCount;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * es服务接口层实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsServiceImpl implements EsService {

    private final RestHighLevelClient client;

    private final RedisCount redisCount;

    @Value("${elasticsearch.index-name}")
    private String INDEX_NAME;

    @SneakyThrows
    @Override
    public List<String> autoComplete(String keyword){
        SearchRequest request = new SearchRequest(INDEX_NAME);
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "mySuggestion",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix(keyword)
                        .skipDuplicates(true)
                        .size(10)
        ));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        Suggest suggest = response.getSuggest();
        CompletionSuggestion suggestions = suggest.getSuggestion("mySuggestion");

        List<String> titles = new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : suggestions.getOptions()) {
            // 从每个选项中获取 _source 并提取 title
            Map<String, Object> source = option.getHit().getSourceAsMap();
            if (source != null && source.containsKey("title")) {
                titles.add((String) source.get("title"));
            }
        }

        return titles;
    }

    @SneakyThrows
    public List<String> analyze(String text) {
        AnalyzeRequest request = AnalyzeRequest.withIndexAnalyzer(INDEX_NAME, "ik_max_word", text);
        AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);

        List<String> tokens = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : response.getTokens()) {
            tokens.add(token.getTerm());
        }
        return tokens;
    }

}
