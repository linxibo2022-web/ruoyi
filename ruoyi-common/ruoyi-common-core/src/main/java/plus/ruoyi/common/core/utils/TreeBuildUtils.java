package plus.ruoyi.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 树形结构构建工具类
 * <p>基于HuTool的TreeUtil进行扩展，提供树形数据结构的构建和操作功能</p>
 * <p>主要用于将平级数据转换为树形结构，支持叶子节点提取等树形操作</p>
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeBuildUtils extends TreeUtil {

    /**
     * 默认树形节点配置
     * <p>将节点名称字段设置为"label"，适配前端组件的字段要求</p>
     */
    public static final TreeNodeConfig DEFAULT_CONFIG = TreeNodeConfig.DEFAULT_CONFIG.setNameKey("label");

    /**
     * 构建树形结构（自动识别根节点）
     * <p>示例：{@code List<Tree<Long>> tree = TreeBuildUtils.build(menuList, Menu::getId, Menu::getParentId, Menu::getName)}</p>
     * <p>自动以第一个节点的parentId作为根节点ID</p>
     *
     * @param <T> 输入节点的类型
     * @param <K> 节点ID的类型
     * @param list 节点列表，包含要构建树形结构的所有节点
     * @param nodeParser 解析器，用于将输入节点转换为树节点
     * @return 构建好的树形结构列表，如果输入为空则返回空列表
     */
    public static <T, K> List<Tree<K>> build(List<T> list, NodeParser<T, K> nodeParser) {
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        K k = ReflectUtils.invokeGetter(list.get(0), "parentId");
        return TreeUtil.build(list, k, DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 构建树形结构（指定根节点）
     * <p>示例：{@code List<Tree<Long>> tree = TreeBuildUtils.buildTree(menuList, 0L, nodeParser)}</p>
     *
     * @param <T> 输入节点的类型
     * @param <K> 节点ID的类型
     * @param list 节点列表，包含要构建树形结构的所有节点
     * @param rootId 根节点ID，作为树的顶级父节点
     * @param nodeParser 解析器，用于将输入节点转换为树节点
     * @return 构建好的树形结构列表，如果输入为空则返回空列表
     */
    public static <T, K> List<Tree<K>> build(List<T> list, K rootId, NodeParser<T, K> nodeParser) {
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return TreeUtil.build(list, rootId, DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 获取树形结构中的所有叶子节点
     * <p>示例：{@code List<Tree<Long>> leafNodes = TreeBuildUtils.getLeafNodes(treeList)}</p>
     * <p>叶子节点是指没有子节点的节点</p>
     *
     * @param <K> 节点ID的类型
     * @param treeNodes 树形节点列表
     * @return 包含所有叶子节点的列表，如果输入为空则返回空列表
     */
    public static <K> List<Tree<K>> getLeafNodes(List<Tree<K>> treeNodes) {
        if (CollUtil.isEmpty(treeNodes)) {
            return CollUtil.newArrayList();
        }
        return treeNodes.stream()
            .flatMap(TreeBuildUtils::extractLeafNodes)
            .collect(Collectors.toList());
    }

    /**
     * 获取指定树节点下的所有子节点（包括自身）
     * <p>示例：{@code List<Tree<Long>> allNodes = TreeBuildUtils.getAllNodes(rootNode)}</p>
     *
     * @param <K> 节点ID的类型
     * @param treeNode 根节点
     * @return 包含指定节点及其所有子节点的扁平列表
     */
    public static <K> List<Tree<K>> getAllNodes(Tree<K> treeNode) {
        return Stream.concat(
            Stream.of(treeNode),
            treeNode.getChildren().stream().flatMap(child -> getAllNodes(child).stream())
        ).collect(Collectors.toList());
    }

    /**
     * 查找指定ID的节点
     * <p>示例：{@code Tree<Long> node = TreeBuildUtils.findNodeById(treeList, 123L)}</p>
     *
     * @param <K> 节点ID的类型
     * @param treeNodes 树形节点列表
     * @param nodeId 要查找的节点ID
     * @return 找到的节点，如果不存在则返回null
     */
    public static <K> Tree<K> findNodeById(List<Tree<K>> treeNodes, K nodeId) {
        if (CollUtil.isEmpty(treeNodes) || nodeId == null) {
            return null;
        }

        for (Tree<K> node : treeNodes) {
            if (nodeId.equals(node.getId())) {
                return node;
            }
            // 递归查找子节点
            Tree<K> found = findNodeById(node.getChildren(), nodeId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 获取树的最大深度
     * <p>示例：{@code int depth = TreeBuildUtils.getMaxDepth(treeList)}</p>
     *
     * @param <K> 节点ID的类型
     * @param treeNodes 树形节点列表
     * @return 树的最大深度，空树返回0
     */
    public static <K> int getMaxDepth(List<Tree<K>> treeNodes) {
        if (CollUtil.isEmpty(treeNodes)) {
            return 0;
        }

        return treeNodes.stream()
            .mapToInt(node -> 1 + getMaxDepth(node.getChildren()))
            .max()
            .orElse(0);
    }

    /**
     * 递归提取指定节点的所有叶子节点
     *
     * @param <K> 节点ID的类型
     * @param node 要查找叶子节点的根节点
     * @return 包含所有叶子节点的流
     */
    private static <K> Stream<Tree<K>> extractLeafNodes(Tree<K> node) {
        if (!node.hasChild()) {
            // 当前节点是叶子节点
            return Stream.of(node);
        } else {
            // 递归获取所有子节点的叶子节点
            return node.getChildren().stream()
                .flatMap(TreeBuildUtils::extractLeafNodes);
        }
    }

}
