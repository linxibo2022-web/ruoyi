### 自定义图标

可以类似system系统图标那样通过iconfont配置图标，下载图标文件置于此目录，然后在main.ts中导入样式文件如：import '@/assets/icons/system/iconfont.css'
那本图标则可以导入为 import '@/assets/icons/custom/iconfont.css'
类型声明会通过vite下的iconfont-types自动生成，无需手动添加
