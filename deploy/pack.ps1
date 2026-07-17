# 智能手环MES - 部署文件打包脚本
# 在本地Windows执行，生成部署包

$projectRoot = "d:\SmartBraceletMES"
$deployDir = "d:\SmartBraceletMES\deploy"

Write-Host "开始打包部署文件..." -ForegroundColor Green

# 1. 创建目录结构
$serverDirs = @(
    "$deployDir\server\sql",
    "$deployDir\server\backend",
    "$deployDir\server\web-dist"
)
foreach ($dir in $serverDirs) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

# 2. 复制SQL文件
Write-Host "复制SQL文件..." -ForegroundColor Yellow
$sqlFiles = @(
    "sql\schema.sql",
    "sql\init_data.sql",
    "sql\sample_data.sql",
    "sql\supplement_data.sql",
    "backend\src\main\resources\demo_schema.sql",
    "backend\src\main\resources\demo_data.sql"
)
foreach ($file in $sqlFiles) {
    $src = "$projectRoot\$file"
    $fileName = Split-Path $file -Leaf
    $dst = "$deployDir\server\sql\$fileName"
    if (Test-Path $src) {
        Copy-Item $src $dst -Force
        Write-Host "  ✓ $fileName"
    } else {
        Write-Host "  ✗ 未找到: $file" -ForegroundColor Red
    }
}

# 3. 复制后端源码（排除target和node_modules）
Write-Host "复制后端源码..." -ForegroundColor Yellow
$backendSrc = "$projectRoot\backend"
$backendDst = "$deployDir\server\backend"

# 使用robocopy排除不需要的目录
robocopy $backendSrc $backendDst /E /XD target node_modules .git /XF *.class /NJH /NJS /NP /NDL /NC /NS /NP 2>$null
Write-Host "  ✓ 后端源码复制完成"

# 4. 复制Web前端dist
Write-Host "复制Web前端..." -ForegroundColor Yellow
$webDist = "$projectRoot\web-admin\dist"
$webDst = "$deployDir\server\web-dist"
if (Test-Path $webDist) {
    robocopy $webDist $webDst /E /NJH /NJS /NP /NDL /NC /NS /NP 2>$null
    Write-Host "  ✓ Web前端复制完成"
} else {
    Write-Host "  ✗ Web前端dist目录不存在，请先运行 npm run build" -ForegroundColor Red
}

# 5. 复制部署脚本
Write-Host "复制部署脚本..." -ForegroundColor Yellow
Copy-Item "$deployDir\deploy.sh" "$deployDir\server\deploy.sh" -Force
Write-Host "  ✓ deploy.sh"

# 6. 生成上传说明
$readme = @"
============================================
  智能手环MES系统 - 服务器部署包
============================================

部署步骤：
1. 将 server 文件夹整体上传到阿里云服务器 /opt/mes-package/
   (可用scp或WinSCP等工具)

2. SSH登录服务器，执行以下命令：

   # 移动文件到目标目录
   mkdir -p /opt/mes
   cp -r /opt/mes-package/server/* /opt/mes/

   # 赋予执行权限并运行部署脚本
   cd /opt/mes
   chmod +x deploy.sh
   sudo ./deploy.sh

3. 等待脚本执行完成（约5-10分钟）

4. 浏览器访问 http://47.105.66.12 验证

5. 小程序在微信开发者工具中打开 mini-app 目录
   详情中勾选「不校验合法域名」

============================================
"@
Set-Content -Path "$deployDir\README.txt" -Value $readme -Encoding UTF8

Write-Host ""
Write-Host "打包完成！" -ForegroundColor Green
Write-Host "部署文件位于: $deployDir\server" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "  1. 将 $deployDir\server 文件夹上传到服务器 /opt/mes-package/"
Write-Host "  2. SSH登录服务器执行部署脚本"
Write-Host ""
