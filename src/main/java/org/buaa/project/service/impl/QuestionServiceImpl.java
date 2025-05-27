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
import org.buaa.project.dto.req.question.*;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.dto.resp.QuestionUploadRespDTO;
import org.buaa.project.dto.resp.UserRecommendedRespDTO;
import org.buaa.project.service.QuestionService;
import org.buaa.project.service.UserActionService;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.*;

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
        if(requestParam.getSatisfied()) {
            questionDO.setSolvedFlag(1);
        }   else {
            questionDO.setSolvedFlag(2);
        }
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
    public void askUsers(AskUsersReqDTO requestParam) {
          Long questionId = requestParam.getQuestionId();
          List<Long> userIds = requestParam.getUserIds();
          QuestionDO questionDO = baseMapper.selectById(questionId);
          checkQuestionExist(questionDO);
          for (Long userId : userIds) {
              userActionService.recommendQuestion(questionId, userId);
          }
    }
    
    @Override
    public List<UserRecommendedRespDTO> recommendUsers(RecommendUsersReqDTO requestParam){
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:4999/recommend";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("username", UserContext.getUsername());
        
        Map<String, String> body = new HashMap<>();
        body.put("question", requestParam.getQuestion());
        
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        // 发起 POST 请求
        ResponseEntity<List<UserRecommendedRespDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );
        
        return response.getBody();
    }
    
    @Override
    public List<QuestionRespDTO> fetchPreviousQuestions(RecommendUsersReqDTO requestParam) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:4999/fetch_previous_questions";  // 假设接口路径
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("username", UserContext.getUsername());
        
        // 构造请求体
        Map<String, String> body = new HashMap<>();
        body.put("question", requestParam.getQuestion());
        
        // 封装请求实体
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        // 发送 POST 请求，接收 List<QuestionRespDTO>
        ResponseEntity<List<QuestionRespDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );
        
        return response.getBody();
    }
    
}
