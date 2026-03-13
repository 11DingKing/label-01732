package com.cardmanager.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.cardmanager.common.Constants;
import com.cardmanager.dto.BatchVerifyResultDTO;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.impl.VerifyServiceImpl;
import com.cardmanager.vo.CardVerifyExcelVO;
import com.cardmanager.vo.PublicCardVO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // ==================== 批量核销测试 ====================

    private MockMultipartFile createTestExcelFile(List<CardVerifyExcelVO> data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, CardVerifyExcelVO.class).sheet("Sheet1").doWrite(data);
        return new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(out.toByteArray()));
    }

    @Test
    @DisplayName("批量核销 - 全部成功")
    void batchVerifyCard_AllSuccess() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("111111111");
        card1.setCardPassword("AAA111");
        excelData.add(card1);

        CardVerifyExcelVO card2 = new CardVerifyExcelVO();
        card2.setCardNumber("222222222");
        card2.setCardPassword("BBB222");
        excelData.add(card2);

        MockMultipartFile file = createTestExcelFile(excelData);

        // Mock数据
        CardInfo cardInfo1 = new CardInfo();
        cardInfo1.setCardNumber("111111111");
        cardInfo1.setCardPassword("AAA111");
        cardInfo1.setBatchNumber("BATCH001");
        cardInfo1.setStatus(Constants.CardStatus.UNUSED);

        CardInfo cardInfo2 = new CardInfo();
        cardInfo2.setCardNumber("222222222");
        cardInfo2.setCardPassword("BBB222");
        cardInfo2.setBatchNumber("BATCH002");
        cardInfo2.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("111111111")).thenReturn(cardInfo1);
        when(cardInfoMapper.selectByCardNumber("222222222")).thenReturn(cardInfo2);
        when(cardInfoMapper.updateById(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.incrementUsedCount(anyString())).thenReturn(1);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        assertEquals(2, result.getSuccessCards().size());
        assertTrue(result.getFailRecords().isEmpty());

        verify(cardInfoMapper, times(2)).updateById(any(CardInfo.class));
        verify(cardBatchMapper, times(2)).incrementUsedCount(anyString());
    }

    @Test
    @DisplayName("批量核销 - 数据格式错误")
    void batchVerifyCard_InvalidDataFormat() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("123"); // 卡号太短
        card1.setCardPassword("AAA111");
        excelData.add(card1);

        CardVerifyExcelVO card2 = new CardVerifyExcelVO();
        card2.setCardNumber("444444444");
        card2.setCardPassword("123"); // 密码太短
        excelData.add(card2);

        MockMultipartFile file = createTestExcelFile(excelData);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(2, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(2, result.getFailCount());
        assertEquals(2, result.getFailRecords().size());

        verify(cardInfoMapper, never()).selectByCardNumber(anyString());
        verify(cardInfoMapper, never()).updateById(any(CardInfo.class));
    }

    @Test
    @DisplayName("批量核销 - Excel中重复卡号")
    void batchVerifyCard_DuplicateCardsInExcel() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("111111111");
        card1.setCardPassword("AAA111");
        excelData.add(card1);

        CardVerifyExcelVO card2 = new CardVerifyExcelVO();
        card2.setCardNumber("111111111"); // 重复卡号
        card2.setCardPassword("AAA111");
        excelData.add(card2);

        MockMultipartFile file = createTestExcelFile(excelData);

        // Mock数据
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardNumber("111111111");
        cardInfo.setCardPassword("AAA111");
        cardInfo.setBatchNumber("BATCH001");
        cardInfo.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("111111111")).thenReturn(cardInfo);
        when(cardInfoMapper.updateById(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.incrementUsedCount(anyString())).thenReturn(1);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(1, result.getSuccessCards().size());
        assertEquals(1, result.getFailRecords().size());
        assertEquals("Excel中存在重复卡号", result.getFailRecords().get(0).getErrorMessage());

        verify(cardInfoMapper, times(1)).updateById(any(CardInfo.class));
        verify(cardBatchMapper, times(1)).incrementUsedCount(anyString());
    }

    @Test
    @DisplayName("批量核销 - 卡密不存在")
    void batchVerifyCard_CardNotFound() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("111111111");
        card1.setCardPassword("AAA111");
        excelData.add(card1);

        CardVerifyExcelVO card2 = new CardVerifyExcelVO();
        card2.setCardNumber("999999999");
        card2.setCardPassword("ZZZ999");
        excelData.add(card2);

        MockMultipartFile file = createTestExcelFile(excelData);

        // Mock数据
        CardInfo cardInfo1 = new CardInfo();
        cardInfo1.setCardNumber("111111111");
        cardInfo1.setCardPassword("AAA111");
        cardInfo1.setBatchNumber("BATCH001");
        cardInfo1.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("111111111")).thenReturn(cardInfo1);
        when(cardInfoMapper.selectByCardNumber("999999999")).thenReturn(null);
        when(cardInfoMapper.updateById(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.incrementUsedCount(anyString())).thenReturn(1);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals(1, result.getSuccessCards().size());
        assertEquals(1, result.getFailRecords().size());
        assertEquals("卡密不存在", result.getFailRecords().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("批量核销 - 密码错误")
    void batchVerifyCard_WrongPassword() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("111111111");
        card1.setCardPassword("WRONG1"); // 错误密码
        excelData.add(card1);

        MockMultipartFile file = createTestExcelFile(excelData);

        // Mock数据
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardNumber("111111111");
        cardInfo.setCardPassword("AAA111"); // 正确密码
        cardInfo.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("111111111")).thenReturn(cardInfo);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("卡号或密码错误", result.getFailRecords().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("批量核销 - 已核销的卡密")
    void batchVerifyCard_AlreadyUsed() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("111111111");
        card1.setCardPassword("AAA111");
        excelData.add(card1);

        MockMultipartFile file = createTestExcelFile(excelData);

        // Mock数据
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardNumber("111111111");
        cardInfo.setCardPassword("AAA111");
        cardInfo.setStatus(Constants.CardStatus.USED); // 已核销状态

        when(cardInfoMapper.selectByCardNumber("111111111")).thenReturn(cardInfo);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
        assertEquals("卡密已被核销", result.getFailRecords().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("批量核销 - 空文件")
    void batchVerifyCard_EmptyFile() throws IOException {
        // 准备空的Excel文件
        List<CardVerifyExcelVO> excelData = new ArrayList<>();
        MockMultipartFile file = createTestExcelFile(excelData);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
                () -> verifyService.batchVerifyCard(file));
        assertEquals("Excel文件中没有有效的卡密数据", exception.getMessage());
    }

    @Test
    @DisplayName("批量核销 - 混合成功和失败")
    void batchVerifyCard_MixedResults() throws IOException {
        // 准备测试数据
        List<CardVerifyExcelVO> excelData = new ArrayList<>();

        // 成功的卡
        CardVerifyExcelVO card1 = new CardVerifyExcelVO();
        card1.setCardNumber("111111111");
        card1.setCardPassword("AAA111");
        excelData.add(card1);

        // 格式错误
        CardVerifyExcelVO card2 = new CardVerifyExcelVO();
        card2.setCardNumber("123");
        card2.setCardPassword("BBB222");
        excelData.add(card2);

        // 密码错误（格式正确但密码不匹配）
        CardVerifyExcelVO card3 = new CardVerifyExcelVO();
        card3.setCardNumber("333333333");
        card3.setCardPassword("WRONGP"); // 6位字符，但密码不正确
        excelData.add(card3);

        // 成功的卡
        CardVerifyExcelVO card4 = new CardVerifyExcelVO();
        card4.setCardNumber("444444444");
        card4.setCardPassword("DDD444");
        excelData.add(card4);

        MockMultipartFile file = createTestExcelFile(excelData);

        // Mock数据
        CardInfo cardInfo1 = new CardInfo();
        cardInfo1.setCardNumber("111111111");
        cardInfo1.setCardPassword("AAA111");
        cardInfo1.setBatchNumber("BATCH001");
        cardInfo1.setStatus(Constants.CardStatus.UNUSED);

        CardInfo cardInfo3 = new CardInfo();
        cardInfo3.setCardNumber("333333333");
        cardInfo3.setCardPassword("CCC333"); // 正确密码
        cardInfo3.setStatus(Constants.CardStatus.UNUSED);

        CardInfo cardInfo4 = new CardInfo();
        cardInfo4.setCardNumber("444444444");
        cardInfo4.setCardPassword("DDD444");
        cardInfo4.setBatchNumber("BATCH004");
        cardInfo4.setStatus(Constants.CardStatus.UNUSED);

        when(cardInfoMapper.selectByCardNumber("111111111")).thenReturn(cardInfo1);
        when(cardInfoMapper.selectByCardNumber("333333333")).thenReturn(cardInfo3);
        when(cardInfoMapper.selectByCardNumber("444444444")).thenReturn(cardInfo4);
        when(cardInfoMapper.updateById(any(CardInfo.class))).thenReturn(1);
        when(cardBatchMapper.incrementUsedCount(anyString())).thenReturn(1);

        // 执行
        BatchVerifyResultDTO result = verifyService.batchVerifyCard(file);

        // 验证
        assertEquals(4, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(2, result.getFailCount());
        assertEquals(2, result.getSuccessCards().size());
        assertEquals(2, result.getFailRecords().size());
    }
}
