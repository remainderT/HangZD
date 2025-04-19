package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.service.MessageService;
import org.springframework.stereotype.Service;

/**
 * 消息接口实现层
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl  extends ServiceImpl<MessageMapper, MessageDO> implements MessageService {

}
