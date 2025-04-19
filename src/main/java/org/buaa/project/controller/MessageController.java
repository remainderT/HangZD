package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.service.MessageService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息控制层
 */
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

}
