package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;

import java.util.List;

/**
 * 问题接口层
 */
public interface QuestionService extends IService<QuestionDO> {

    /**
     * 上传问题
     */
    Long uploadQuestion(QuestionUploadReqDTO requestParam);

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
    void checkQuestionExist(QuestionDO questionDO);

    /**
     * 检查问题是否为当前用户所有
     */
    void checkQuestionOwner(QuestionDO questionDO);

    /**
     * 返回指定id问题的详细信息
     */
    QuestionRespDTO getQuestionById(Long id);

    /**
     * 回答推荐
     */
    List<UserRespDTO> findAnswerers( Long questionId);
}
