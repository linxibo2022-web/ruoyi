/**
 * 树结构工具函数库
 *
 * 提供全面的树形数据结构处理工具，用于前端各种树形数据的操作和转换。
 * 包含以下功能：
 * - 构建树: 将平铺列表转换为嵌套树结构 (buildTree)
 * - 查找节点: 在树中查找符合条件的节点 (findTreeNode)
 * - 查找路径: 获取从根节点到目标节点的路径 (findTreeNodePath)
 * - 过滤树: 根据条件过滤树节点 (filterTree)
 * - 扁平化: 将树结构转换为一维数组 (flattenTree)
 * - 遍历树: 遍历并对每个节点执行操作 (traverseTree)
 * - 插入节点: 在树中指定位置插入节点 (insertNode)
 * - 删除节点: 移除符合条件的节点 (removeNode)
 * - 更新节点: 更新符合条件的节点 (updateNode)
 * - 获取叶子: 获取所有叶子节点 (getLeafNodes)
 * - 计算深度: 获取树的最大深度 (getTreeDepth)
 *
 * 所有函数支持自定义字段名配置，适用于各种业务场景的树形数据结构。
 */

/**
 * 树节点通用接口
 */
export interface TreeNode {
  [key: string]: any

  children?: TreeNode[]
}

/**
 * 树节点配置选项
 */
export interface TreeOptions {
  /** ID字段名称 */
  id?: string
  /** 父ID字段名称 */
  parentId?: string
  /** 子节点字段名称 */
  children?: string
  /** 深拷贝数据，默认为true */
  deepCopy?: boolean
}

// 默认树配置
const DEFAULT_TREE_OPTIONS: TreeOptions = {
  id: 'id',
  parentId: 'parentId',
  children: 'children',
  deepCopy: true
}

/**
 * 构造树型结构数据
 *
 * @param {Array} data 源数据数组
 * @param {TreeOptions} options 配置选项
 * @returns {Array} 树结构数据
 *
 * @example
 * // 基本使用
 * const list = [
 *   { id: 1, name: '部门1', parentId: 0 },
 *   { id: 2, name: '部门2', parentId: 1 },
 *   { id: 3, name: '部门3', parentId: 1 },
 * ];
 * const tree = buildTree(list);
 * // 结果:
 * // [
 * //   {
 * //     id: 1, name: '部门1', parentId: 0,
 * //     children: [
 * //       { id: 2, name: '部门2', parentId: 1, children: [] },
 * //       { id: 3, name: '部门3', parentId: 1, children: [] }
 * //     ]
 * //   }
 * // ]
 *
 * // 自定义字段名
 * const customList = [
 *   { itemId: 1, name: '部门1', pid: 0 },
 *   { itemId: 2, name: '部门2', pid: 1 },
 * ];
 * const customTree = buildTree(customList, { id: 'itemId', parentId: 'pid' });
 */
export const buildTree = <T = TreeNode>(data: any[], options: TreeOptions = {}): T[] => {
  const mergedOptions = { ...DEFAULT_TREE_OPTIONS, ...options }
  const { id, parentId, children, deepCopy } = mergedOptions

  // 如果需要深拷贝数据
  const sourceData = deepCopy ? JSON.parse(JSON.stringify(data)) : data

  // 使用Map优化查找效率
  const childrenMap = new Map<any, any[]>()
  const nodeMap = new Map<any, any>()

  // 第一次遍历，初始化每个节点的map
  for (const item of sourceData) {
    // 确保每个节点都有children属性
    if (!item[children!]) {
      item[children!] = []
    }

    const itemId = item[id!]
    nodeMap.set(itemId, item)

    // 初始化childrenMap
    if (!childrenMap.has(item[parentId!])) {
      childrenMap.set(item[parentId!], [])
    }
    childrenMap.get(item[parentId!]).push(item)
  }

  // 第二次遍历，构建树结构
  const result: T[] = []
  for (const item of sourceData) {
    const pId = item[parentId!]
    // 如果父节点存在于nodeMap中，将当前节点添加到父节点的children中
    if (nodeMap.has(pId)) {
      const parent = nodeMap.get(pId)
      if (!parent[children!].includes(item)) {
        parent[children!].push(item)
      }
    } else {
      // 如果找不到父节点，则作为根节点
      result.push(item)
    }
  }

  return result
}

