/**
 * 安全异步执行工具函数集 (utils/to.ts)
 *
 * 提供一系列工具函数，将 Promise 和可能抛出异常的代码转换为 [error, data] 格式，
 * 避免 try-catch 的使用，让代码更加简洁和可读。
 *
 * 包含函数列表：
 *
 * - to(): 基础 Promise 异常处理，将任何 Promise 转换为 [error, data] 格式
 *
 * - toValidate(): 表单验证专用，处理 Element Plus 等 UI 库的表单验证
 *
 * - toAll(): 批量处理 Promise 数组，并行执行多个异步操作
 * - toWithTimeout(): 带超时控制，为 Promise 添加超时机制
 *
 * - toSync(): 同步版本，处理可能抛出异常的同步代码
 * - toResult(): 类型安全透传，用于已返回 Result 格式的函数
 * - toWithRetry(): 自动重试机制，适用于网络不稳定场景
 * - toWithDefault(): 带默认值，失败时使用备用数据
 *
 * - toWithLog(): 带调试日志，开发阶段监控异步操作
 * - toSequence(): 串行执行，按顺序处理有依赖关系的操作
 * - toIf(): 条件执行，根据条件决定是否执行异步操作
 *
 *  使用示例：
 * ```typescript
 * // 基础用法
 * const [err, user] = await to(fetchUser(id));
 * if (err) return handleError(err);
 *
 * // 表单验证
 * const [err, isValid] = await toValidate(formRef);
 * if (err || !isValid) return;
 *
 * // 批量请求
 * const results = await toAll([api1(), api2(), api3()]);
 *
 * // 带超时
 * const [err, data] = await toWithTimeout(slowApi(), 5000);
 * ```
 *
 * @author 抓蛙师
 * @since 2025-05-29
 */

/**
 * 基础 to 函数 - 处理 Promise 异常
 *
 * 将任何 Promise 转换为 [error, data] 格式，避免使用 try-catch
 * 这是最常用的函数，99% 的场景都会用到
 *
 * @template T Promise 返回的数据类型
 * @param promise 要执行的 Promise 对象
 * @returns Promise<[Error | null, T | null]> 包含错误和数据的元组
 *
 * @example
 * // 基础用法
 * const [err, user] = await to(getUserById('123'));
 * if (err) {
 *   console.error('获取用户失败:', err.message);
 *   return;
 * }
 * console.log('用户信息:', user);
 *
 * @example
 * // 处理 API 请求
 * const [err, response] = await to(fetch('/api/data'));
 * if (err) {
 *   showErrorMessage('网络请求失败');
 *   return;
 * }
 * const [parseErr, data] = await to(response.json());
 * if (parseErr) {
 *   showErrorMessage('数据解析失败');
 *   return;
 * }
 * console.log('请求结果:', data);
 */
export const to = async <T>(promise: Promise<T>): Promise<[Error | null, T | null]> => {
  try {
    const data = await promise
    return [null, data]
  } catch (error) {
    return [error instanceof Error ? error : new Error(String(error)), null]
  }
}

/**
 * 表单验证专用的 to 函数
 *
 * 专门处理 Element Plus 等 UI 库的表单验证，将回调式 API 转换为 Promise
 * 在表单密集的后台管理系统中使用频率很高
 *
 * @param formRef 表单引用对象
 * @returns Promise<[Error | null, boolean]> 验证结果，成功时返回 true
 *
 * @example
 * // Vue 组件中的表单验证
 * const handleSubmit = async () => {
 *   const [err, isValid] = await toValidate(formRef.value);
 *   if (err || !isValid) {
 *     ElMessage.error(err?.message || '表单验证失败');
 *     return;
 *   }
 *
 *   // 验证通过，提交表单
 *   const [submitErr] = await to(submitForm(formData));
 *   if (submitErr) {
 *     ElMessage.error('提交失败: ' + submitErr.message);
 *     return;
 *   }
 *
 *   ElMessage.success('提交成功');
 * };
 *
 * @example
 * // 多个表单同时验证
 * const validateAllForms = async () => {
 *   const results = await toAll([
 *     toValidate(userFormRef.value),
 *     toValidate(addressFormRef.value),
 *     toValidate(paymentFormRef.value)
 *   ]);
 *
 *   const hasErrors = results.some(([err, isValid]) => err || !isValid);
 *   if (hasErrors) {
 *     ElMessage.error('请检查表单输入');
 *     return false;
 *   }
 *
 *   return true;
 * };
 */
