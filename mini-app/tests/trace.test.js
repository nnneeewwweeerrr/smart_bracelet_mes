import { getForwardTrace, getReverseTrace } from '@/api/production'

describe('正向追溯测试用例', () => {
  const validBatchNo = 'BATCH202401001'
  const emptyBatchNo = ''
  const notExistBatchNo = 'NOTEXIST999'

  test('TC-FWD-001: 输入有效批次号，应返回关联工单列表', async () => {
    const res = await getForwardTrace(validBatchNo)
    expect(res).toBeDefined()
    expect(Array.isArray(res)).toBe(true)
    expect(res.length).toBeGreaterThan(0)

    const firstItem = res[0]
    expect(firstItem).toHaveProperty('processName')
    expect(firstItem).toHaveProperty('executeTime')
    expect(firstItem).toHaveProperty('result')
    expect(firstItem).toHaveProperty('detail')

    const hasRelatedWorkNo = res.some(item => item.relatedWorkNo && item.relatedWorkNo.length > 0)
    expect(hasRelatedWorkNo).toBe(true)
  })

  test('TC-FWD-002: 输入有效批次号，返回数据应包含完整时间线字段', async () => {
    const res = await getForwardTrace(validBatchNo)
    expect(res.length).toBeGreaterThan(0)

    res.forEach(item => {
      expect(item.processName).toBeTruthy()
      expect(item.executeTime).toMatch(/^\d{4}-\d{2}-\d{2}/)
      expect(['pass', 'fail', 'pending', 'running']).toContain(item.result)
      expect(item.detail).toBeTruthy()
    })
  })

  test('TC-FWD-003: 输入有效批次号，追溯链路应按时间正序排列', async () => {
    const res = await getForwardTrace(validBatchNo)
    expect(res.length).toBeGreaterThan(1)

    for (let i = 1; i < res.length; i++) {
      const prev = new Date(res[i - 1].executeTime).getTime()
      const curr = new Date(res[i].executeTime).getTime()
      expect(curr).toBeGreaterThanOrEqual(prev)
    }
  })

  test('TC-FWD-004: 输入有效批次号，应能正确关联到工单号', async () => {
    const res = await getForwardTrace(validBatchNo)
    const workOrders = res
      .filter(item => item.relatedWorkNo)
      .map(item => item.relatedWorkNo)

    const uniqueOrders = [...new Set(workOrders)]
    expect(uniqueOrders.length).toBeGreaterThan(0)
    uniqueOrders.forEach(wo => {
      expect(wo).toMatch(/^WO/)
    })
  })

  test('TC-FWD-005: 输入空批次号，应返回空数组', async () => {
    const res = await getForwardTrace(emptyBatchNo)
    expect(res).toBeDefined()
    expect(Array.isArray(res)).toBe(true)
    expect(res.length).toBe(0)
  })

  test('TC-FWD-006: 输入不存在的批次号，应返回空数组', async () => {
    const res = await getForwardTrace(notExistBatchNo)
    expect(res).toBeDefined()
    expect(Array.isArray(res)).toBe(true)
    expect(res.length).toBe(0)
  })

  test('TC-FWD-007: 正向追溯与反向追溯结果应可交叉验证', async () => {
    const fwdRes = await getForwardTrace(validBatchNo)
    const relatedWorkNo = fwdRes.find(item => item.relatedWorkNo)?.relatedWorkNo

    if (relatedWorkNo) {
      const revRes = await getReverseTrace(relatedWorkNo)
      expect(revRes).toBeDefined()
      expect(revRes.length).toBeGreaterThan(0)

      const revProcessNames = revRes.map(i => i.processName)
      const fwdProcessNames = fwdRes.map(i => i.processName)
      const overlap = revProcessNames.filter(n => fwdProcessNames.includes(n))
      expect(overlap.length).toBeGreaterThan(0)
    }
  })
})

describe('反向追溯测试用例', () => {
  const validWorkNo = 'WO202401001'

  test('TC-REV-001: 输入有效工单号，应返回完整工序履历', async () => {
    const res = await getReverseTrace(validWorkNo)
    expect(res).toBeDefined()
    expect(Array.isArray(res)).toBe(true)
    expect(res.length).toBeGreaterThan(0)

    res.forEach(item => {
      expect(item).toHaveProperty('processName')
      expect(item).toHaveProperty('executeTime')
      expect(item).toHaveProperty('result')
    })
  })

  test('TC-REV-002: 反向追溯结果应包含合格/不合格判定', async () => {
    const res = await getReverseTrace(validWorkNo)
    const results = res.map(i => i.result)
    const validResults = ['pass', 'fail', 'pending', 'running']
    results.forEach(r => {
      expect(validResults).toContain(r)
    })
  })
})