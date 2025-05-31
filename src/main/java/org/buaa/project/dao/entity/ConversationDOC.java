package org.buaa.project.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDOC {

    /**
     * 会话id
     */
    private Long id;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题内容
     */
    private String content;

    /**
     * 创建日期
     */
    private Long createTime;

    /**
     * 自动补全
     */
    private List<String> suggestion;
}