export const toValidate = async (formRef: Ref<ElFormInstance>): Promise<[Error | null, boolean]> => {
  return new Promise((resolve) => {
    if (!formRef) {
      resolve([new Error('表单引用不存在'), false])
      return
    }

    formRef.value.validate((valid: boolean, fields: any) => {
      if (!valid) {
        // 提取所有验证错误信息
        const errorMessages = Object.values(fields || {})
          .flat()
          .map((item: any) => item.message)
          .filter(Boolean)
          .join(', ')

        const errorMessage = errorMessages || '表单验证失败'
        resolve([new Error(errorMessage), false])
      } else {
        resolve([null, true])
      }
    })
  })
}

/**
 * 批量处理 Promise 数组
 *
 * 并行执行多个 Promise，每个都使用 to 函数包装，不会因为某个失败而中断其他请求
 * 支持混合传入原始 Promise 和已用 to() 包装的 Promise
 * 适用于需要同时请求多个接口的场景
 *
 * @template T Promise 返回的数据类型
 * @param promises Promise 数组，支持原始 Promise 和 to() 包装的 Promise 混合使用
 * @returns Promise<Array<[Error | null, T | null]>> 每个 Promise 结果的数组
 *
 * @example
 * // 并行请求多个接口
 * const promises = [
 *   getUserInfo(userId),              // 原始 Promise
 *   to(getUserOrders(userId)),        // 手动用 to() 包装的 Promise
 *   getUserPreferences(userId)        // 原始 Promise
 * ];
 *
 * const results = await toAll(promises);
 * const [userErr, userInfo] = results[0];
 * const [ordersErr, orders] = results[1];
 * const [prefsErr, preferences] = results[2];
 *
 * // 统一处理，无论原始格式如何
 * if (userErr) console.error('获取用户信息失败:', userErr.message);
 * if (ordersErr) console.error('获取订单失败:', ordersErr.message);
 * if (prefsErr) console.error('获取偏好设置失败:', prefsErr.message);
 *
 * @example
 * // 批量上传文件
 * const uploadPromises = files.map(file => uploadFile(file));
 * const results = await toAll(uploadPromises);
 *
 * const successCount = results.filter(([err]) => !err).length;
 * const failCount = results.filter(([err]) => err).length;
 *
 * console.log(`上传完成: 成功 ${successCount} 个，失败 ${failCount} 个`);
 */
export const toAll = async <T>(promises: (Promise<T> | Promise<[Error | null, T | null]>)[]): Promise<Array<[Error | null, T | null]>> => {
  return Promise.all(
    promises.map(async (promise) => {
      try {
        const result = await promise

        // 检查是否已经是 to() 格式 [Error | null, T | null]
        if (Array.isArray(result) && result.length === 2) {
          return result as [Error | null, T | null]
        }

        // 原始数据，包装为 to() 格式
        return [null, result as T] as [Error | null, T | null]
      } catch (error) {
        return [error instanceof Error ? error : new Error(String(error)), null] as [Error | null, T | null]
      }
    })
  )
}

/**
 * 带超时控制的 to 函数
 *
 * 为 Promise 添加超时控制，避免长时间等待
 * 在网络请求、外部服务调用等场景中经常使用
 *
 * @template T Promise 返回的数据类型
 * @param promise 要执行的 Promise
 * @param timeoutMs 超时时间（毫秒）
 * @param timeoutMessage 超时错误信息
 * @returns Promise<[Error | null, T | null]> 包含错误和数据的元组
 *
 * @example
 * // 为 API 请求设置 5 秒超时
 * const [err, data] = await toWithTimeout(
 *   fetch('/api/heavy-computation'),
 *   5000,
 *   '计算超时，请稍后重试'
 * );
 * if (err) {
 *   if (err.message.includes('超时')) {
 *     showTimeoutMessage();
 *   } else {
 *     showErrorMessage('请求失败');
 *   }
 *   return;
 * }
 * console.log('计算结果:', data);
 *
 * @example
 * // 用户输入验证（3秒超时）
 * const [err, isValid] = await toWithTimeout(
 *   validateUserInput(inputValue),
 *   3000,
 *   '验证超时，请检查网络连接'
 * );
 * if (err) {
 *   setValidationError(err.message);
 *   return;
 * }
 * if (isValid) {
 *   submitForm();
 * }
 */
