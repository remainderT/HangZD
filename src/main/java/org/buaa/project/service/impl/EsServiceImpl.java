package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dao.entity.ConversationDOC;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dto.req.conversation.ConversationPageReqDTO;
import org.buaa.project.dto.resp.ConversationAllRespDTO;
import org.buaa.project.dto.resp.ConversationPageRespDTO;
import org.buaa.project.service.EsService;
import org.buaa.project.toolkit.RedisCount;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.buaa.project.common.consts.RedisCacheConstants.CONVERSATION_LIKE_SET_KEY;


/**
 * es服务接口层实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsServiceImpl implements EsService {

    private final RestHighLevelClient client;

    private final QuestionMapper questionMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${elasticsearch.index-name}")
    private String INDEX_NAME;

    @Override
    public void insert(ConversationDO conversationDO) {
        Long questionId = conversationDO.getQuestionId();
        QuestionDO questionDO = questionMapper.selectById(questionId);
        ConversationDOC conversationDOC = ConversationDOC.builder()
                .content(questionDO.getContent())
                .title(questionDO.getTitle())
                .id(conversationDO.getId())
                .build();
        List<String> suggestion = analyze(questionDO.getTitle());
        conversationDOC.setSuggestion(suggestion);
        try {
            IndexRequest indexRequest= new IndexRequest(INDEX_NAME).id(questionDO.getId().toString());
            indexRequest.source(JSON.toJSONString(conversationDOC), XContentType.JSON);
            client.index(indexRequest, RequestOptions.DEFAULT);
            log.info("ES插入数据: {}", conversationDOC);
        } catch (Exception e) {
            log.error("ES操作失败", e);
        }
    }

    @SneakyThrows
    public ConversationAllRespDTO search(ConversationPageReqDTO requestParam)  {
        SearchRequest request = new SearchRequest(INDEX_NAME);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title").requireFieldMatch(false)
                .field("content").requireFieldMatch(false);

        String keyword = requestParam.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(
                    QueryBuilders.boolQuery()
                            .should(QueryBuilders.matchQuery("title", keyword))
                            .should(QueryBuilders.matchQuery("content", keyword))
                            .minimumShouldMatch(1)
            );
        }

        request.source()
                .query(boolQuery)
                .highlighter(highlightBuilder);

        long page = requestParam.getCurrent();
        long size = requestParam.getSize();
        request.source().from((int) ((page - 1) * size)).size((int) size);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        SearchHits searchHits = response.getHits();
        ConversationAllRespDTO questionPageAllRespDTO = ConversationAllRespDTO.builder()
                .total(searchHits.getTotalHits() == null ?  0 :  searchHits.getTotalHits().value)
                .size(requestParam.getSize())
                .current(requestParam.getCurrent())
                .build();

        List<ConversationPageRespDTO> conversationPageRespDTOS = new ArrayList<>();
        searchHits.forEach(hit -> {
            String json = hit.getSourceAsString();
            ConversationPageRespDTO conversation = JSON.parseObject(json, ConversationPageRespDTO.class);

            // 高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("title");
                if (highlightField != null) {
                    String title = highlightField.fragments()[0].string();
                    conversation.setTitle(title);
                }
                highlightField = highlightFields.get("content");
                if (highlightField != null) {
                    String content = highlightField.fragments()[0].string();
                    conversation.setContent(content);
                }
            }
            Long id = conversation.getId();
            String likeStatus = Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(CONVERSATION_LIKE_SET_KEY + id, String.valueOf(UserContext.getUserId()))) ?   "已点赞" : "未点赞";
            conversation.setLikeStatus(likeStatus);
            Long count = stringRedisTemplate.opsForSet().size(CONVERSATION_LIKE_SET_KEY + id);
            conversation.setLikeCount(count == null ? 0 : count.intValue());

            conversationPageRespDTOS.add(conversation);
        });

        questionPageAllRespDTO.setRecords(conversationPageRespDTOS);
        return questionPageAllRespDTO;
    }

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
