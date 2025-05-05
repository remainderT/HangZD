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


    public void askUsers(AskUsersReqDTO requestParam) {
//        Long aid = requestParam.getAid();
//        Long qid = requestParam.getQid();
//        List<Long> ids = requestParam.getIds();
//        System.out.println(aid);
//        System.out.println(qid);
//        System.out.println(ids);
//        // 检查提问者是否存在
//        LambdaQueryWrapper<UserDO> askerQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
//                .eq(UserDO::getId, aid);
//        UserDO asker = baseMapper.selectOne(askerQueryWrapper);
//        System.out.println(asker);
//        if (asker == null) {
//            throw new ClientException(USER_NULL);
//        }
//        // 检查问题是否存在
//        LambdaQueryWrapper<QuestionDO> questionQueryWrapper = Wrappers.lambdaQuery(QuestionDO.class)
//                .eq(QuestionDO::getId, qid);
//        QuestionDO question = questionMapper.selectOne(questionQueryWrapper);
//        if (question == null) {
//            throw new ClientException(QUESTION_NULL);
//        }
//
//        for(Long answerid: ids) {
//            // 检查回答者是否存在
//            LambdaQueryWrapper<UserDO> answererQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
//                    .eq(UserDO::getId, answerid);
//            UserDO answerer = baseMapper.selectOne(answererQueryWrapper);
//            if (answerer == null) {
//                throw new ClientException(USER_NULL);
//            }
//
//            //根据回答者id和提问生成answerDO
//            AnswerDO answerDO = new AnswerDO();
//            answerDO.setUserId(answerid);
//            answerDO.setUsername(answerer.getUsername());
//            answerDO.setQuestionId(qid);
//            //answerDO.setContent(question.getContent());
//            //插入Answer表
//            int inserted = answerMapper.insert(answerDO);
//            if (inserted < 1) {
//                throw new ClientException(ANSWER_POST_FAIL);
//            }
//        }

    }

}