export const toWithTimeout = async <T>(
  promise: Promise<T>,
  timeoutMs: number,
  timeoutMessage = `操作超时（${timeoutMs}ms）`
): Promise<[Error | null, T | null]> => {
  const timeoutPromise = new Promise<never>((_, reject) => {
    setTimeout(() => reject(new Error(timeoutMessage)), timeoutMs)
  })

  return to(Promise.race([promise, timeoutPromise]))
}

/**
 * 同步版本的 to 函数
 *
 * 用于包装可能抛出异常的同步函数，统一错误处理格式
 * 主要用于 JSON 解析、数据处理等可能出错的同步操作
 *
 * @template T 函数返回的数据类型
 * @param fn 要执行的同步函数
 * @returns [Error | null, T | null] 包含错误和数据的元组
 *
 * @example
 * // 处理 JSON 解析
 * const [err, data] = toSync(() => JSON.parse(jsonString));
 * if (err) {
 *   console.error('JSON 解析失败:', err.message);
 *   return;
 * }
 * console.log('解析结果:', data);
 *
 * @example
 * // 处理数组操作
 * const [err, result] = toSync(() => {
 *   if (array.length === 0) throw new Error('数组为空');
 *   return array[0].someProperty.toUpperCase();
 * });
 * if (err) {
 *   console.error('操作失败:', err.message);
 *   return;
 * }
 * console.log('处理结果:', result);
 */
export const toSync = <T>(fn: () => T): [Error | null, T | null] => {
  try {
    const data = fn()
    return [null, data]
  } catch (error) {
    return [error instanceof Error ? error : new Error(String(error)), null]
  }
}

/**
 * 类型安全的 to 函数
 *
 * 当函数已经返回 [Error | null, T | null] 格式时，直接透传结果
 * 主要用于保持 API 一致性和类型安全，在已经使用 Result 格式的项目中有用
 *
 * @template T 数据类型
 * @param resultPromise 返回 Result 格式的 Promise
 * @returns Promise<[Error | null, T | null]> 直接返回输入的 Promise
 *
 * @example
 * // 用于保持 API 一致性
 * const fetchUserSafely = async (userId: string) => {
 *   // 这个函数已经返回 Result 格式
 *   return toResult(apiClient.getUser(userId));
 * };
 *
 * // 使用时保持一致的调用方式
 * const [err, user] = await fetchUserSafely('123');
 *
 * @example
 * // 在函数组合中使用
 * const operations = [
 *   () => to(regularPromiseFunction()),        // 需要包装
 *   () => toResult(alreadySafeFunction()),     // 已经安全，直接透传
 *   () => to(anotherRegularFunction())         // 需要包装
 * ];
 *
 * const [err, results] = await toSequence(operations);
 */
export const toResult = async <T>(resultPromise: Promise<[Error | null, T | null]>): Promise<[Error | null, T | null]> => {
  return resultPromise
}

/**
 * 带重试机制的 to 函数（增强版）
 *
 * 支持两种传入方式：
 * 1. 直接传入 Promise 工厂函数
 * 2. 传入已经用 to() 包装的函数
 *
 * @template T Promise 返回的数据类型
 * @param promiseFactory 返回 Promise 或 [Error | null, T | null] 的工厂函数
 * @param maxRetries 最大重试次数（默认 3 次）
 * @param retryDelay 重试间隔时间（毫秒，默认 1000ms）
 * @returns Promise<[Error | null, T | null]>
 *
 * @example
 * // 重试网络请求
 * const [err, data] = await toWithRetry(
 *   () => fetch('/api/unstable-endpoint'),
 *   3,    // 重试 3 次
 *   2000  // 间隔 2 秒
 * );
 * if (err) {
 *   console.error('重试 3 次后仍然失败:', err.message);
 *   showErrorMessage('网络连接不稳定，请稍后重试');
 *   return;
 * }
 * console.log('请求成功:', data);
 *
 * @example
 * // 兼容已用 to() 包装的函数
 * const [err, data] = await toWithRetry(
 *   () => to(queryOrderStatus(orderNo)),
 *   2,
 *   2000
 * );
 * if (err) {
 *   console.error('查询支付状态失败:', err.message);
 *   return;
 * }
 * console.log('支付状态:', data);
 */
