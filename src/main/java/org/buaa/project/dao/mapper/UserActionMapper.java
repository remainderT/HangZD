package org.buaa.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.buaa.project.dao.entity.UserActionDO;

/**
 * 用户行为Mapper接口
 */
@Mapper
public interface UserActionMapper extends BaseMapper<UserActionDO> {
} 