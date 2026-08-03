#!/usr/bin/env python3
"""
RuoYi Plus UniApp - 一键自动化部署脚本

流程：
  后端：本地 mvn package → docker build → docker save → SSH 传输 → docker load → compose up
  前端：本地 pnpm build → tar 打包 → SSH 传输 → 解压到 Nginx 目录
  H5：（可选）本地 pnpm build:h5 → 同上

用法：
  python deploy.py                # 交互式选择部署目标
  python deploy.py backend        # 只部署后端
  python deploy.py frontend       # 只部署前端 PC
  python deploy.py h5             # 只部署 H5 移动端
  python deploy.py all            # 部署后端 + 前端
  python deploy.py full           # 部署后端 + 前端 + H5
  python deploy.py --init         # 首次部署引导（生成服务器 compose 配置）

前提：
  - 本地安装 Docker Desktop 且已启动
  - 本地安装 Maven、pnpm
  - pip install paramiko
"""

import os
import sys
import json
import subprocess
import time
import getpass
import secrets

# ─── 路径常量 ──────────────────────────────
PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
CONFIG_FILE = os.path.join(PROJECT_ROOT, ".deploy-config.json")
BACKEND_DIR = os.path.join(PROJECT_ROOT, "ruoyi-admin")
FRONTEND_DIR = os.path.join(PROJECT_ROOT, "plus-ui")
H5_DIR = os.path.join(PROJECT_ROOT, "plus-uniapp")

# ─── 默认配置 ──────────────────────────────
DEFAULT_CONFIG = {
    "server": {
        "host": "",
        "port": 22,
        "username": "root",
        "auth_type": "password",
        "password": "",
        "key_file": ""
    },
    "backend": {
        "deploy_dir": "/opt/ryplus",
        "image_name": "ryplus_uni",
        "image_tag": "latest",
        "server_port": 5500,
        "compose_mode": "single"
    },
    "frontend": {
        "nginx_html_dir": "/docker/nginx/html",
        "pc_subdir": "",
        "h5_subdir": "h5"
    },
    "options": {
        "skip_maven_build": False,
        "skip_frontend_build": False,
        "health_check_timeout": 30
    }
}


# ═══════════════════════════════════════════
# 工具函数
# ═══════════════════════════════════════════

def print_header(title):
    """打印步骤标题"""
    print(f"\n{'─' * 50}")
    print(f"  {title}")
    print(f"{'─' * 50}")


def print_step(msg):
    """打印步骤信息"""
    print(f"  >>> {msg}")


def print_ok(msg):
    """打印成功信息"""
    print(f"  [OK] {msg}")


def print_warn(msg):
    """打印警告信息"""
    print(f"  [!!] {msg}")


def print_err(msg):
    """打印错误信息"""
    print(f"  [ERR] {msg}")


def confirm(msg):
    """确认操作，返回 True/False"""
    answer = input(f"\n  {msg} (y/n): ").strip().lower()
    return answer in ("y", "yes", "")


def run(cmd, cwd=None, check=True, timeout=600):
    """执行本地命令"""
    print(f"  $ {cmd}")
    result = subprocess.run(
        cmd, shell=True, cwd=cwd or PROJECT_ROOT,
        capture_output=False, text=True, timeout=timeout
    )
    if check and result.returncode != 0:
        print_err(f"命令失败 (exit {result.returncode})")
        sys.exit(1)
    return result


