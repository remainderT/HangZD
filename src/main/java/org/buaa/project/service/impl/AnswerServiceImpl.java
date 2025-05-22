package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.AnswerMapper;
import org.buaa.project.dto.req.answer.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.answer.AnswerUploadReqDTO;
import org.buaa.project.dto.resp.AnswerRespDTO;
import org.buaa.project.service.AnswerService;
import org.buaa.project.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static org.buaa.project.common.enums.QAErrorCodeEnum.ANSWER_NULL;
import static org.buaa.project.common.enums.QAErrorCodeEnum.ANSWER_USER_INCORRECT;

/**
 * 回答接口实现层
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, AnswerDO> implements AnswerService {

    private final QuestionService questionService;

    @Override
    public AnswerRespDTO postAnswer(AnswerUploadReqDTO requestParam) {
        AnswerDO answerDO = BeanUtil.copyProperties(requestParam, AnswerDO.class);
        Long questionId = requestParam.getQuestionId();
        QuestionDO questionDO = questionService.getById(questionId);
        questionService.checkQuestionExist(questionDO);

        answerDO.setUserId(UserContext.getUserId());
        answerDO.setUsername(UserContext.getUsername());
        baseMapper.insert(answerDO);
        return BeanUtil.copyProperties(answerDO, AnswerRespDTO.class);
    }

    @Override
    public void updateAnswer(AnswerUpdateReqDTO requestParam) {
        AnswerDO answerDO = baseMapper.selectById(requestParam.getId());
        checkAnswerExist(answerDO);
        checkAnswerOwner(answerDO);

        BeanUtils.copyProperties(requestParam, answerDO);
        baseMapper.updateById(answerDO);
    }

    @Override
    public AnswerRespDTO getAnswerById(Long id) {
        AnswerDO answerDO = baseMapper.selectById(id);
        checkAnswerExist(answerDO);
        return BeanUtil.copyProperties(answerDO, AnswerRespDTO.class);
    }

    @Override
    public void deleteAnswer(Long id) {
        AnswerDO answerDO = baseMapper.selectById(id);
        checkAnswerExist(answerDO);
        checkAnswerOwner(answerDO);

        answerDO.setDelFlag(1);
        baseMapper.updateById(answerDO);
    }

    @Override
    public void checkAnswerExist(AnswerDO answerDO) {
        if (answerDO == null || answerDO.getDelFlag() != 0) {
            throw new ClientException(ANSWER_NULL);
        }
    }

    @Override
    public void checkAnswerOwner(AnswerDO answerDO) {
        long userId = UserContext.getUserId();
        if (!answerDO.getUserId().equals(userId)) {
            throw new ClientException(ANSWER_USER_INCORRECT);
        }
    }

}
