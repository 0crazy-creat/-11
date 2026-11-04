package com.csp.awd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csp.awd.entity.AwdTrafficCapture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AwdTrafficCaptureMapper extends BaseMapper<AwdTrafficCapture> {
    
    /** 更新捕获状态为完成 */
    @Update("UPDATE awd_traffic_capture SET status = 'COMPLETED', " +
            "file_size = #{fileSize}, packet_count = #{packetCount}, " +
            "completed_at = NOW() WHERE id = #{id}")
    int markCompleted(@Param("id") Long id, 
                      @Param("fileSize") Long fileSize, 
                      @Param("packetCount") Integer packetCount);
    
    /** 更新捕获状态为失败 */
    @Update("UPDATE awd_traffic_capture SET status = 'FAILED', " +
            "error_message = #{errorMessage}, completed_at = NOW() WHERE id = #{id}")
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
