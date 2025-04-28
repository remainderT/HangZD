package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.common.enums.AnswerErrorCodeEnum;
import org.buaa.project.common.enums.QuestionErrorCodeEnum;
import org.buaa.project.common.enums.UserErrorCodeEnum;
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dao.mapper.AnswerMapper;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.dto.req.answer.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.answer.AnswerUploadReqDTO;
import org.buaa.project.dto.resp.AnswerRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;
import org.buaa.project.service.AnswerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 回答接口实现层
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, AnswerDO> implements AnswerService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public void updateAnswer(AnswerUpdateReqDTO requestParam) {
        //TODO
    }

    @Override
    public void postAnswer(AnswerUploadReqDTO answer) {
        AnswerDO answerDO = new AnswerDO();

        //检查用户是否存在
        if (userMapper.selectById(answer.getUser_id()) == null) {
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        answerDO.setUserId(answer.getUser_id());
        answerDO.setUsername(answer.getUsername());

        //检查问题是否存在
        if (questionMapper.selectById(answer.getQuestion_id()) == null) {
            throw new ServiceException(QuestionErrorCodeEnum.QUESTION_NULL);
        }
        answerDO.setQuestionId(answer.getQuestion_id());
        answerDO.setContent(answer.getContent());
        answerDO.setImages(answer.getImages());
        answerDO.setLikeCount(0);
        answerDO.setUseful(0);
        //写入Answer表
        int inserted = baseMapper.insert(answerDO);
        if(inserted < 1) {
            throw new ServiceException(AnswerErrorCodeEnum.ANSWER_POST_FAIL);
        }
    }

    @Override
    public AnswerRespDTO getAnswerById(Long id) {
        AnswerDO answerDO = baseMapper.selectById(id);
        if (answerDO == null) {
            throw new ServiceException(AnswerErrorCodeEnum.ANSWER_NULL);
        }
        AnswerRespDTO result = new AnswerRespDTO();
        BeanUtils.copyProperties(answerDO, result);
        return result;
    }

    @Override
    public void deleteAnswer(Long id) {
        //TODO
    }
}
