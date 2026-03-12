package com.cardmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardmanager.entity.CardBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 卡密批次Mapper
 */
@Mapper
public interface CardBatchMapper extends BaseMapper<CardBatch> {

    /**
     * 根据批次号查询
     */
    @Select("SELECT * FROM card_batch WHERE batch_number = #{batchNumber} AND is_deleted = 0")
    CardBatch selectByBatchNumber(String batchNumber);

    /**
     * 增加已核销数量
     */
    @Update("UPDATE card_batch SET used_count = used_count + 1 WHERE batch_number = #{batchNumber}")
    int incrementUsedCount(String batchNumber);

    /**
     * 批量更新回收数量
     */
    @Update("UPDATE card_batch SET recycled_count = (SELECT COUNT(*) FROM card_info WHERE batch_number = #{batchNumber} AND status = 2 AND is_deleted = 0) WHERE batch_number = #{batchNumber}")
    int updateRecycledCount(String batchNumber);

    /**
     * 统计批次总数
     */
    @Select("SELECT COUNT(*) FROM card_batch WHERE is_deleted = 0")
    Long countTotal();
}
