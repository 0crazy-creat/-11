package com.csp.ctf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.ctf.entity.DeviceFingerprint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceFingerprintMapper extends BaseMapper<DeviceFingerprint> {
    
    /** 查询某设备在某赛事中关联的所有用户（用于多账号检测） */
    @Select("SELECT DISTINCT user_id FROM device_fingerprint " +
            "WHERE device_id = #{deviceId} AND contest_id = #{contestId}")
    List<Long> selectUsersByDeviceAndContest(@Param("deviceId") String deviceId,
                                              @Param("contestId") Long contestId);
    
    /** 查询某用户使用的的所有设备（用于设备切换检测） */
    @Select("SELECT DISTINCT device_id FROM device_fingerprint " +
            "WHERE user_id = #{userId} AND contest_id = #{contestId}")
    List<String> selectDevicesByUserAndContest(@Param("userId") Long userId,
                                                @Param("contestId") Long contestId);
    
    /** 统计某设备关联的用户数 */
    @Select("SELECT COUNT(DISTINCT user_id) FROM device_fingerprint " +
            "WHERE device_id = #{deviceId} AND contest_id = #{contestId}")
    Integer countUsersByDevice(@Param("deviceId") String deviceId,
                               @Param("contestId") Long contestId);
}
