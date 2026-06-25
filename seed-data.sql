-- ============================================
-- 种子数据：先注册一个招聘官，再发布示例岗位
-- 运行方式：psql -U postgres -d recruitment -f seed-data.sql
-- ============================================

-- 1. 创建招聘官账号（密码: admin123，BCrypt 加密）
INSERT INTO tb_user (username, password, role, email, phone, created_at)
VALUES ('张经理', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjg/n3.YV3xfNTOGdVXbXJ.W3u', 'RECRUITER', 'zhang@company.com', '13800138001', NOW())
ON CONFLICT DO NOTHING;

-- 2. 创建求职者账号（密码: user123）
INSERT INTO tb_user (username, password, role, email, phone, created_at)
VALUES ('李明', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjg/n3.YV3xfNTOGdVXbXJ.W3u', 'JOB_SEEKER', 'liming@test.com', '13900139001', NOW())
ON CONFLICT DO NOTHING;

-- 3. 示例岗位（recruiter_id 对应上面创建的招聘官，通常是 1）
-- 如果 recruiter_id 不是 1，请先查询 SELECT id, username FROM tb_user;

INSERT INTO tb_job (recruiter_id, title, description, requirements, salary_range, location, job_type, status, created_at)
VALUES
(
    1,
    'Java 后端开发工程师',
    '负责公司核心业务系统的后端设计与开发，参与微服务架构演进，保障系统高可用和高并发性能。\n\n岗位职责：\n1. 负责后端服务的设计、开发与维护\n2. 参与系统架构设计和技术选型\n3. 编写单元测试和集成测试，保障代码质量\n4. 参与 Code Review 和技术分享',
    '1. 本科及以上学历，计算机相关专业，3年以上 Java 开发经验\n2. 扎实的 Java 基础，熟悉 JVM 原理、多线程、集合框架\n3. 熟练使用 Spring Boot、Spring Cloud、MyBatis 等主流框架\n4. 熟悉 MySQL、Redis、RabbitMQ 等中间件\n5. 有微服务架构设计和实战经验\n6. 良好的沟通能力和团队协作精神\n7. 有高并发系统设计经验优先',
    '15K-30K',
    '北京 · 朝阳区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    '前端开发工程师（React）',
    '负责公司产品的前端开发，包括 Web 端和移动端 H5 页面的开发与优化。\n\n岗位职责：\n1. 负责产品前端页面开发和维护\n2. 参与前端技术选型和架构设计\n3. 优化前端性能和用户体验\n4. 封装通用组件，建设前端工程化体系',
    '1. 本科及以上学历，2年以上前端开发经验\n2. 熟练掌握 HTML5、CSS3、JavaScript/TypeScript\n3. 精通 React 框架，熟悉 Hooks、状态管理（Zustand/Redux）\n4. 熟悉前端工程化（Vite/Webpack、ESLint、Prettier）\n5. 有移动端适配和性能优化经验\n6. 了解 Node.js 服务端开发优先\n7. 有开源项目或技术博客优先',
    '12K-25K',
    '北京 · 海淀区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    'Python 数据分析师',
    '负责业务数据的采集、清洗、分析和可视化，为产品决策和运营策略提供数据支持。\n\n岗位职责：\n1. 负责业务数据的提取、清洗和分析\n2. 搭建数据看板和自动化报表\n3. 通过数据分析发现业务问题并提出优化建议\n4. 与产品和运营团队紧密协作',
    '1. 本科及以上学历，统计学、数学、计算机相关专业\n2. 熟练使用 Python（Pandas、NumPy、Matplotlib）\n3. 熟悉 SQL，能独立编写复杂查询\n4. 了解常用的数据分析方法和统计学知识\n5. 有 Tableau/Power BI 等可视化工具使用经验\n6. 有电商或互联网行业数据分析经验优先',
    '12K-22K',
    '上海 · 浦东新区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    '产品经理（B端）',
    '负责企业级 SaaS 产品的需求分析、功能规划和迭代管理，推动产品从 0 到 1 的增长。\n\n岗位职责：\n1. 负责产品需求调研和分析，输出 PRD 文档\n2. 制定产品路线图和迭代计划\n3. 协调设计、研发、测试团队推进产品落地\n4. 跟踪产品数据，持续优化产品体验',
    '1. 本科及以上学历，3年以上 B 端产品经验\n2. 有 SaaS 产品或企业服务产品经验\n3. 熟练使用 Axure/Figma/XMind 等产品工具\n4. 具备较强的逻辑思维和数据分析能力\n5. 优秀的沟通协调和项目管理能力\n6. 有技术背景或了解软件开发流程优先',
    '18K-35K',
    '深圳 · 南山区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    'DevOps 运维工程师',
    '负责公司线上服务的稳定性保障，建设和维护 CI/CD 流水线，推动基础设施即代码的落地。\n\n岗位职责：\n1. 负责生产环境的日常运维和故障处理\n2. 搭建和维护 CI/CD 流水线\n3. 管理和优化 Kubernetes 集群\n4. 建设监控告警体系，保障服务 SLA\n5. 推动容器化和自动化运维',
    '1. 本科及以上学历，3年以上运维或 DevOps 经验\n2. 精通 Linux 系统管理和 Shell 脚本\n3. 熟练使用 Docker 和 Kubernetes\n4. 熟悉 Jenkins/GitLab CI/GitHub Actions 等 CI/CD 工具\n5. 了解 Prometheus + Grafana 监控体系\n6. 至少熟悉一种云平台（AWS/阿里云/腾讯云）\n7. 有 Terraform/Ansible 等 IaC 工具使用经验优先',
    '18K-30K',
    '杭州 · 余杭区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    '测试开发工程师',
    '负责产品质量保障，建设自动化测试框架和效能工具，推动质量左移。\n\n岗位职责：\n1. 设计和编写自动化测试用例\n2. 搭建和维护自动化测试框架\n3. 参与需求评审，从测试角度提出建议\n4. 开发测试效能工具，提升测试效率\n5. 跟踪和分析线上质量问题',
    '1. 本科及以上学历，2年以上测试开发经验\n2. 精通至少一种编程语言（Java/Python/Go）\n3. 熟悉 Selenium/Appium/JUnit 等测试框架\n4. 有接口自动化测试和性能测试经验\n5. 了解 CI/CD 流程，能把测试集成到流水线\n6. 细心、责任心强，有良好的 Bug 敏感度',
    '14K-28K',
    '北京 · 朝阳区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    'Android 开发工程师',
    '负责公司 Android 客户端的设计、开发和维护，持续优化用户体验和应用性能。\n\n岗位职责：\n1. 负责 Android 客户端功能开发和维护\n2. 参与客户端架构设计与优化\n3. 优化应用性能，提升启动速度和流畅度\n4. 跟进 Android 新版本特性，推动技术升级',
    '1. 本科及以上学历，3年以上 Android 开发经验\n2. 精通 Java 和 Kotlin，熟悉 Android SDK\n3. 熟悉 MVVM/MVI 架构模式，熟练使用 Jetpack 组件\n4. 有性能优化经验（启动优化、内存优化、卡顿治理）\n5. 了解跨平台方案（Flutter/React Native）优先\n6. 有上架应用或开源作品优先',
    '15K-28K',
    '深圳 · 南山区',
    '全职',
    'OPEN',
    NOW()
),
(
    1,
    'UI/UX 设计师',
    '负责公司产品的界面设计和用户体验优化，制定设计规范，推动设计系统的建设。\n\n岗位职责：\n1. 负责产品的 UI 界面设计和交互原型\n2. 制定和维护设计规范和组件库\n3. 参与用户研究，根据反馈优化设计方案\n4. 与产品、开发团队紧密协作，确保设计落地',
    '1. 本科及以上学历，设计相关专业\n2. 2年以上 UI/UX 设计经验，有完整项目案例\n3. 精通 Figma/Sketch/Adobe XD 等设计工具\n4. 了解前端基础知识（HTML/CSS），能与开发高效沟通\n5. 有 B 端产品设计经验优先\n6. 请附带作品集',
    '12K-22K',
    '上海 · 静安区',
    '全职',
    'OPEN',
    NOW()
);

-- 验证
SELECT count(*) AS 岗位总数 FROM tb_job;
SELECT id, title, location, salary_range FROM tb_job ORDER BY id;