/**
 * 查找树节点
 *
 * @param {Array} tree 树结构数据
 * @param {Function} predicate 判断函数，返回true表示找到目标节点
 * @param {TreeOptions} options 配置选项
 * @returns {any|null} 找到的节点或null
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       { id: 3, name: '部门3' }
 *     ]
 *   }
 * ];
 *
 * // 按ID查找
 * const node = findTreeNode(tree, node => node.id === 3);
 * console.log(node); // { id: 3, name: '部门3' }
 *
 * // 按名称查找
 * const node2 = findTreeNode(tree, node => node.name === '部门2');
 * console.log(node2); // { id: 2, name: '部门2' }
 */
export const findTreeNode = <T = TreeNode>(tree: T[], predicate: (node: T) => boolean, options: TreeOptions = {}): T | null => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }

  // 深度优先搜索
  for (const node of tree) {
    // 如果当前节点符合条件，返回该节点
    if (predicate(node)) {
      return node
    }

    // 如果当前节点有子节点，递归搜索子节点
    if (node[children!] && node[children!].length > 0) {
      const found = findTreeNode(node[children!], predicate, options)
      if (found) {
        return found
      }
    }
  }

  return null
}

/**
 * 查找树节点路径
 *
 * @param {Array} tree 树结构数据
 * @param {Function} predicate 判断函数，返回true表示找到目标节点
 * @param {TreeOptions} options 配置选项
 * @returns {Array} 节点路径数组，从根节点到目标节点
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       {
 *         id: 3, name: '部门3',
 *         children: [
 *           { id: 4, name: '部门4' }
 *         ]
 *       }
 *     ]
 *   }
 * ];
 *
 * // 查找ID为4的节点路径
 * const path = findTreeNodePath(tree, node => node.id === 4);
 * console.log(path);
 * // [
 * //   { id: 1, name: '部门1', ... },
 * //   { id: 3, name: '部门3', ... },
 * //   { id: 4, name: '部门4' }
 * // ]
 */
export const findTreeNodePath = <T = TreeNode>(tree: T[], predicate: (node: T) => boolean, options: TreeOptions = {}): T[] => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }

  // 辅助函数：深度优先搜索路径
  function dfs(nodes: T[], path: T[] = []): T[] | null {
    if (!nodes || nodes.length === 0) return null

    for (const node of nodes) {
      // 将当前节点加入路径
      path.push(node)

      // 如果当前节点符合条件，返回路径
      if (predicate(node)) {
        return path
      }

      // 递归搜索子节点
      if (node[children!] && node[children!].length > 0) {
        const found = dfs(node[children!], [...path])
        if (found) {
          return found
        }
      }

      // 回溯：从路径中移除当前节点
      path.pop()
    }

    return null
  }

  const result = dfs(tree)
  return result || []
}

/**
 * 过滤树结构
 *
 * @param {Array} tree 树结构数据
 * @param {Function} predicate 过滤函数，返回true表示保留该节点
 * @param {TreeOptions} options 配置选项
 * @returns {Array} 过滤后的树结构
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '研发部' },
 *       { id: 3, name: '销售部' }
 *     ]
 *   },
 *   {
 *     id: 4, name: '财务部',
 *     children: [
 *       { id: 5, name: '会计部' },
 *       { id: 6, name: '出纳部' }
 *     ]
 *   }
 * ];
 *
 * // 过滤包含"销售"的部门或其父部门
 * const filtered = filterTree(tree, node => {
 *   return node.name.includes('销售') || node.children?.some(child => child.name.includes('销售'));
 * });
 *
 * // 结果保留了部门1及其子部门中的销售部
 * console.log(filtered);
 * // [
 * //   {
 * //     id: 1, name: '部门1',
 * //     children: [
 * //       { id: 3, name: '销售部' }
 * //     ]
 * //   }
 * // ]
 */
export const filterTree = <T = TreeNode>(tree: T[], predicate: (node: T) => boolean, options: TreeOptions = {}): T[] => {
  const mergedOptions = { ...DEFAULT_TREE_OPTIONS, ...options }
  const { children, deepCopy } = mergedOptions

  // 如果需要深拷贝数据
  const sourceTree = deepCopy ? JSON.parse(JSON.stringify(tree)) : tree

  // 过滤函数
  function filter(nodes: T[]): T[] {
    if (!nodes || nodes.length === 0) return []

    return nodes.filter((node) => {
      // 递归过滤子节点
      if (node[children!] && node[children!].length > 0) {
        node[children!] = filter(node[children!])
      }

      // 如果有子节点或自身符合条件则保留
      return predicate(node) || (node[children!] && node[children!].length > 0)
    })
  }

  return filter(sourceTree)
}

