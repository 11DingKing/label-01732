# 游戏卡密管理系统

一个完整的游戏卡密生成、管理和核销系统，支持批量发卡、卡密查询、核销管理和用户权限控制。

## How to Run

### 方式一：Docker Compose 一键部署（推荐）

```bash
# 1. 构建并启动所有服务
docker-compose up --build -d

# 2. 查看服务状态
docker-compose ps

# 3. 查看日志
docker-compose logs -f

# 4. 停止服务
docker-compose down

# 5. 重启单个服务（如后端代码更新后）
docker-compose build backend
docker-compose up -d backend
```

启动后访问：
- 管理后台：http://localhost:8081
- 后端 API：http://localhost:8080

### 方式二：本地开发环境

#### 前置条件

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

#### 数据库初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 执行初始化脚本
source backend/src/main/resources/schema.sql
```

#### 启动后端

```bash
cd backend

# 修改 application.yml 中的数据库配置

# 启动
mvn spring-boot:run
```

#### 启动前端

```bash
cd frontend-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 方式三：混合开发模式（推荐用于前端开发）

使用 Docker 运行后端和数据库，本地运行前端开发服务器：

```bash
# 1. 启动后端和数据库
docker-compose up -d mysql backend

# 2. 本地启动前端（支持热更新）
cd frontend-admin
npm install
npm run dev
```

访问 http://localhost:5173 即可，前端会自动代理 API 请求到后端。

## 运行测试

### 后端测试

```bash
cd backend

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=CardServiceTest

# 生成测试报告
mvn test jacoco:report
```

### 前端测试

```bash
cd frontend-admin

# 运行测试
npm run test

# 运行单次测试
npm run test:run

# 生成覆盖率报告
npm run test:coverage
```

## Services

| 服务 | 地址 | 说明 |
|------|------|------|
| 管理后台 | http://localhost:8081 | Vue 3 + Element Plus 管理界面 |
| 后端API | http://localhost:8080 | Spring Boot REST API |
| MySQL | localhost:3306 | 数据库服务 |

## 测试账号

| 账号 | 密码 | 角色 | 权限说明 |
|------|------|------|----------|
| admin | admin123 | 管理员 | 全部功能权限 |
| operator | 123456 | 操作员 | 发卡、核销、查询权限（无用户管理和回收权限） |

## 题目内容

### 项目名称
游戏卡密管理系统

### 技术栈
- **后端**：Java 17 + Spring Boot 2.7 + MyBatis-Plus + MySQL 8.0
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Axios
- **部署**：Docker + Docker Compose

### 设计决策说明

本项目在实现过程中，针对 Prompt 中存在的歧义或冲突点，做出了以下工程决策：

#### 1. 前端技术栈版本（良性偏差）

| 要求 | 实现 | 说明 |
|------|------|------|
| Vue.js + ElementUI | Vue 3 + Element Plus | 技术升级 |

- **原因**：Vue 3 是 Vue.js 的最新主版本，Element Plus 是 ElementUI 适配 Vue 3 的官方升级版本
- **优势**：更好的性能、Composition API、TypeScript 支持、更活跃的社区维护
- **兼容性**：两者 API 设计高度一致，属于合理的技术升级而非技术栈变更

#### 2. 卡号生成算法（必要的修正）

| 要求 | 实现 | 说明 |
|------|------|------|
| 建议使用雪花算法 | 随机数 + 数据库查重 | 需求冲突的必要妥协 |

- **冲突点**：Prompt 同时要求"9位数字卡号"和"建议使用雪花算法"
- **问题**：雪花算法生成 64 位长整型（约 19 位数字），物理上无法满足 9 位要求
- **决策**：优先满足业务需求（9 位数字，用户友好），采用随机生成 + 数据库唯一性校验
- **性能**：在 10000 张卡的规模下，随机碰撞概率极低，性能完全满足要求

#### 3. 卡密回收逻辑（语义澄清）

| 要求 | 实现 | 说明 |
|------|------|------|
| "逻辑删除"该批次 | 状态更新为"已回收" | 满足显示需求 |

