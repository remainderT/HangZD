package org.buaa.project.common.consts;

/**
 * Redis缓存常量
 */
public class RedisCacheConstants {

    /**
     * 用户注册分布式锁
     */
    public static final String USER_REGISTER_LOCK_KEY = "hangzd:user:register:lock:";

    /**
     * 用户注册验证码缓存
     */
    public static final String USER_REGISTER_CODE_KEY = "hangzd:user:register:code:";

    /**
     * 用户注册验证码发送计时
     */
    public static final String USER_CODE_TO_FREQUENT = "hangzd:user:register:frequent:";

    /**
     * 用户注册验证码缓存过期时间
     */
    public static final long USER_REGISTER_CODE_EXPIRE_KEY = 5L;

    /**
     * 用户登录缓存标识
     */
    public static final String USER_LOGIN_KEY = "hangzd:user:login:";

    /**
     * 用户登录缓存过期时间(天)
     */
    public static final long USER_LOGIN_EXPIRE_KEY = 30L;

    /**
     * 用户个人信息缓存标识
     */
    public static final String USER_INFO_KEY = "hangzd:user:info:";

    /**
     * 用户登录图片验证码
     */
    public static final String USER_LOGIN_CAPTCHA_KEY = "hangzd:user:login:captcha:";

    /**
     * 消息发送流缓存标识
     */
    public static final String MESSAGE_SEND_STREAM_KEY = "hangzd:stream:message-send";

    /**
     * 题目统计信息缓存标识
     */
    public static final String QUESTION_COUNT_KEY = "hangzd:question:count:";

    /**
     * 回答统计信息缓存标识
     */
    public static final String ANSWER_COUNT_KEY = "hangzd:answer:count:";

    /**
     * 题目点赞集合
     */
    public static final String QUESTION_LIKE_SET_KEY = "hangzd:question:like:";

    /**
     * 题目收藏集合
     */
    public static final String QUESTION_COLLECT_SET_KEY = "hangzd:question:collect:";

    /**
     * 会话点赞集合
     */
    public static final String CONVERSATION_LIKE_SET_KEY = "hangzd:conversation:like:";

    /**
     * 用户统计信息缓存标识
     */
    public static final String USER_COUNT_KEY = "hangzd:user:count:";

    /**
     * 用户行为分布式锁
     */
    public static final String USER_ACTION_KEY = "hangzd:user:action:";

}
