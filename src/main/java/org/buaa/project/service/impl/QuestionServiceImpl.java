package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import static org.buaa.project.common.enums.QuestionErrorCodeEnum.QUESTION_NULL;
import static org.buaa.project.common.enums.QuestionErrorCodeEnum.QUESTION_USER_INCORRECT;
import java.util.Objects;

/**
 * The type Question service.
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, QuestionDO> implements QuestionService {

    @Override
    public void uploadQuestion(QuestionUploadReqDTO requestParam) {
        QuestionDO question = BeanUtil.toBean(requestParam, QuestionDO.class);
        question.setUserId(UserContext.getUserId());
        question.setUsername(UserContext.getUsername());
        question.setSolvedFlag(0);
        baseMapper.insert(question);
    }

    @Override
    public void updateQuestion(QuestionUpdateReqDTO requestParam){
        Long id = requestParam.getId();
        checkQuestionExist(id);
        checkQuestionOwner(id);

        LambdaUpdateWrapper<QuestionDO> queryWrapper = Wrappers.lambdaUpdate(QuestionDO.class)
                .eq(QuestionDO::getId, requestParam.getId());
        QuestionDO questionDO = baseMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(requestParam, questionDO);
        baseMapper.update(questionDO, queryWrapper);
    }

    @Override
    public void deleteQuestion(Long id) {
        checkQuestionExist(id);
        if(!UserContext.getUserType().equals("admin")){
            checkQuestionOwner(id);
        }

        QuestionDO question = baseMapper.selectById(id);
        question.setDelFlag(1);
        baseMapper.updateById(question);
    }

    @Override
    public void resolvedQuestion(QuestionSolveReqDTO requestParam) {
        checkQuestionExist(requestParam.getId());
        checkQuestionOwner(requestParam.getId());

        QuestionDO question = baseMapper.selectById(requestParam.getId());
        question.setSolvedFlag(1);
        baseMapper.updateById(question);
    }

    @Override
    public void checkQuestionExist(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        if (Objects.isNull(question)) {
            throw new ServiceException(QUESTION_NULL);
        }
    }

    @Override
    public void checkQuestionOwner(Long id) {
        checkQuestionExist(id);
        Long currentUserId = UserContext.getUserId();
        QuestionDO question = baseMapper.selectById(id);
        if (!Objects.equals(currentUserId, question.getUserId())) {
            throw new ServiceException(QUESTION_USER_INCORRECT);
        }
    }
}
