// 字符串工具函数单元测试
import { describe, it, expect } from 'vitest'
import {
  parseStrEmpty,
  isEmpty,
  capitalize,
  truncate,
  camelToKebab,
  kebabToCamel,
  isExternal,
  isHttp,
  isValidJSON
} from '@/utils/string'

describe('parseStrEmpty', () => {
  it('null 和 undefined 应返回空字符串', () => {
    expect(parseStrEmpty(null)).toBe('')
    expect(parseStrEmpty(undefined)).toBe('')
    expect(parseStrEmpty('null')).toBe('')
    expect(parseStrEmpty('undefined')).toBe('')
  })

  it('正常字符串原样返回', () => {
    expect(parseStrEmpty('hello')).toBe('hello')
    expect(parseStrEmpty('')).toBe('')
  })
})

describe('isEmpty', () => {
  it('空值返回 true', () => {
    expect(isEmpty(null)).toBe(true)
    expect(isEmpty(undefined)).toBe(true)
    expect(isEmpty('')).toBe(true)
    expect(isEmpty('   ')).toBe(true)
  })

  it('非空返回 false', () => {
    expect(isEmpty('hello')).toBe(false)
    expect(isEmpty(' a ')).toBe(false)
  })
})

describe('capitalize', () => {
  it('首字母大写', () => {
    expect(capitalize('hello')).toBe('Hello')
    expect(capitalize('world')).toBe('World')
  })

  it('空字符串处理', () => {
    expect(capitalize('')).toBe('')
  })
})

describe('truncate', () => {
  it('超长截断加省略号', () => {
    expect(truncate('Hello world', 8)).toBe('Hello...')
  })

  it('长度内不截断', () => {
    expect(truncate('Hello', 20)).toBe('Hello')
  })
})

describe('camelToKebab', () => {
  it('驼峰转短横线', () => {
    expect(camelToKebab('myVariableName')).toBe('my-variable-name')
    expect(camelToKebab('backgroundColor')).toBe('background-color')
  })

  it('已经是短横线则不变', () => {
    expect(camelToKebab('my-name')).toBe('my-name')
  })
})

describe('kebabToCamel', () => {
  it('短横线转驼峰', () => {
    expect(kebabToCamel('my-variable-name')).toBe('myVariableName')
    expect(kebabToCamel('background-color')).toBe('backgroundColor')
  })
})

describe('isExternal', () => {
  it('外部链接返回 true', () => {
    expect(isExternal('https://example.com')).toBe(true)
    expect(isExternal('http://example.com')).toBe(true)
    expect(isExternal('mailto:test@test.com')).toBe(true)
    expect(isExternal('tel:13800138000')).toBe(true)
  })

  it('内部路径返回 false', () => {
    expect(isExternal('/internal/path')).toBe(false)
    expect(isExternal('internal/path')).toBe(false)
  })
})

describe('isHttp', () => {
  it('HTTP 链接返回 true', () => {
    expect(isHttp('http://example.com')).toBe(true)
    expect(isHttp('https://example.com')).toBe(true)
  })

  it('非 HTTP 返回 false', () => {
    expect(isHttp('/internal')).toBe(false)
    expect(isHttp('ftp://example.com')).toBe(false)
  })
})

describe('isValidJSON', () => {
  it('合法 JSON 返回 true', () => {
    expect(isValidJSON('{"name":"John"}')).toBe(true)
    expect(isValidJSON('[1,2,3]')).toBe(true)
  })

  it('非法 JSON 返回 false', () => {
    expect(isValidJSON('{name:John}')).toBe(false)
    expect(isValidJSON('')).toBe(false)
    expect(isValidJSON('not json')).toBe(false)
  })
})
