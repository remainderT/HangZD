package org.buaa.project.controller;

import lombok.RequiredArgsConstructor;
import org.buaa.project.service.AnswerService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回答管理控制层
 */
@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

}
