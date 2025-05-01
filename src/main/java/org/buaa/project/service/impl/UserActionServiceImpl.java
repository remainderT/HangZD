package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.dao.entity.UserActionDO;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.service.UserActionService;
import org.springframework.stereotype.Service;

/**
 * 用户行为服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl extends ServiceImpl<UserActionMapper, UserActionDO> implements UserActionService {


} 