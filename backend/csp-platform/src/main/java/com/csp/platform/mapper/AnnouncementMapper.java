package com.csp.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.platform.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/** 公告 Mapper。 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}