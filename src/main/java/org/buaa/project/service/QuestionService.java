package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dto.req.question.*;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.dto.resp.QuestionUploadRespDTO;
import org.buaa.project.dto.resp.UserRecommendedRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 问题接口层
 */
public interface QuestionService extends IService<QuestionDO> {

    /**
     * 上传问题
     */
    QuestionUploadRespDTO uploadQuestion(QuestionUploadReqDTO requestParam);

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
     * 向用户提问
     */
    void askUsers(AskUsersReqDTO requestParam);
    
    /**
     * 根据问题推荐回答者
     */
    List<UserRecommendedRespDTO> recommendUsers(RecommendUsersReqDTO requestParam);
    
    /**
     * 根据问题返回类似过往问题
     */
    List<QuestionRespDTO> fetchPreviousQuestions(RecommendUsersReqDTO requestParam);
}
