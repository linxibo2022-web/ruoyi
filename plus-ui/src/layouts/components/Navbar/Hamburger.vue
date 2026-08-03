<!-- 菜单折叠按钮 -->
<template>
  <!-- 汉堡菜单按钮 -->
  <div
    class="navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer dark:(text-white hover:text-primary) transition-all duration-300"
    @click="toggleClick"
  >
    <Icon code="hamburger" size="md" class="transition-transform duration-300 ease-out" :class="isActive ? 'scale-x-[-1]' : ''" />
  </div>
</template>

<script setup lang="ts" name="Hamburger">
/**
 * 汉堡菜单组件
 *
 * 用于控制侧边栏的展开和收起，点击时触发toggleClick事件
 * 通过isActive属性控制图标的水平翻转状态
 */

/**
 * 汉堡菜单组件的属性接口
 */
interface HamburgerProps {
  /**
   * 菜单是否处于激活状态
   * 当为true时，图标水平翻转
   * @default false
   */
  isActive?: boolean
}

/**
 * 组件属性定义，提供默认值
 */
const props = withDefaults(defineProps<HamburgerProps>(), {
  isActive: false
})

/**
 * 定义组件可触发的事件
 * toggleClick: 当汉堡按钮被点击时触发
 */
const emit = defineEmits(['toggleClick'])

/**
 * 处理切换点击事件
 * 触发 toggleClick 事件，通常用于控制侧边栏的展开/收起
 */
const toggleClick = () => {
  emit('toggleClick')
}
</script>

<style lang="scss" scoped></style>
