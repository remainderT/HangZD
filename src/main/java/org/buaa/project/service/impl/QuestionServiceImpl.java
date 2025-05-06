package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dto.req.question.AskUsersReqDTO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.dto.resp.QuestionUploadRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;
import org.buaa.project.service.QuestionService;
import org.buaa.project.service.UserActionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.buaa.project.common.enums.QAErrorCodeEnum.QUESTION_NULL;
import static org.buaa.project.common.enums.QAErrorCodeEnum.QUESTION_USER_INCORRECT;

/**
 * The type Question service.
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, QuestionDO> implements QuestionService {

    private final UserActionService userActionService;

    @Override
    public QuestionUploadRespDTO uploadQuestion(QuestionUploadReqDTO requestParam) {
        QuestionDO questionDO = BeanUtil.toBean(requestParam, QuestionDO.class);
        questionDO.setUserId(UserContext.getUserId());
        questionDO.setUsername(UserContext.getUsername());
        questionDO.setSolvedFlag(0);
        baseMapper.insert(questionDO);
        return BeanUtil.toBean(questionDO, QuestionUploadRespDTO.class);
    }

    @Override
    public void updateQuestion(QuestionUpdateReqDTO requestParam){
        LambdaUpdateWrapper<QuestionDO> queryWrapper = Wrappers.lambdaUpdate(QuestionDO.class)
                .eq(QuestionDO::getId, requestParam.getId());
        QuestionDO questionDO = baseMapper.selectOne(queryWrapper);
        checkQuestionExist(questionDO);
        checkQuestionOwner(questionDO);

        BeanUtils.copyProperties(requestParam, questionDO);
        baseMapper.update(questionDO, queryWrapper);
    }

    @Override
    public void deleteQuestion(Long id) {
        QuestionDO questionDO = baseMapper.selectById(id);
        checkQuestionExist(questionDO);
        if(!UserContext.getUserType().equals("admin")){
            checkQuestionOwner(questionDO);
        }

        questionDO.setDelFlag(1);
        baseMapper.updateById(questionDO);
    }

    @Override
    public void resolvedQuestion(QuestionSolveReqDTO requestParam) {
        QuestionDO questionDO = baseMapper.selectById(requestParam.getId());
        checkQuestionExist(questionDO);
        checkQuestionOwner(questionDO);

        questionDO.setSolvedFlag(1);
        baseMapper.updateById(questionDO);
    }

    @Override
    public void checkQuestionExist(QuestionDO questionDO) {
        if (Objects.isNull(questionDO)) {
            throw new ServiceException(QUESTION_NULL);
        }
    }

    @Override
    public void checkQuestionOwner(QuestionDO questionDO) {
        Long currentUserId = UserContext.getUserId();
        if (!Objects.equals(currentUserId, questionDO.getUserId())) {
            throw new ServiceException(QUESTION_USER_INCORRECT);
        }
    }

    @Override
    public QuestionRespDTO getQuestionById(Long id) {
        QuestionDO questionDO = baseMapper.selectById(id);
        checkQuestionExist(questionDO);
        QuestionRespDTO result = new QuestionRespDTO();
        BeanUtils.copyProperties(questionDO, result);
        return result;
    }

    @Override
    public List<UserRespDTO> findAnswerers(Long questionId){
        //TODO
        return new ArrayList<>();
    }

    @Override
    public void askUsers(AskUsersReqDTO requestParam) {
          Long questionId = requestParam.getQuestionId();
          List<Long> userIds = requestParam.getUserIds();
          QuestionDO questionDO = baseMapper.selectById(questionId);
          checkQuestionExist(questionDO);
          for (Long userId : userIds) {
              userActionService.recommendQuestion(questionId, userId);
          }
    }

}
