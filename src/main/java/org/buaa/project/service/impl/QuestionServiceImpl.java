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

    @Override
    public QuestionRespDTO getQuestionById(Long id) {
        checkQuestionExist(id);
        QuestionDO question = baseMapper.selectById(id);
        QuestionRespDTO result = new QuestionRespDTO();
        BeanUtils.copyProperties(question, result);
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
