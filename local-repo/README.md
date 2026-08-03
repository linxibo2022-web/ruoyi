# 本地仓库说明

本地仓库用于存放项目特有的第三方 jar 包（Maven 中央仓库没有的）。

## 目录结构规则

按照 Maven 规范创建目录：`groupId/artifactId/version/`

**示例：**
```
local-repo/
└── plus/                           ← groupId 的第一部分
    └── ruoyi/                      ← groupId 的第二部分
        └── ruoyi-common-license/   ← artifactId
            └── 5.5.0/              ← version
                ├── ruoyi-common-license-5.5.0.jar
                └── ruoyi-common-license-5.5.0.pom
```

## 配置 pom.xml

在父 pom.xml 中添加本地仓库：
```xml
<repositories>
    <repository>
        <id>local-repo</id>
        <url>file://${maven.multiModuleProjectDirectory}/local-repo</url>
    </repository>
</repositories>
```

## 使用依赖

在需要的模块中正常引用：
```xml
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-license</artifactId>
    <version>5.5.0</version>
</dependency>
```

## 升级：防止jar包被删除

在核心包比如core的utils包下放置ClientLicenseModuleValidator.java , 然后在合适的位置比如启动的时候或者某个bean加载的时候，触发ClientLicenseModuleValidator.check()方法调用就可以了。
框架已经在RuoyiPlus启动类中添加了该方法调用。需要的时候打开注释即可
