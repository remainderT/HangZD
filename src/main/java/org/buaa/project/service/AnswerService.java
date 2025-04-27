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
    void updateAnswer(AnswerUpdateReqDTO requestParam);

    void postAnswer(AnswerUploadReqDTO message);

    AnswerRespDTO getAnswerById(Long id);

    void deleteAnswer(Long id);

}
