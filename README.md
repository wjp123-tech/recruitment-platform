# AI 智能招聘平台

> 基于 Spring Boot + Spring AI + PostgreSQL pgvector + React 的全栈智能招聘系统

[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue)](https://spring.io/projects/spring-ai)
[![React](https://img.shields.io/badge/React-18-61DAFB)](https://react.dev/)

---

## 项目简介

面向求职者和招聘官的双端智能招聘平台，核心亮点是 **AI 模拟面试** 和 **AI 笔试模拟**。

- **求职者**：浏览岗位、上传简历、AI 岗位推荐、AI 模拟面试、AI 笔试、与 HR 在线聊天
- **招聘官**：发布管理岗位、查看候选人投递和简历、筛选状态、在线回复消息

---

## 核心功能

### AI 模拟面试

选岗位和简历 → AI 扮演面试官多轮对话 → 自动生成四维评估报告

- **Tool Calling**：AI 面试官可主动调用 `lookupResume` / `getCandidateInfo` 查阅简历细节
- **流式输出**：SSE（Server-Sent Events）+ fetch ReadableStream 逐块渲染
- **对话记忆**：MessageChatMemory 滑动窗口 + PostgreSQL 全量持久化
- **Fallback 兜底**：评估失败自动降级，不阻断主流程

### AI 笔试模拟

- AI 按岗位自动出题：单选 / 多选 / 简答 / 编程
- 倒计时作答 + 时间到自动交卷
- 选择题自动判分 + LLM 批改主观题
- 前后端双重校验防止作弊

### AI 岗位推荐

- 求职者设置求职意向（期望岗位 / 城市 / 薪资）
- pgvector 向量语义匹配 + `ts_rank()` 相关性排序
- 三级 fallback：向量检索 → 全文检索 → 最新岗位

### 其他功能

- PostgreSQL 全文检索（GIN 索引替代 ILIKE 全表扫描）
- JWT 认证 + 角色权限控制（求职者 / 招聘官）
- Apache Tika 简历解析（PDF / DOCX / DOC / TXT）
- 在线聊天 + 站内信通知
- 投递管理 + 状态流转 + 简历查看

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.4.5 + Java 17 |
| AI 集成 | Spring AI 1.0.0（ChatClient / EmbeddingModel / Tool Calling） |
| 数据库 | PostgreSQL + pgvector（HNSW + COSINE_DISTANCE） |
| 前端 | React 18 + TypeScript + Tailwind CSS + Vite |
| 文档解析 | Apache Tika 2.9 |
| 认证 | JWT（jjwt 0.12） |
| 构建 | Maven |

---

## 项目结构

```
recruitment-platform/
├── pom.xml                              # Maven 配置
├── src/main/java/com/recruitment/
│   ├── App.java                         # 启动类
│   ├── common/
│   │   ├── ai/                          # LlmProviderRegistry / InterviewTools / ToolConfig
│   │   ├── config/                      # JWT / CORS / Filter
│   │   ├── exception/                   # 全局异常处理
│   │   └── result/                      # Result<T> 统一响应
│   ├── infrastructure/
│   │   ├── file/                        # Tika 文档解析 / 文件存储
│   │   └── db/                          # 全文索引 / 向量索引初始化
│   └── modules/
│       ├── user/                        # 用户 + 求职意向
│       ├── job/                         # 岗位 CRUD + AI 推荐 + 全文检索
│       ├── resume/                      # 简历上传 / 解析
│       ├── delivery/                    # 投递 + 状态流转 + 简历查看
│       ├── chat/                        # 在线聊天（HTTP 轮询）
│       ├── notification/                # 站内信通知
│       ├── interview/                   # AI 模拟面试（Tool Calling + SSE）
│       └── exam/                        # AI 笔试（出题 + 倒计时 + 批改）
├── src/main/resources/
│   ├── application.yml                  # 主配置
│   └── prompts/                         # Prompt 模板
├── frontend/                            # React 前端
│   └── src/
│       ├── api/                         # API 层
│       ├── components/                  # Layout / Navbar / AuthGuard
│       └── pages/                       # auth / jobs / resume / delivery / chat / interview / exam / notification
├── seed-data.sql                        # 种子数据（8 个岗位 + 2 个账号）
└── 面试准备-合订本.md                    # 面试准备文档
```

---

## 快速开始

### 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| PostgreSQL | 14+ | 数据库（需安装 pgvector 扩展） |
| Node.js | 18+ | 前端运行环境 |
| pnpm | 8+ | 前端包管理 |

### 1. 数据库准备

```sql
CREATE DATABASE recruitment;
\c recruitment
CREATE EXTENSION vector;
```

### 2. 配置 API Key

在 IDEA Run Configuration 或系统环境变量中设置：

```
DASHSCOPE_API_KEY=你的阿里云百炼API密钥
```

> 默认使用通义千问（DashScope），修改 `application.yml` 可切换 DeepSeek / Kimi / GLM。

### 3. 启动后端

```bash
# IDEA 直接运行 App.java
# 或命令行
mvn spring-boot:run
```

启动后访问 http://localhost:8080

### 4. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

访问 http://localhost:5173

### 5. 导入种子数据（可选）

```bash
psql -U postgres -d recruitment -f seed-data.sql
```

预置账号（密码均为 `admin123`）：

| 角色 | 用户名 |
|------|--------|
| 招聘官 | 张经理 |
| 求职者 | 李明 |

---

## API 概览

| 模块 | 端点 | 说明 |
|------|------|------|
| 用户 | `POST /api/user/register` | 注册 |
| 用户 | `POST /api/user/login` | 登录 |
| 用户 | `PUT /api/user/preferences` | 设置求职意向 |
| 岗位 | `GET /api/jobs?keyword=` | 全文检索岗位 |
| 岗位 | `GET /api/jobs/recommend` | AI 推荐岗位 |
| 简历 | `POST /api/resume/upload` | 上传简历 |
| 投递 | `POST /api/delivery/apply` | 投递简历 |
| 投递 | `GET /api/delivery/job/{jobId}` | 查看投递列表 |
| 投递 | `GET /api/delivery/{id}/resume` | 查看候选人简历 |
| 聊天 | `POST /api/chat/send` | 发送消息 |
| 聊天 | `GET /api/chat/history/{userId}` | 获取聊天记录 |
| 面试 | `POST /api/interview/sessions` | 创建面试会话 |
| 面试 | `POST /api/interview/sessions/{id}/answer` | 提交回答（SSE 流式） |
| 面试 | `GET /api/interview/sessions/{id}/report` | 查看评估报告 |
| 笔试 | `POST /api/exam/generate` | 生成试卷 |
| 笔试 | `GET /api/exam/{id}/remaining` | 剩余时间 |
| 笔试 | `POST /api/exam/{id}/submit` | 提交答卷 |
| 通知 | `GET /api/notification/unread-count` | 未读通知数 |

---

## 项目亮点（面试向）

- **Tool Calling 实践**：`FunctionToolCallback` 编程式注册工具，AI 面试官主动查阅简历细节
- **SSE 流式输出**：`Flux<ServerSentEvent>` + `fetch ReadableStream` 手动 SSE 解析
- **全文检索演进**：ILIKE 全表扫描 → `to_tsvector` + GIN 索引 + `ts_rank()` 排序
- **三级 fallback 模式**：向量检索 → 全文检索 → 兜底返回，AI 是加分项不是阻断项
- **多 ChatClient 变体**：标准版（Memory / SafeGuard）/ Plain 版（结构化输出）/ Interview 版（Tool Calling）
- **前后端双重倒计时校验**：防止前端时间被篡改



## License



