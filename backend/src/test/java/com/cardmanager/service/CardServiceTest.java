package com.cardmanager.service;

import com.cardmanager.common.Constants;
import com.cardmanager.dto.CardGenerateDTO;
import com.cardmanager.dto.CardQueryDTO;
import com.cardmanager.entity.CardBatch;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.impl.CardServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 卡密服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("卡密服务测试")
class CardServiceTest {

    @Mock
    private CardInfoMapper cardInfoMapper;

    @Mock
    private CardBatchMapper cardBatchMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private MockedStatic<UserContext> userContextMock;

    @BeforeEach
    void setUp() {
        userContextMock = mockStatic(UserContext.class);
        userContextMock.when(UserContext::getUserId).thenReturn(1L);
        userContextMock.when(UserContext::getRealName).thenReturn("测试用户");
    }

    @AfterEach
    void tearDown() {
        userContextMock.close();
    }

    @Test
    @DisplayName("发卡成功 - 返回批次号")
    void generateCards_Success() {
        // 准备
        CardGenerateDTO dto = new CardGenerateDTO();
        dto.setCount(10);

        Set<String> existingNumbers = new HashSet<>();
        when(cardInfoMapper.selectExistingCardNumbers(any())).thenReturn(existingNumbers);
        when(cardInfoMapper.insert(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.insert(any(CardBatch.class))).thenReturn(1);

        // 执行
        String batchNumber = cardService.generateCards(dto);

        // 验证
        assertNotNull(batchNumber);
        assertEquals(14, batchNumber.length());
        verify(cardInfoMapper, times(10)).insert(any(CardInfo.class));
        verify(cardBatchMapper, times(1)).insert(any(CardBatch.class));
    }

    @Test
    @DisplayName("回收单张卡密 - 未使用状态")
    void recycleSingle_UnusedCard_Success() {
        // 准备
        String cardNumber = "123456789";
        CardInfo card = new CardInfo();
        card.setId(1L);
        card.setCardNumber(cardNumber);
        card.setBatchNumber("20240101120000");
        card.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber(cardNumber)).thenReturn(card);
        when(cardInfoMapper.updateById(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.updateRecycledCount(anyString())).thenReturn(1);

        // 执行
        assertDoesNotThrow(() -> cardService.recycleSingle(cardNumber));

        // 验证
        verify(cardInfoMapper).updateById(argThat(c -> c.getStatus() == Constants.CardStatus.RECYCLED));
    }

    @Test
    @DisplayName("回收单张卡密 - 卡密不存在")
    void recycleSingle_CardNotFound_ThrowsException() {
        // 准备
        String cardNumber = "999999999";
        when(cardInfoMapper.selectByCardNumber(cardNumber)).thenReturn(null);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> cardService.recycleSingle(cardNumber));
        assertEquals("卡密不存在", exception.getMessage());
    }

    @Test
    @DisplayName("回收单张卡密 - 已核销状态")
    void recycleSingle_UsedCard_ThrowsException() {
        // 准备
        String cardNumber = "123456789";
        CardInfo card = new CardInfo();
        card.setCardNumber(cardNumber);
        card.setStatus(Constants.CardStatus.USED);

        when(cardInfoMapper.selectByCardNumber(cardNumber)).thenReturn(card);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> cardService.recycleSingle(cardNumber));
        assertEquals("已核销的卡密不能回收", exception.getMessage());
    }

    @Test
    @DisplayName("回收单张卡密 - 已回收状态")
    void recycleSingle_RecycledCard_ThrowsException() {
        // 准备
        String cardNumber = "123456789";
        CardInfo card = new CardInfo();
        card.setCardNumber(cardNumber);
        card.setStatus(Constants.CardStatus.RECYCLED);

        when(cardInfoMapper.selectByCardNumber(cardNumber)).thenReturn(card);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> cardService.recycleSingle(cardNumber));
        assertEquals("卡密已被回收", exception.getMessage());
    }

    @Test
    @DisplayName("批量回收 - 批次不存在")
    void recycleBatch_BatchNotFound_ThrowsException() {
        // 准备
        String batchNumber = "99999999999999";
        when(cardBatchMapper.selectByBatchNumber(batchNumber)).thenReturn(null);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> cardService.recycleBatch(batchNumber));
        assertEquals("批次不存在", exception.getMessage());
    }

    @Test
    @DisplayName("查询卡密列表 - 无条件查询")
    void listCards_NoCondition() {
        // 准备
        CardQueryDTO dto = new CardQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        // 执行不会抛出异常
        assertDoesNotThrow(() -> cardService.listCards(dto));
    }
}
