/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.buaa.project.common.enums;


import org.buaa.project.common.convention.errorcode.IErrorCode;

/**
 * 用户错误码
 */
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NAME_EXIST("A000101", "用户名已存在"),

    USER_EXIST("A000102", "用户记录已存在"),

    USER_SAVE_ERROR("A000103", "用户记录新增失败"),

    USER_CODE_ERROR("A000104", "验证码错误"),

    USER_CODE_TOO_FREQUENT("A000105", "验证码发送过于频繁"),

    USER_INVALID_DATA("A000106", "用户数据不合法"),

    USER_NAME_NULL("A000201", "用户名不存在"),

    USER_PASSWORD_ERROR("A000202", "密码错误"),

    USER_REPEATED_LOGIN("A000203", "重复登录"),

    USER_TOKEN_NULL("A000204", "用户未登录"),

    USER_LOGIN_CAPTCHA_ERROR("A000205","用户登录验证码错误"),

    USER_NULL("A000301", "用户记录不存在"),

    USER_UPDATE_ERROR("A000302", "用户信息更新失败");

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }


}
