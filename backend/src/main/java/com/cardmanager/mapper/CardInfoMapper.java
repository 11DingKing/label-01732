package com.cardmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardmanager.entity.CardInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 卡密信息Mapper
 */
@Mapper
public interface CardInfoMapper extends BaseMapper<CardInfo> {

    /**
     * 检查卡号是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM card_info WHERE card_number = #{cardNumber}")
    boolean existsByCardNumber(String cardNumber);

    /**
     * 根据卡号查询（不判断删除状态）
     */
    @Select("SELECT * FROM card_info WHERE card_number = #{cardNumber} AND is_deleted = 0")
    CardInfo selectByCardNumber(String cardNumber);

    /**
     * 统计卡密总数
     */
    @Select("SELECT COUNT(*) FROM card_info WHERE is_deleted = 0")
    Long countTotal();

    /**
     * 按状态统计
     */
    @Select("SELECT COUNT(*) FROM card_info WHERE status = #{status} AND is_deleted = 0")
    Long countByStatus(Integer status);

    /**
     * 今日发卡数
     */
    @Select("SELECT COUNT(*) FROM card_info WHERE DATE(create_time) = CURDATE() AND is_deleted = 0")
    Long countTodayGenerated();

    /**
     * 今日核销数
     */
    @Select("SELECT COUNT(*) FROM card_info WHERE DATE(use_time) = CURDATE() AND status = 1 AND is_deleted = 0")
    Long countTodayUsed();

    /**
     * 查询已存在的卡号集合
     */
    @Select("<script>SELECT card_number FROM card_info WHERE card_number IN " +
            "<foreach collection='cardNumbers' item='cardNumber' open='(' separator=',' close=')'>" +
            "#{cardNumber}</foreach></script>")
    Set<String> selectExistingCardNumbers(@Param("cardNumbers") Set<String> cardNumbers);

    /**
     * 批量插入卡密（高性能批量插入，使用 VALUES (...), (...) 语法）
     * @param cardList 卡密列表
     * @return 插入条数
     */
    @Insert("<script>" +
            "INSERT INTO card_info (card_number, card_password, batch_number, status, " +
            "create_operator_id, create_operator_name, create_time, is_deleted) VALUES " +
            "<foreach collection='cardList' item='card' separator=','>" +
            "(#{card.cardNumber}, #{card.cardPassword}, #{card.batchNumber}, #{card.status}, " +
            "#{card.createOperatorId}, #{card.createOperatorName}, #{card.createTime}, #{card.isDeleted})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("cardList") List<CardInfo> cardList);
}
