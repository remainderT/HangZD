package org.buaa.project.service;

import java.util.List;

/**
 * es服务接口层
 */
public interface EsService {

    /**
     * 自动补全
     */
    List<String> autoComplete(String keyword);

    /**
     * 分词
     */
    List<String> analyze(String text);
}