def ssh_exec(ssh, cmd, timeout=300):
    """SSH 执行远程命令"""
    print(f"  [SSH] {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode(errors='replace')
    err = stderr.read().decode(errors='replace')
    code = stdout.channel.recv_exit_status()
    if out.strip():
        for line in out.strip().split('\n')[:20]:
            print(f"        {line}")
    if err.strip() and code != 0:
        for line in err.strip().split('\n')[:10]:
            print(f"  STDERR: {line}")
    return code, out, err


def file_size_mb(path):
    """获取文件大小（MB）"""
    return os.path.getsize(path) / 1024 / 1024


# ═══════════════════════════════════════════
# 配置管理
# ═══════════════════════════════════════════

def load_config():
    """加载配置文件，不存在则引导创建"""
    if os.path.exists(CONFIG_FILE):
        with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
            config = json.load(f)
        # 合并默认值（兼容旧版本配置缺少新字段）
        for section in DEFAULT_CONFIG:
            if section not in config:
                config[section] = DEFAULT_CONFIG[section]
            elif isinstance(DEFAULT_CONFIG[section], dict):
                for key in DEFAULT_CONFIG[section]:
                    if key not in config[section]:
                        config[section][key] = DEFAULT_CONFIG[section][key]
        return config

    print_header("首次运行 - 创建部署配置")
    print("  配置将保存到 .deploy-config.json（已在 .gitignore 中排除）\n")

    config = json.loads(json.dumps(DEFAULT_CONFIG))

    # 服务器连接
    config["server"]["host"] = input("  服务器 IP 地址: ").strip()
    port_input = input("  SSH 端口 [22]: ").strip()
    config["server"]["port"] = int(port_input) if port_input else 22
    username_input = input("  SSH 用户名 [root]: ").strip()
    config["server"]["username"] = username_input if username_input else "root"

    auth_type = input("  认证方式 (password/key) [password]: ").strip().lower()
    if auth_type == "key":
        config["server"]["auth_type"] = "key"
        config["server"]["key_file"] = input("  SSH 密钥文件路径: ").strip()
    else:
        config["server"]["auth_type"] = "password"
        config["server"]["password"] = getpass.getpass("  SSH 密码: ")

    # 部署目录
    deploy_dir = input("  后端部署目录 [/opt/ryplus]: ").strip()
    config["backend"]["deploy_dir"] = deploy_dir if deploy_dir else "/opt/ryplus"

    # 后端端口
    port = input("  后端服务端口 [5500]: ").strip()
    config["backend"]["server_port"] = int(port) if port else 5500

    # compose 模式
    mode = input("  部署模式 single(单实例)/cluster(双实例) [single]: ").strip()
    config["backend"]["compose_mode"] = mode if mode in ("single", "cluster") else "single"

    # Nginx 目录
    nginx_dir = input("  Nginx HTML 目录 [/docker/nginx/html]: ").strip()
    config["frontend"]["nginx_html_dir"] = nginx_dir if nginx_dir else "/docker/nginx/html"

    # 保存
    save_config(config)
    print_ok(f"配置已保存到 {CONFIG_FILE}")
    return config


def save_config(config):
    """保存配置到文件"""
    with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
        json.dump(config, f, indent=2, ensure_ascii=False)


# ═══════════════════════════════════════════
# SSH 连接
# ═══════════════════════════════════════════

def create_ssh(config):
    """创建 SSH 连接"""
    try:
        import paramiko
    except ImportError:
        print_err("需要安装 paramiko: pip install paramiko")
        sys.exit(1)

    server = config["server"]
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    print_step(f"连接服务器 {server['host']}:{server['port']}...")

    connect_kwargs = {
        "hostname": server["host"],
        "port": server["port"],
        "username": server["username"],
        "timeout": 15,
    }

    if server["auth_type"] == "key":
        connect_kwargs["key_filename"] = server["key_file"]
    else:
        connect_kwargs["password"] = server["password"]

    ssh.connect(**connect_kwargs)
    print_ok("SSH 连接成功")
    return ssh


# ═══════════════════════════════════════════
# 后端部署
# ═══════════════════════════════════════════

def deploy_backend(config):
    """后端部署全流程"""
    backend = config["backend"]
    full_image = f"{backend['image_name']}:{backend['image_tag']}"
    tar_file = f"{backend['image_name']}.tar.gz"
    tar_path = os.path.join(PROJECT_ROOT, tar_file)
    deploy_dir = backend["deploy_dir"]

    # ── 说明即将执行的操作 ──
    print_header("后端部署计划")
    print(f"  1. 本地执行 mvn clean package -DskipTests（编译 JAR）")
    print(f"  2. 本地执行 docker build -t {full_image}（构建镜像）")
    print(f"  3. 导出镜像为 {tar_file}（docker save | gzip）")
    print(f"  4. SSH 传输到服务器 {config['server']['host']}:{deploy_dir}/")
    print(f"  5. 服务器 docker load + docker compose up -d")
    print(f"  6. 健康检查验证服务启动")

    if config["options"]["skip_maven_build"]:
        print(f"\n  [跳过] Maven 构建（skip_maven_build=true）")

    if not confirm("确认开始后端部署？"):
        print("  已跳过后端部署")
        return

    # ── Step 1: Maven 构建 ──
    if not config["options"]["skip_maven_build"]:
        print_header("Step 1/6: Maven 构建")
        jar_path = os.path.join(BACKEND_DIR, "target", "ryplus_uni.jar")

        run("mvn clean package -DskipTests", cwd=PROJECT_ROOT, timeout=300)

        if not os.path.exists(jar_path):
            print_err(f"构建产物不存在: {jar_path}")
            sys.exit(1)
        print_ok(f"JAR 构建成功 ({file_size_mb(jar_path):.1f} MB)")
    else:
        print_header("Step 1/6: Maven 构建 [跳过]")

    # ── Step 2: Docker 构建 ──
    print_header("Step 2/6: Docker 构建镜像")
    run(f"docker build -t {full_image} .", cwd=BACKEND_DIR)
    print_ok(f"镜像 {full_image} 构建成功")

    # ── Step 3: 导出镜像 ──
    print_header("Step 3/6: 导出镜像为 tar.gz")
    run(f'docker save {full_image} | gzip > "{tar_path}"')
    print_ok(f"镜像已导出: {tar_file} ({file_size_mb(tar_path):.1f} MB)")

    # ── Step 4: SSH 传输 ──
    print_header("Step 4/6: 传输镜像到服务器")
    ssh = create_ssh(config)
    ssh_exec(ssh, f"mkdir -p {deploy_dir}")

    sftp = ssh.open_sftp()
    remote_tar = f"{deploy_dir}/{tar_file}"
    size = os.path.getsize(tar_path)
    uploaded = [0]
    last_print = [0]

    def progress(sent, total):
        uploaded[0] = sent
        now = time.time()
        if now - last_print[0] >= 2 or sent == total:
            last_print[0] = now
            pct = sent * 100 // total
            print(f"\r  上传进度: {sent / 1024 / 1024:.1f}/{total / 1024 / 1024:.1f} MB ({pct}%)", end="", flush=True)

    print_step(f"上传 {tar_file} ({file_size_mb(tar_path):.1f} MB)...")
    sftp.put(tar_path, remote_tar, callback=progress)
    print()
    sftp.close()
    print_ok("上传完成")

    # ── Step 5: 服务器加载镜像并启动 ──
    print_header("Step 5/6: 服务器加载镜像并启动")

    # 加载镜像
    code, out, err = ssh_exec(ssh, f"docker load < {remote_tar}", timeout=120)
    if code != 0:
        print_err("镜像加载失败")
        ssh.close()
        sys.exit(1)
    print_ok("镜像加载成功")

    # 清理服务器 tar
    ssh_exec(ssh, f"rm -f {remote_tar}")

    # 检查 compose 文件是否存在
    code, out, _ = ssh_exec(ssh, f"test -f {deploy_dir}/docker-compose.yml && echo EXISTS || echo MISSING", timeout=10)

    if "MISSING" in out:
        print_warn("服务器上未找到 docker-compose.yml，正在生成...")
        compose_content = generate_compose(config)
        # 通过 sftp 写入 compose 文件
        sftp = ssh.open_sftp()
        with sftp.open(f"{deploy_dir}/docker-compose.yml", 'w') as f:
            f.write(compose_content)
        sftp.close()
        print_ok("docker-compose.yml 已生成")
    else:
        print_ok("使用服务器上已有的 docker-compose.yml")

    # 启动/重建容器
    ssh_exec(ssh, f"cd {deploy_dir} && docker compose up -d --force-recreate", timeout=120)

    # ── Step 6: 健康检查 ──
    print_header("Step 6/6: 健康检查")
    server_port = backend["server_port"]
    timeout_sec = config["options"]["health_check_timeout"]
    health_url = f"http://127.0.0.1:{server_port}/actuator/health"

    print_step(f"等待服务启动（最多 {timeout_sec} 秒）...")
    for i in range(timeout_sec):
        time.sleep(1)
        code, out, _ = ssh_exec(ssh, f"curl -sf {health_url} 2>/dev/null || true", timeout=10)
        if '"status":"UP"' in out or '"status": "UP"' in out:
            print_ok(f"服务启动成功！({i + 1}s)")
            break
        if (i + 1) % 5 == 0:
            print(f"        等待中... ({i + 1}s)")
    else:
        print_warn(f"健康检查超时（{timeout_sec}s），服务可能还在启动中")
        print_step("查看容器日志：")
        ssh_exec(ssh, f"docker logs {backend['image_name']} --tail 15 2>&1", timeout=10)

    ssh.close()

    # 清理本地 tar
    if os.path.exists(tar_path):
        os.remove(tar_path)
        print_ok(f"已清理本地 {tar_file}")


def generate_compose(config):
    """生成 docker-compose.yml 内容"""
    backend = config["backend"]
    port = backend["server_port"]
    image = f"{backend['image_name']}:{backend['image_tag']}"
    jwt_secret = secrets.token_urlsafe(16)

    compose = f"""# RuoYi Plus UniApp - Docker Compose
# 由 deploy.py 自动生成，可手动修改环境变量
# MySQL 和 Redis 由 1Panel 管理，此处仅部署应用容器

services:
  {backend['image_name']}:
    image: {image}
    container_name: {backend['image_name']}
    environment:
      TZ: Asia/Shanghai
      SERVER_PORT: {port}
      SPRING_PROFILES_ACTIVE: prod
      DEBUG_ARGS: ""

      # === 数据库配置（连接 1Panel 管理的 MySQL）===
      DB_HOST: 127.0.0.1
      DB_PORT: 3306
      DB_NAME: ryplus_uni
      DB_USERNAME: root
      DB_PASSWORD: "CHANGE_ME"

      # === Redis 配置（连接 1Panel 管理的 Redis）===
      REDIS_HOST: 127.0.0.1
      REDIS_PORT: 6379
      REDIS_PASSWORD: ""

      # === 安全配置（请修改以下默认值）===
      JWT_SECRET_KEY: "{jwt_secret}"
      SPRINGDOC_ENABLED: false

      # === 功能开关 ===
      MONITOR_ENABLED: false
      SNAIL_JOB_ENABLED: false
      LANGCHAIN4J_ENABLED: false
      OPEN_API_ENABLED: false
      MAIL_ENABLED: false
      ROCKETMQ_ENABLED: false

    volumes:
      - {backend['deploy_dir']}/logs/:/ruoyi/server/logs/
      - {backend['deploy_dir']}/upload/:/ruoyi/server/upload/
      - {backend['deploy_dir']}/temp/:/ruoyi/server/temp/
    network_mode: "host"
    restart: always
"""

    # 双实例模式
    if backend.get("compose_mode") == "cluster":
        port2 = port + 1
        compose += f"""
  {backend['image_name']}2:
    image: {image}
    container_name: {backend['image_name']}2
    environment:
      TZ: Asia/Shanghai
      SERVER_PORT: {port2}
      SPRING_PROFILES_ACTIVE: prod
      DEBUG_ARGS: ""

      DB_HOST: 127.0.0.1
      DB_PORT: 3306
      DB_NAME: ryplus_uni
      DB_USERNAME: root
      DB_PASSWORD: "CHANGE_ME"

      REDIS_HOST: 127.0.0.1
      REDIS_PORT: 6379
      REDIS_PASSWORD: ""

      JWT_SECRET_KEY: "{jwt_secret}"
      SPRINGDOC_ENABLED: false

      MONITOR_ENABLED: false
      SNAIL_JOB_ENABLED: false
      LANGCHAIN4J_ENABLED: false
      OPEN_API_ENABLED: false
      MAIL_ENABLED: false
      ROCKETMQ_ENABLED: false

    volumes:
      - {backend['deploy_dir']}2/logs/:/ruoyi/server/logs/
      - {backend['deploy_dir']}/upload/:/ruoyi/server/upload/
      - {backend['deploy_dir']}2/temp/:/ruoyi/server/temp/
    network_mode: "host"
    restart: always
"""

    return compose


# ═══════════════════════════════════════════
# 前端部署
# ═══════════════════════════════════════════

def deploy_frontend(config):
    """前端 PC 部署"""
    frontend = config["frontend"]
    nginx_dir = frontend["nginx_html_dir"]
    pc_subdir = frontend.get("pc_subdir", "")
    # PC 部署到根目录或子目录
    if pc_subdir:
        remote_dir = f"{nginx_dir}/{pc_subdir}"
    else:
        remote_dir = nginx_dir

    dist_dir = os.path.join(FRONTEND_DIR, "dist")
    tar_file = "plus-ui-dist.tar.gz"
    tar_path = os.path.join(PROJECT_ROOT, tar_file)

    # ── 说明即将执行的操作 ──
    print_header("前端 PC 部署计划")
    print(f"  1. 本地执行 pnpm build（构建 plus-ui）")
    print(f"  2. 打包 dist 目录为 {tar_file}")
    print(f"  3. SSH 传输到服务器")
    print(f"  4. 解压到 {remote_dir}/")

    if config["options"]["skip_frontend_build"]:
        print(f"\n  [跳过] 前端构建（skip_frontend_build=true）")

    if not confirm("确认开始前端 PC 部署？"):
        print("  已跳过前端 PC 部署")
        return

    # ── Step 1: 构建 ──
    if not config["options"]["skip_frontend_build"]:
        print_header("前端构建: pnpm build")
        run("pnpm install", cwd=FRONTEND_DIR, timeout=120)
        run("pnpm build", cwd=FRONTEND_DIR, timeout=120)

        if not os.path.isdir(dist_dir):
            print_err(f"构建产物不存在: {dist_dir}")
            sys.exit(1)
        print_ok("前端 PC 构建成功")
    else:
        print_step("跳过前端构建")

    # ── Step 2: 打包 ──
    print_step(f"打包 {tar_file}...")
    # 使用 tar 命令，Windows 下 Git Bash 自带 tar
    run(f'tar czf "{tar_path}" -C "{dist_dir}" .')
    print_ok(f"打包完成 ({file_size_mb(tar_path):.1f} MB)")

    # ── Step 3: 传输 ──
    print_step("传输到服务器...")
    ssh = create_ssh(config)
    deploy_dir = config["backend"]["deploy_dir"]
    remote_tar = f"{deploy_dir}/{tar_file}"

    ssh_exec(ssh, f"mkdir -p {deploy_dir}")
    sftp = ssh.open_sftp()
    sftp.put(tar_path, remote_tar)
    sftp.close()
    print_ok("上传完成")

    # ── Step 4: 解压 ──
    print_step(f"解压到 {remote_dir}/...")
    ssh_exec(ssh, f"mkdir -p {remote_dir}")
    # 先备份旧文件（保留 index.html 以免短暂 404）
    ssh_exec(ssh, f"cd {remote_dir} && rm -rf assets static css js img fonts 2>/dev/null; true")
    ssh_exec(ssh, f"tar xzf {remote_tar} -C {remote_dir}")
    ssh_exec(ssh, f"rm -f {remote_tar}")
    print_ok(f"前端 PC 已部署到 {remote_dir}/")

    ssh.close()

    # 清理本地
    if os.path.exists(tar_path):
        os.remove(tar_path)
        print_ok(f"已清理本地 {tar_file}")


def deploy_h5(config):
    """H5 移动端部署"""
    frontend = config["frontend"]
    nginx_dir = frontend["nginx_html_dir"]
    h5_subdir = frontend.get("h5_subdir", "h5")
    remote_dir = f"{nginx_dir}/{h5_subdir}"

    h5_dist_dir = os.path.join(H5_DIR, "dist", "build", "h5")
    tar_file = "plus-h5-dist.tar.gz"
    tar_path = os.path.join(PROJECT_ROOT, tar_file)

    # ── 说明即将执行的操作 ──
    print_header("H5 移动端部署计划")
    print(f"  1. 本地执行 pnpm build:h5（构建 plus-uniapp H5）")
    print(f"  2. 打包 dist 目录为 {tar_file}")
    print(f"  3. SSH 传输到服务器")
    print(f"  4. 解压到 {remote_dir}/")

    if not confirm("确认开始 H5 部署？"):
        print("  已跳过 H5 部署")
        return

    # ── 构建 ──
    if not config["options"]["skip_frontend_build"]:
        print_header("H5 构建: pnpm build:h5")
        run("pnpm install", cwd=H5_DIR, timeout=120)
        run("pnpm build:h5", cwd=H5_DIR, timeout=120)

        if not os.path.isdir(h5_dist_dir):
            print_err(f"构建产物不存在: {h5_dist_dir}")
            sys.exit(1)
        print_ok("H5 构建成功")

    # ── 打包传输解压 ──
    print_step(f"打包 {tar_file}...")
    run(f'tar czf "{tar_path}" -C "{h5_dist_dir}" .')
    print_ok(f"打包完成 ({file_size_mb(tar_path):.1f} MB)")

    ssh = create_ssh(config)
    deploy_dir = config["backend"]["deploy_dir"]
    remote_tar = f"{deploy_dir}/{tar_file}"

    sftp = ssh.open_sftp()
    sftp.put(tar_path, remote_tar)
    sftp.close()

    ssh_exec(ssh, f"mkdir -p {remote_dir}")
    ssh_exec(ssh, f"cd {remote_dir} && rm -rf assets static css js img fonts 2>/dev/null; true")
    ssh_exec(ssh, f"tar xzf {remote_tar} -C {remote_dir}")
    ssh_exec(ssh, f"rm -f {remote_tar}")
    print_ok(f"H5 已部署到 {remote_dir}/")

    ssh.close()

    if os.path.exists(tar_path):
        os.remove(tar_path)
        print_ok(f"已清理本地 {tar_file}")


# ═══════════════════════════════════════════
# 首次部署引导
# ═══════════════════════════════════════════

def init_deploy(config):
    """首次部署引导：在服务器上创建目录结构和 compose 文件"""
    backend = config["backend"]
    deploy_dir = backend["deploy_dir"]

    print_header("首次部署引导")
    print(f"  即将在服务器 {config['server']['host']} 上执行：")
    print(f"  1. 创建部署目录 {deploy_dir}/")
    print(f"  2. 创建子目录（logs、upload、temp）")
    print(f"  3. 生成 docker-compose.yml")
    print()
    print_warn("生成的 compose 文件使用默认数据库密码 CHANGE_ME")
    print_warn("请在部署前修改服务器上的 docker-compose.yml 中的密码配置")

    if not confirm("确认执行首次初始化？"):
        return

    ssh = create_ssh(config)

    # 创建目录
    ssh_exec(ssh, f"mkdir -p {deploy_dir}/logs {deploy_dir}/upload {deploy_dir}/temp")

    if backend.get("compose_mode") == "cluster":
        ssh_exec(ssh, f"mkdir -p {deploy_dir}2/logs {deploy_dir}2/temp")

    # 生成 compose
    compose_content = generate_compose(config)
    sftp = ssh.open_sftp()
    compose_path = f"{deploy_dir}/docker-compose.yml"

    # 检查是否已存在
    try:
        sftp.stat(compose_path)
        if not confirm("docker-compose.yml 已存在，是否覆盖？"):
            print("  保留现有配置")
            sftp.close()
            ssh.close()
            return
    except FileNotFoundError:
        pass

    with sftp.open(compose_path, 'w') as f:
        f.write(compose_content)
    sftp.close()

    print_ok(f"docker-compose.yml 已创建于 {compose_path}")
    print()
    print("  下一步操作：")
    print(f"  1. SSH 登录服务器，编辑 {compose_path}")
    print(f"     修改 DB_PASSWORD、REDIS_PASSWORD、JWT_SECRET_KEY 等")
    print(f"  2. 运行 python deploy.py backend 部署后端")
    print(f"  3. 运行 python deploy.py frontend 部署前端")

    ssh.close()


# ═══════════════════════════════════════════
# 主入口
# ═══════════════════════════════════════════

def show_menu():
    """交互式菜单"""
    print()
    print("  请选择部署目标：")
    print("  1. backend   - 后端（Docker 镜像）")
    print("  2. frontend  - 前端 PC（静态文件）")
    print("  3. h5        - H5 移动端（静态文件）")
    print("  4. all       - 后端 + 前端 PC")
    print("  5. full      - 后端 + 前端 PC + H5")
    print("  6. init      - 首次部署引导")
    print("  0. 退出")
    print()
    choice = input("  请输入编号或名称: ").strip().lower()

    mapping = {
        "1": "backend", "backend": "backend",
        "2": "frontend", "frontend": "frontend",
        "3": "h5", "h5": "h5",
        "4": "all", "all": "all",
        "5": "full", "full": "full",
        "6": "init", "init": "init",
        "0": "exit", "exit": "exit", "q": "exit",
    }
    return mapping.get(choice, "")


def main():
    print("=" * 50)
    print("  RuoYi Plus UniApp - 一键自动化部署")
    print("=" * 50)

    # 解析命令行参数
    args = sys.argv[1:]
    if args:
        target = args[0].lower().lstrip("-")
    else:
        config = load_config()
        target = show_menu()

    if not args:
        pass  # config 已加载
    else:
        config = load_config()

    if target == "exit" or not target:
        print("  已退出")
        return

    if target == "init":
        init_deploy(config)
    elif target == "backend":
        deploy_backend(config)
    elif target == "frontend":
        deploy_frontend(config)
    elif target == "h5":
        deploy_h5(config)
    elif target == "all":
        deploy_backend(config)
        deploy_frontend(config)
    elif target == "full":
        deploy_backend(config)
        deploy_frontend(config)
        deploy_h5(config)
    else:
        print_err(f"未知目标: {target}")
        print("  可用: backend, frontend, h5, all, full, --init")
        sys.exit(1)

    print()
    print("=" * 50)
    print("  部署完成！")
    print("=" * 50)


if __name__ == "__main__":
    main()