- **冲突点**：Prompt 要求"逻辑删除"但又要求列表能显示"已回收"状态
- **问题**：如果使用 `is_deleted=1`（@TableLogic），数据会从查询中消失，无法筛选已回收卡密
- **决策**：将"逻辑删除"理解为"作废但保留记录"，仅更新 `status=2`（已回收）
- **效果**：
  - ✅ 已回收卡密可通过状态筛选查询
  - ✅ 已回收卡密不可再被核销
  - ✅ 符合用户"作废但保留记录以供审计"的实际意图

### 功能需求

#### 1. 权限控制
- 除"卡密查询"功能外，其他所有功能均需登录后才能访问
- 登录采用账号密码方式（JWT Token认证）

#### 2. 发卡管理模块
- **一键发卡**：选择发卡数量，系统自动生成卡密
  - 卡号：9位唯一数字
  - 密码：6位随机字符（数字+大写字母）
  - 批次号：YYYYMMDDHHmmss 格式
- **查询卡密**：按批次号、卡号、状态查询，支持导出Excel
- **回收卡密**：按批次回收或单独回收
  - 回收时仅修改状态为"已回收"（status=2）
  - 已回收的卡密仍可在管理界面通过状态筛选查询
  - 已回收的卡密不可再被核销

#### 3. 核销管理模块
- **卡密核销**：输入卡号和密码进行核销
- **核销查询**：查询核销记录，支持日期范围筛选

#### 4. 卡密查询（公开功能）
- 无需登录即可访问
- 输入卡号和密码查询卡密状态

#### 5. 用户管理模块
- 用户增删改查
- 角色区分：管理员/操作员
- 不同角色权限控制

### 数据库设计

```
├── sys_user        # 系统用户表
├── card_batch      # 卡密批次表
├── card_info       # 卡密信息表
└── operation_log   # 操作日志表
```

### 非功能性需求
- 支持一次性生成10000张卡密
- 密码BCrypt加密存储
- 关键操作记录操作日志
- 全局异常处理
- 接口参数校验
- XSS 攻击防护
- 公开接口频率限制（防止滥用）
- 完整的单元测试和集成测试覆盖

---

## 项目结构

```
.
├── backend/                    # 后端项目
│   ├── src/main/java/com/cardmanager/
│   │   ├── annotation/         # 自定义注解
│   │   ├── aspect/             # AOP切面
│   │   ├── common/             # 公共类
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── exception/          # 异常处理
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── security/           # 安全相关
│   │   ├── service/            # 服务层
│   │   ├── util/               # 工具类
│   │   └── vo/                 # 视图对象
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   ├── logback-spring.xml  # 日志配置
│   │   └── schema.sql          # 数据库脚本
│   ├── src/test/               # 测试代码
│   │   ├── java/               # 单元测试和集成测试
│   │   └── resources/          # 测试配置
│   ├── Dockerfile
│   └── pom.xml
├── frontend-admin/             # 管理后台前端
│   ├── src/
│   │   ├── api/                # API 接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由
│   │   ├── store/              # 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docs/                       # 文档
│   └── project_design.md       # 项目设计文档
├── docker-compose.yml          # Docker 编排文件
├── .gitignore
└── README.md
```

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取当前用户信息

### 发卡管理接口
- `POST /api/card/generate` - 一键发卡
- `GET /api/card/list` - 查询卡密列表
- `GET /api/card/export` - 导出卡密Excel
- `PUT /api/card/recycle/batch` - 按批次回收
- `PUT /api/card/recycle/single` - 单独回收
- `GET /api/card/batch/list` - 查询批次列表

### 核销管理接口
- `POST /api/verify/use` - 核销卡密
- `GET /api/verify/history` - 核销记录查询
- `GET /api/verify/export` - 导出核销记录Excel

### 公开查询接口
- `GET /api/public/query` - 卡密状态查询

### 用户管理接口
- `GET /api/user/list` - 查询用户列表
- `POST /api/user/add` - 添加用户
- `PUT /api/user/update` - 修改用户
- `DELETE /api/user/delete/{id}` - 删除用户

### 统计接口
- `GET /api/statistics/dashboard` - 获取仪表盘数据

## License

MIT License