export const toWithRetry = async <T>(
  promiseFactory: (() => Promise<T>) | (() => Promise<[Error | null, T | null]>),
  maxRetries: number = 2,
  retryDelay: number = 1000
): Promise<[Error | null, T | null]> => {
  let lastError: Error

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      const result = await promiseFactory()

      // 检查返回值是否是 to() 包装后的格式 [Error | null, T | null]
      if (Array.isArray(result) && result.length === 2) {
        const [error, data] = result as [Error | null, T | null]
        if (!error) {
          if (attempt > 0) {
            console.log(`操作在第 ${attempt + 1} 次尝试时成功`)
          }
          return [null, data]
        }
        lastError = error
      } else {
        // 直接返回的数据，表示成功
        if (attempt > 0) {
          console.log(`操作在第 ${attempt + 1} 次尝试时成功`)
        }
        return [null, result as T]
      }
    } catch (error) {
      lastError = error as Error
    }

    // 如果不是最后一次尝试，等待后重试
    if (attempt < maxRetries) {
      console.warn(`第 ${attempt + 1} 次尝试失败，${retryDelay}ms 后重试:`, lastError.message)
      await new Promise((resolve) => setTimeout(resolve, retryDelay))
    }
  }

  return [lastError, null]
}

/**
 * 带默认值的 to 函数
 *
 * 当 Promise 失败或返回 null/undefined 时，使用提供的默认值
 * 适用于配置加载、可选数据获取等场景
 *
 * @template T Promise 返回的数据类型
 * @param promise 要执行的 Promise
 * @param defaultValue 出错或无数据时的默认值
 * @returns Promise<[Error | null, T]> 包含错误和数据（保证有值）的元组
 *
 * @example
 * // 获取用户配置，失败时使用默认配置
 * const [err, config] = await toWithDefault(
 *   getUserConfig(userId),
 *   { theme: 'light', language: 'zh-CN', pageSize: 10 }
 * );
 * // config 保证不为 null，要么是获取到的配置，要么是默认配置
 * applyUserConfig(config);
 * if (err) {
 *   console.warn('使用默认配置，原因:', err.message);
 * }
 *
 * @example
 * // 获取缓存数据，失败时使用空数组
 * const [err, cachedItems] = await toWithDefault(
 *   getCachedData('shopping-cart'),
 *   []
 * );
 * // cachedItems 保证是数组，可以安全调用数组方法
 * console.log(`购物车中有 ${cachedItems.length} 件商品`);
 * cachedItems.forEach(item => renderCartItem(item));
 */
export const toWithDefault = async <T>(promise: Promise<T>, defaultValue: T): Promise<[Error | null, T]> => {
  const [error, data] = await to(promise)
  return [error, data ?? defaultValue]
}

/**
 * 带日志的 to 函数 ⭐
 *
 * 自动记录操作的开始、成功和失败状态，便于调试和监控
 * 主要在开发调试阶段使用，生产环境使用较少
 *
 * @template T Promise 返回的数据类型
 * @param promise 要执行的 Promise
 * @param label 日志标签，用于标识操作
 * @param enableLog 是否启用日志（默认 true）
 * @returns Promise<[Error | null, T | null]> 包含错误和数据的元组
 *
 * @example
 * // 监控 API 请求
 * const [err, userData] = await toWithLog(
 *   fetchUserData(userId),
 *   '获取用户数据',
 *   true
 * );
 * // 控制台输出:
 * // [获取用户数据] 开始执行
 * // [获取用户数据] 执行成功  (或 执行失败: 错误信息)
 *
 * @example
 * // 在生产环境中关闭日志
 * const isProduction = process.env.NODE_ENV === 'production';
 * const [err, result] = await toWithLog(
 *   performCriticalOperation(),
 *   '关键操作',
 *   !isProduction  // 生产环境不记录日志
 * );
 */
export const toWithLog = async <T>(promise: Promise<T>, label: string = 'Promise', enableLog: boolean = true): Promise<[Error | null, T | null]> => {
  if (enableLog) {
    console.log(`[${label}] 开始执行`)
  }

  const startTime = Date.now()
  const [error, data] = await to(promise)
  const duration = Date.now() - startTime

  if (enableLog) {
    if (error) {
      console.error(`[${label}] 执行失败 (${duration}ms):`, error.message)
    } else {
      console.log(`[${label}] 执行成功 (${duration}ms)`)
    }
  }

  return [error, data]
}