/**
 * 扁平化树结构为数组
 *
 * @param {Array} tree 树结构数据
 * @param {TreeOptions} options 配置选项
 * @returns {Array} 扁平化后的数组
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       { id: 3, name: '部门3' }
 *     ]
 *   }
 * ];
 *
 * // 扁平化树结构
 * const flatArray = flattenTree(tree);
 * console.log(flatArray);
 * // [
 * //   { id: 1, name: '部门1', children: [...] },
 * //   { id: 2, name: '部门2' },
 * //   { id: 3, name: '部门3' }
 * // ]
 *
 * // 去除结果中的children字段
 * const clean = flattenTree(tree).map(({ children, ...rest }) => rest);
 * console.log(clean);
 * // [
 * //   { id: 1, name: '部门1' },
 * //   { id: 2, name: '部门2' },
 * //   { id: 3, name: '部门3' }
 * // ]
 */
export const flattenTree = <T = TreeNode>(tree: T[], options: TreeOptions = {}): T[] => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }
  const result: T[] = []

  function flatten(nodes: T[]) {
    for (const node of nodes) {
      result.push(node)

      if (node[children!] && node[children!].length > 0) {
        flatten(node[children!])
      }
    }
  }

  flatten(tree)
  return result
}

/**
 * 遍历树结构
 *
 * @param {Array} tree 树结构数据
 * @param {Function} callback 回调函数，接收当前节点和父节点
 * @param {TreeOptions} options 配置选项
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       { id: 3, name: '部门3' }
 *     ]
 *   }
 * ];
 *
 * // 输出所有节点及其父节点
 * traverseTree(tree, (node, parent) => {
 *   console.log(`节点:${node.name}, 父节点:${parent?.name || '无'}`);
 * });
 *
 * // 修改树中所有节点，添加level属性
 * traverseTree(tree, (node, parent, level) => {
 *   node.level = level;
 * });
 */
export const traverseTree = <T = TreeNode>(
  tree: T[],
  callback: (node: T, parent: T | null, level: number) => void,
  options: TreeOptions = {}
): void => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }

  function traverse(nodes: T[], parent: T | null = null, level: number = 0) {
    for (const node of nodes) {
      // 调用回调函数
      callback(node, parent, level)

      // 递归遍历子节点
      if (node[children!] && node[children!].length > 0) {
        traverse(node[children!], node, level + 1)
      }
    }
  }

  traverse(tree)
}

/**
 * 向树中插入节点
 *
 * @param {Array} tree 树结构数据
 * @param {Object} node 要插入的节点
 * @param {any} parentId 父节点ID，如果为null则插入为根节点
 * @param {TreeOptions} options 配置选项
 * @returns {boolean} 是否插入成功
 *
 * @example
 * let tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' }
 *     ]
 *   }
 * ];
 *
 * // 插入到指定父节点
 * const newNode = { id: 3, name: '部门3' };
 * insertNode(tree, newNode, 1);
 *
 * // 插入为根节点
 * const rootNode = { id: 4, name: '部门4' };
 * insertNode(tree, rootNode, null);
 */
export const insertNode = <T = TreeNode>(tree: T[], node: T, parentId: any = null, options: TreeOptions = {}): boolean => {
  const mergedOptions = { ...DEFAULT_TREE_OPTIONS, ...options }
  const { id, children, parentId: parentIdField } = mergedOptions

  // 确保节点有children数组
  if (!node[children!]) {
    node[children!] = []
  }

  // 如果parentId为null，插入为根节点
  if (parentId === null) {
    tree.push(node)
    return true
  }

  // 辅助函数：根据ID查找节点
  const findById = (nodes: T[], targetId: any): T | null => {
    for (const item of nodes) {
      if (item[id!] === targetId) {
        return item
      }

      if (item[children!] && item[children!].length > 0) {
        const found = findById(item[children!], targetId)
        if (found) {
          return found
        }
      }
    }

    return null
  }

  // 查找父节点
  const parent = findById(tree, parentId)
  if (!parent) {
    return false
  }

  // 设置节点的parentId
  node[parentIdField!] = parentId

  // 将节点添加到父节点的children中
  parent[children!].push(node)
  return true
}

/**
 * 从树中删除节点
 *
 * @param {Array} tree 树结构数据
 * @param {Function} predicate 判断函数，返回true表示删除该节点
 * @param {TreeOptions} options 配置选项
 * @returns {boolean} 是否删除成功
 *
 * @example
 * let tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       { id: 3, name: '部门3' }
 *     ]
 *   }
 * ];
 *
 * // 删除ID为3的节点
 * removeNode(tree, node => node.id === 3);
 *
 * // 删除多个节点
 * removeNode(tree, node => [2, 3].includes(node.id));
 */
