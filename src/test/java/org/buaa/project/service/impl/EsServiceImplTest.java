package org.buaa.project.service.impl;

import org.buaa.project.dao.entity.ConversationDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dto.req.conversation.ConversationPageReqDTO;
import org.buaa.project.dto.resp.ConversationAllRespDTO;
import org.buaa.project.dto.resp.ConversationPageRespDTO;
import org.buaa.project.service.EsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EsServiceImpl集成测试
 * 直接调用insert方法插入数据到Elasticsearch
 */
@SpringBootTest
@Transactional // 测试后回滚数据库操作
class EsServiceImplTest {

    @Autowired
    private EsService esService;

    @Autowired
    private QuestionMapper questionMapper;

    @Test
    void testSearch() {
        int i = 0;
        // 创建问题
        QuestionDO questionDO = new QuestionDO();
        questionDO.setTitle("Java开发问题 " + i + ": Spring Boot微服务架构");
        questionDO.setContent("这是关于Spring Boot微服务架构的问题，包含服务发现、配置管理、API网关等内容。");
        questionDO.setUserId(1000L + i);
        questionDO.setUsername("开发者" + i);
        questionDO.setCategoryId(1L);
        questionDO.setViewCount(i * 15);
        questionDO.setLikeCount(i * 3);
        questionDO.setAnswerCount(i * 2);
        questionDO.setSolvedFlag(0);

        // 插入问题到数据库
        questionMapper.insert(questionDO);
        Long questionId = questionDO.getId();

        // 创建会话
        ConversationDO conversationDO = new ConversationDO();
        conversationDO.setId((long) i);
        conversationDO.setUser1(1000L + i);
        conversationDO.setUser2(2000L + i);
        conversationDO.setQuestionId(questionId);
        conversationDO.setStatus(2);
        conversationDO.setDefaultPublic(1);
        conversationDO.setAnswererPublic(1);
        conversationDO.setLikeCount(i * 4);

        // 插入到ES
        try {
            esService.insert(conversationDO);
            System.out.println("✅ 插入测试数据 " + i + " 成功");
        } catch (Exception e) {
            System.err.println("❌ 插入测试数据 " + i + " 失败: " + e.getMessage());
        }

        // 等待ES索引刷新
        try {
            Thread.sleep(2000);
            System.out.println("等待ES索引刷新完成...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 2. 测试搜索功能
        System.out.println("\n=== 开始搜索测试 ===");
        
        // 测试1: 搜索"Spring Boot"
        testSearchWithKeyword("Spring Boot");
        // 测试5: 搜索不存在的关键词
        testSearchWithKeyword("不存在的关键词");
    }

    private void testSearchWithKeyword(String keyword) {
        System.out.println("\n--- 搜索关键词: \"" + keyword + "\" ---");
        
        // 创建搜索请求
        ConversationPageReqDTO searchReq = new ConversationPageReqDTO();
        searchReq.setKeyword(keyword);
        searchReq.setCurrent(1);  // 第一页
        searchReq.setSize(10);    // 每页10条
        
        try {
            ConversationAllRespDTO result = esService.search(searchReq);
            
            System.out.println("搜索结果统计:");
            System.out.println("- 总数: " + result.getTotal());
            System.out.println("- 当前页: " + result.getCurrent());
            System.out.println("- 每页大小: " + result.getSize());
            
            List<ConversationPageRespDTO> records = result.getRecords();
            if (records != null && !records.isEmpty()) {
                System.out.println("- 实际返回: " + records.size() + " 条记录");
                
                // 显示前3条结果
                int showCount = Math.min(3, records.size());
                System.out.println("前 " + showCount + " 条结果:");
                for (int i = 0; i < showCount; i++) {
                    ConversationPageRespDTO record = records.get(i);
                    System.out.println("  " + (i + 1) + ". " + record.getTitle());
                    if (record.getContent() != null && record.getContent().length() > 50) {
                        System.out.println("     " + record.getContent().substring(0, 50) + "...");
                    } else {
                        System.out.println("     " + record.getContent());
                    }
                }
                System.out.println("✅ 搜索成功！");
            } else {
                System.out.println("- 未找到匹配的记录");
                System.out.println("✅ 搜索完成（无结果）");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 搜索失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

} 