// to 函数（异步安全执行器）单元测试
import { describe, it, expect } from 'vitest'
import { to, toSync, toWithTimeout, toWithDefault, toIf, toAll } from '@/utils/to'

describe('to', () => {
  it('成功时返回 [null, data]', async () => {
    const [err, data] = await to(Promise.resolve('hello'))
    expect(err).toBeNull()
    expect(data).toBe('hello')
  })

  it('失败时返回 [error, null]', async () => {
    const [err, data] = await to(Promise.reject(new Error('失败了')))
    expect(err).toBeInstanceOf(Error)
    expect(err?.message).toBe('失败了')
    expect(data).toBeNull()
  })
})

describe('toSync', () => {
  it('成功时返回 [null, data]', () => {
    const [err, data] = toSync(() => 42)
    expect(err).toBeNull()
    expect(data).toBe(42)
  })

  it('抛出异常时返回 [error, null]', () => {
    const [err, data] = toSync(() => {
      throw new Error('同步错误')
    })
    expect(err).toBeInstanceOf(Error)
    expect(err?.message).toBe('同步错误')
    expect(data).toBeNull()
  })
})

describe('toWithTimeout', () => {
  it('正常完成', async () => {
    const [err, data] = await toWithTimeout(Promise.resolve('ok'), 1000)
    expect(err).toBeNull()
    expect(data).toBe('ok')
  })

  it('超时报错', async () => {
    const slow = new Promise((resolve) => setTimeout(() => resolve('late'), 200))
    const [err, data] = await toWithTimeout(slow, 10, '超时了')
    expect(err).toBeInstanceOf(Error)
    expect(err?.message).toBe('超时了')
    expect(data).toBeNull()
  })
})

describe('toWithDefault', () => {
  it('成功时返回原值', async () => {
    const [err, data] = await toWithDefault(Promise.resolve('hello'), '默认')
    expect(err).toBeNull()
    expect(data).toBe('hello')
  })

  it('失败时返回默认值', async () => {
    const [err, data] = await toWithDefault(Promise.reject(new Error('fail')), '默认')
    expect(err).toBeInstanceOf(Error)
    expect(data).toBe('默认')
  })
})

describe('toIf', () => {
  it('条件为 true 时执行', async () => {
    const [err, data] = await toIf(true, Promise.resolve('done'))
    expect(err).toBeNull()
    expect(data).toBe('done')
  })

  it('条件为 false 时跳过', async () => {
    const [err, data] = await toIf(false, Promise.resolve('done'))
    expect(err).toBeNull()
    expect(data).toBeNull()
  })
})

describe('toAll', () => {
  it('并行执行多个 Promise', async () => {
    const results = await toAll([
      Promise.resolve('a'),
      Promise.resolve('b'),
      Promise.resolve('c')
    ])
    expect(results).toHaveLength(3)
    expect(results[0]).toEqual([null, 'a'])
    expect(results[1]).toEqual([null, 'b'])
    expect(results[2]).toEqual([null, 'c'])
  })

  it('某个失败不影响其他', async () => {
    const results = await toAll([
      Promise.resolve('a'),
      Promise.reject(new Error('fail')),
      Promise.resolve('c')
    ])
    expect(results).toHaveLength(3)
    expect(results[0][1]).toBe('a')
    expect(results[1][0]).toBeInstanceOf(Error)
    expect(results[2][1]).toBe('c')
  })
})