export const removeNode = <T = TreeNode>(tree: T[], predicate: (node: T) => boolean, options: TreeOptions = {}): boolean => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }
  let removed = false

  // 从数组中删除符合条件的节点
  function removeFromArray(nodes: T[]): T[] {
    const result = []

    for (const node of nodes) {
      if (predicate(node)) {
        removed = true
        continue
      }

      if (node[children!] && node[children!].length > 0) {
        node[children!] = removeFromArray(node[children!])
      }

      result.push(node)
    }

    return result
  }

  // 修改原始tree数组
  const result = removeFromArray([...tree])
  tree.length = 0
  tree.push(...result)

  return removed
}

/**
 * 更新树节点
 *
 * @param {Array} tree 树结构数据
 * @param {Function} predicate 判断函数，返回true表示更新该节点
 * @param {Function} updater 更新函数，接收节点作为参数并返回更新后的节点
 * @param {TreeOptions} options 配置选项
 * @returns {boolean} 是否更新成功
 *
 * @example
 * let tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       { id: 3, name: '部门3' }
 *     ]
 *   }
 * ];
 *
 * // 更新ID为3的节点
 * updateNode(
 *   tree,
 *   node => node.id === 3,
 *   node => ({ ...node, name: '销售部', status: 'active' })
 * );
 *
 * // 批量更新节点
 * updateNode(
 *   tree,
 *   node => node.status === 'pending',
 *   node => ({ ...node, status: 'active' })
 * );
 */
export const updateNode = <T = TreeNode>(tree: T[], predicate: (node: T) => boolean, updater: (node: T) => T, options: TreeOptions = {}): boolean => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }
  let updated = false

  function updateTreeNode(nodes: T[]): T[] {
    return nodes.map((node) => {
      // 如果节点符合条件，更新节点
      if (predicate(node)) {
        updated = true
        const updatedNode = updater(node)

        // 保持子节点不变
        if (node[children!]) {
          updatedNode[children!] = node[children!]
        }

        return updatedNode
      }

      // 递归更新子节点
      if (node[children!] && node[children!].length > 0) {
        node[children!] = updateTreeNode(node[children!])
      }

      return node
    })
  }

  // 更新树结构
  const result = updateTreeNode([...tree])
  tree.length = 0
  tree.push(...result)

  return updated
}

/**
 * 获取树中所有叶子节点
 *
 * @param {Array} tree 树结构数据
 * @param {TreeOptions} options 配置选项
 * @returns {Array} 所有叶子节点
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       {
 *         id: 3, name: '部门3',
 *         children: [
 *           { id: 4, name: '部门4' }
 *         ]
 *       }
 *     ]
 *   },
 *   { id: 5, name: '部门5' }
 * ];
 *
 * // 获取所有叶子节点
 * const leaves = getLeafNodes(tree);
 * console.log(leaves);
 * // [
 * //   { id: 2, name: '部门2' },
 * //   { id: 4, name: '部门4' },
 * //   { id: 5, name: '部门5' }
 * // ]
 */
export const getLeafNodes = <T = TreeNode>(tree: T[], options: TreeOptions = {}): T[] => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }
  const result: T[] = []

  function findLeaves(nodes: T[]) {
    for (const node of nodes) {
      const hasChildren = node[children!] && node[children!].length > 0

      if (!hasChildren) {
        result.push(node)
      } else {
        findLeaves(node[children!])
      }
    }
  }

  findLeaves(tree)
  return result
}

/**
 * 计算树的深度
 *
 * @param {Array} tree 树结构数据
 * @param {TreeOptions} options 配置选项
 * @returns {number} 树的最大深度
 *
 * @example
 * const tree = [
 *   {
 *     id: 1, name: '部门1',
 *     children: [
 *       { id: 2, name: '部门2' },
 *       {
 *         id: 3, name: '部门3',
 *         children: [
 *           { id: 4, name: '部门4' }
 *         ]
 *       }
 *     ]
 *   }
 * ];
 *
 * // 计算树的最大深度
 * const depth = getTreeDepth(tree);
 * console.log(depth); // 3
 */
export const getTreeDepth = <T = TreeNode>(tree: T[], options: TreeOptions = {}): number => {
  const { children } = { ...DEFAULT_TREE_OPTIONS, ...options }

  function calcDepth(nodes: T[]): number {
    if (!nodes || nodes.length === 0) return 0

    let maxDepth = 0
    for (const node of nodes) {
      if (node[children!] && node[children!].length > 0) {
        const childDepth = calcDepth(node[children!])
        maxDepth = Math.max(maxDepth, childDepth)
      }
    }

    return maxDepth + 1
  }

  return calcDepth(tree)
}
