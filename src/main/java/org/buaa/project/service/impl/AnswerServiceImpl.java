package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dao.mapper.AnswerMapper;
import org.buaa.project.dto.req.answer.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.answer.AnswerUploadReqDTO;
import org.buaa.project.service.AnswerService;
import org.springframework.stereotype.Service;

/**
 * 回答接口实现层
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, AnswerDO> implements AnswerService {


    @Override
    public void updateAnswer(AnswerUpdateReqDTO requestParam) {
        //TODO
    }

    @Override
    public void postAnswer(AnswerUploadReqDTO message) {
        //TODO

    }

    @Override
    public AnswerDO getAnswerById(Long id) {
        //TODO
        return null;
    }

    @Override
    public void deleteAnswer(Long id) {
        //TODO
    }
}
