package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dto.req.answer.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.answer.AnswerUploadReqDTO;
import org.buaa.project.dto.resp.AnswerRespDTO;

/**
 * 回答接口层
 */
public interface AnswerService extends IService<AnswerDO> {

    /**
     * 发布回答
     */
    AnswerRespDTO postAnswer(AnswerUploadReqDTO requestParam);

    /**
     * 更新回答
     */
    void updateAnswer(AnswerUpdateReqDTO requestParam);

    /**
     * 根据ID获取回答
     */
    AnswerRespDTO getAnswerById(Long id);

    /**
     * 删除回答
     */
    void deleteAnswer(Long id);

    /**
     * 检查回答是否存在
     */
    void checkAnswerExist(AnswerDO answerDO);

    /**
     * 检查回答所有者
     */
    void checkAnswerOwner(AnswerDO answerDO);


}