/**
 * 串行执行多个异步操作
 *
 * 按顺序执行多个操作，如果任何一个失败则停止执行并返回错误
 * 支持混合传入原始 Promise 工厂函数和已用 to() 包装的函数
 * 适用于有依赖关系的操作序列，使用频率相对较低
 *
 * @template T 操作返回的数据类型
 * @param operations 操作函数数组，每个函数返回 Promise 或 [Error | null, T | null]
 * @returns Promise<[Error | null, T[]]> 所有操作的结果数组
 *
 * @example
 * // 按顺序执行初始化步骤（混合格式）
 * const initSteps = [
 *   () => connectToDatabase(),           // 原始 Promise 函数
 *   () => to(loadConfiguration()),       // 用 to() 包装的函数
 *   () => startServices(),               // 原始 Promise 函数
 *   () => to(scheduleJobs())            // 用 to() 包装的函数
 * ];
 *
 * const [err, results] = await toSequence(initSteps);
 * if (err) {
 *   console.error('初始化失败:', err.message);
 *   await rollbackInitialization();
 *   return;
 * }
 *
 * console.log('所有初始化步骤完成:', results);
 *
 * @example
 * // 数据处理流水线
 * const processingSteps = [
 *   () => validateInputData(rawData),
 *   () => to(transformData(rawData)),
 *   () => enrichWithMetadata(transformedData),
 *   () => to(saveToDatabase(enrichedData))
 * ];
 *
 * const [err, stepResults] = await toSequence(processingSteps);
 * if (err) {
 *   console.error('数据处理失败:', err.message);
 *   notifyAdminOfFailure(err);
 *   return;
 * }
 *
 * console.log('数据处理完成，共执行了', stepResults.length, '个步骤');
 */
export const toSequence = async <T>(
  operations: Array<(() => Promise<T>) | (() => Promise<[Error | null, T | null]>)>
): Promise<[Error | null, T[]]> => {
  const results: T[] = []

  for (let i = 0; i < operations.length; i++) {
    const operation = operations[i]

    try {
      const result = await operation()

      // 检查是否已经是 to() 格式 [Error | null, T | null]
      if (Array.isArray(result) && result.length === 2) {
        const [error, data] = result as [Error | null, T | null]
        if (error) {
          console.error(`第 ${i + 1} 个操作失败:`, error.message)
          return [error, []]
        }
        if (data !== null && data !== undefined) {
          results.push(data)
        }
      } else {
        // 原始数据，直接使用
        if (result !== null && result !== undefined) {
          results.push(result as T)
        }
      }
    } catch (error) {
      const err = error instanceof Error ? error : new Error(String(error))
      console.error(`第 ${i + 1} 个操作失败:`, err.message)
      return [err, []]
    }
  }

  return [null, results]
}

/**
 * 条件执行的 to 函数
 *
 * 只有满足条件时才执行 Promise，否则直接返回 [null, null]
 * 支持传入原始 Promise 和已用 to() 包装的 Promise
 * 用于特定场景的条件性操作，使用频率最低
 *
 * @template T Promise 返回的数据类型
 * @param condition 执行条件
 * @param promise 要执行的 Promise，支持原始 Promise 和 to() 包装的 Promise
 * @returns Promise<[Error | null, T | null]> 包含错误和数据的元组
 *
 * @example
 * // 根据权限决定是否加载敏感数据（原始 Promise）
 * const [err, sensitiveData] = await toIf(
 *   userRole === 'admin',
 *   fetchSensitiveUserData(userId)
 * );
 * if (err) {
 *   console.error('加载敏感数据失败:', err.message);
 * } else if (sensitiveData) {
 *   renderSensitiveDataPanel(sensitiveData);
 * } else {
 *   console.log('权限不足，跳过敏感数据加载');
 * }
 *
 * @example
 * // 根据网络状态决定是否同步数据（已用 to() 包装）
 * const [err, syncResult] = await toIf(
 *   navigator.onLine,
 *   to(syncDataToServer(localData))
 * );
 * if (err) {
 *   console.error('数据同步失败:', err.message);
 *   scheduleRetrySync();
 * } else if (syncResult) {
 *   console.log('数据同步成功');
 *   markDataAsSynced();
 * } else {
 *   console.log('离线状态，跳过数据同步');
 * }
 */
export const toIf = async <T>(condition: boolean, promise: Promise<T> | Promise<[Error | null, T | null]>): Promise<[Error | null, T | null]> => {
  if (!condition) {
    return [null, null]
  }

  try {
    const result = await promise

    // 检查是否已经是 to() 格式 [Error | null, T | null]
    if (Array.isArray(result) && result.length === 2) {
      return result as [Error | null, T | null]
    }

    // 原始数据，包装为 to() 格式
    return [null, result as T]
  } catch (error) {
    return [error instanceof Error ? error : new Error(String(error)), null]
  }
}
