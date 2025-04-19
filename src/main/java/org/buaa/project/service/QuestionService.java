package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;

/**
 * 问题接口层
 */
public interface QuestionService extends IService<QuestionDO> {

    /**
     * 上传问题
     */
    void uploadQuestion(QuestionUploadReqDTO requestParam);

    /**
     * 修改问题
     */
    void updateQuestion(QuestionUpdateReqDTO requestParam);

    /**
     * 删除问题
     */
    void deleteQuestion(Long id);

    /**
     * 标记问题已经解决
     */
    void resolvedQuestion(QuestionSolveReqDTO requestParam);

    /**
     * 检查问题是否存在
     */
    void checkQuestionExist(Long id);

    /**
     * 检查问题是否为当前用户所有
     */
    void checkQuestionOwner(Long id);

}
