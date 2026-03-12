import { describe, it, expect, vi, beforeEach } from 'vitest'
import request from '@/utils/request'
import { 
  generateCards, 
  getCardList, 
  recycleBatch, 
  recycleSingle,
  getBatchNumbers 
} from '../card'

// Mock request
vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

describe('Card API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('generateCards', () => {
    it('应该使用正确的参数调用 API', async () => {
      const mockData = { count: 100 }
      const mockResponse = { data: '20240101120000' }
      request.mockResolvedValue(mockResponse)

      const result = await generateCards(mockData)

      expect(request).toHaveBeenCalledWith({
        url: '/card/generate',
        method: 'post',
        data: mockData,
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getCardList', () => {
    it('应该使用正确的查询参数调用 API', async () => {
      const mockParams = { 
        batchNumber: '20240101120000', 
        pageNum: 1, 
        pageSize: 10 
      }
      const mockResponse = { data: { records: [], total: 0 } }
      request.mockResolvedValue(mockResponse)

      const result = await getCardList(mockParams)

      expect(request).toHaveBeenCalledWith({
        url: '/card/list',
        method: 'get',
        params: mockParams,
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('recycleBatch', () => {
    it('应该使用正确的批次号调用 API', async () => {
      const batchNumber = '20240101120000'
      const mockResponse = { data: 10 }
      request.mockResolvedValue(mockResponse)

      const result = await recycleBatch(batchNumber)

      expect(request).toHaveBeenCalledWith({
        url: '/card/recycle/batch',
        method: 'put',
        params: { batchNumber },
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('recycleSingle', () => {
    it('应该使用正确的卡号调用 API', async () => {
      const cardNumber = '123456789'
      const mockResponse = { code: 200 }
      request.mockResolvedValue(mockResponse)

      const result = await recycleSingle(cardNumber)

      expect(request).toHaveBeenCalledWith({
        url: '/card/recycle/single',
        method: 'put',
        params: { cardNumber },
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getBatchNumbers', () => {
    it('应该正确调用 API 获取批次号列表', async () => {
      const mockResponse = { 
        data: ['20240101120000', '20240102120000'] 
      }
      request.mockResolvedValue(mockResponse)

      const result = await getBatchNumbers()

      expect(request).toHaveBeenCalledWith({
        url: '/card/batch/numbers',
        method: 'get',
      })
      expect(result).toEqual(mockResponse)
    })
  })
})
