package com.cardmanager.service;

import com.cardmanager.common.Constants;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.impl.VerifyServiceImpl;
import com.cardmanager.vo.PublicCardVO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 核销服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("核销服务测试")
class VerifyServiceTest {

    @Mock
    private CardInfoMapper cardInfoMapper;

    @Mock
    private CardBatchMapper cardBatchMapper;

    @InjectMocks
    private VerifyServiceImpl verifyService;

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
    @DisplayName("核销成功 - 未使用的卡密")
    void verifyCard_UnusedCard_Success() {
        // 准备
        CardVerifyDTO dto = new CardVerifyDTO();
        dto.setCardNumber("123456789");
        dto.setCardPassword("ABC123");

        CardInfo card = new CardInfo();
        card.setId(1L);
        card.setCardNumber("123456789");
        card.setCardPassword("ABC123");
        card.setBatchNumber("20240101120000");
        card.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("123456789")).thenReturn(card);
        when(cardInfoMapper.updateById(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.incrementUsedCount(anyString())).thenReturn(1);

        // 执行
        assertDoesNotThrow(() -> verifyService.verifyCard(dto));

        // 验证
        verify(cardInfoMapper).updateById(argThat(c -> 
            c.getStatus() == Constants.CardStatus.USED && 
            c.getUseTime() != null
        ));
        verify(cardBatchMapper).incrementUsedCount("20240101120000");
    }

    @Test
    @DisplayName("核销失败 - 卡密不存在")
    void verifyCard_CardNotFound_ThrowsException() {
        // 准备
        CardVerifyDTO dto = new CardVerifyDTO();
        dto.setCardNumber("999999999");
        dto.setCardPassword("ABC123");

        when(cardInfoMapper.selectByCardNumber("999999999")).thenReturn(null);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> verifyService.verifyCard(dto));
        assertEquals("卡密不存在", exception.getMessage());
    }

    @Test
    @DisplayName("核销失败 - 密码错误")
    void verifyCard_WrongPassword_ThrowsException() {
        // 准备
        CardVerifyDTO dto = new CardVerifyDTO();
        dto.setCardNumber("123456789");
        dto.setCardPassword("WRONG1");

        CardInfo card = new CardInfo();
        card.setCardNumber("123456789");
        card.setCardPassword("ABC123");
        card.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("123456789")).thenReturn(card);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> verifyService.verifyCard(dto));
        assertEquals("卡号或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("核销失败 - 已核销的卡密")
    void verifyCard_UsedCard_ThrowsException() {
        // 准备
        CardVerifyDTO dto = new CardVerifyDTO();
        dto.setCardNumber("123456789");
        dto.setCardPassword("ABC123");

        CardInfo card = new CardInfo();
        card.setCardNumber("123456789");
        card.setCardPassword("ABC123");
        card.setStatus(Constants.CardStatus.USED);

        when(cardInfoMapper.selectByCardNumber("123456789")).thenReturn(card);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> verifyService.verifyCard(dto));
        assertEquals("卡密已被核销", exception.getMessage());
    }

    @Test
    @DisplayName("核销失败 - 已回收的卡密")
    void verifyCard_RecycledCard_ThrowsException() {
        // 准备
        CardVerifyDTO dto = new CardVerifyDTO();
        dto.setCardNumber("123456789");
        dto.setCardPassword("ABC123");

        CardInfo card = new CardInfo();
        card.setCardNumber("123456789");
        card.setCardPassword("ABC123");
        card.setStatus(Constants.CardStatus.RECYCLED);

        when(cardInfoMapper.selectByCardNumber("123456789")).thenReturn(card);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> verifyService.verifyCard(dto));
        assertEquals("卡密已被回收", exception.getMessage());
    }

    @Test
    @DisplayName("公开查询 - 成功")
    void queryCardStatus_Success() {
        // 准备
        CardInfo card = new CardInfo();
        card.setCardNumber("123456789");
        card.setCardPassword("ABC123");
        card.setStatus(Constants.CardStatus.UNUSED);
        card.setCreateTime(LocalDateTime.now());

        when(cardInfoMapper.selectByCardNumber("123456789")).thenReturn(card);

        // 执行
        PublicCardVO result = verifyService.queryCardStatus("123456789", "ABC123");

        // 验证
        assertNotNull(result);
        assertEquals("123456789", result.getCardNumber());
        assertEquals("未使用", result.getStatusName());
    }

    @Test
    @DisplayName("公开查询 - 密码错误")
    void queryCardStatus_WrongPassword_ThrowsException() {
        // 准备
        CardInfo card = new CardInfo();
        card.setCardNumber("123456789");
        card.setCardPassword("ABC123");

        when(cardInfoMapper.selectByCardNumber("123456789")).thenReturn(card);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> verifyService.queryCardStatus("123456789", "WRONG1"));
        assertEquals("卡号或密码错误", exception.getMessage());
    }
}
