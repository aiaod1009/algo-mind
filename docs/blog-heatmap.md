# 做题热力图：从数据库到前端的完整实现

> 类似 GitHub Contributions Graph 的做题活跃度可视化，记录用户每天的刷题情况，用颜色深浅展示活跃程度。

---

## 一、数据库设计

核心就一张表 `t_user_problem_record`，记录用户的每一次做题行为。

### 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT, 自增主键 | 记录 ID |
| `user_id` | BIGINT, NOT NULL | 用户 ID |
| `level_id` | BIGINT, NOT NULL | 题目 ID |
| `level_name` | VARCHAR(200) | 题目名称（冗余存储，避免频繁 JOIN） |
| `track` | VARCHAR(20) | 赛道：`algo` / `ds` / `contest` |
| `is_correct` | BOOLEAN, NOT NULL | 本次是否答对 |
| `status` | VARCHAR(20) | 状态标记 |
| `stars` | INT, NOT NULL | 获得星数（0-3） |
| `attempt_no` | INT, NOT NULL | 第几次尝试 |
| `solve_time_ms` | INT | 解题耗时（毫秒） |
| `solved_at` | DATETIME, NOT NULL | 做题时间（热力图的核心字段） |
| `created_at` | DATETIME, NOT NULL | 记录创建时间 |

### 索引

```sql
CREATE INDEX idx_user_solved_at ON t_user_problem_record (user_id, solved_at);
CREATE INDEX idx_user_level     ON t_user_problem_record (user_id, level_id);
```

- `idx_user_solved_at`：热力图查询的核心索引，按用户 + 时间范围筛选
- `idx_user_level`：查询某用户是否做过某题

### 热力图聚合查询

```sql
SELECT DATE_FORMAT(solved_at, '%Y-%m-%d') AS day, COUNT(*) AS cnt
FROM t_user_problem_record
WHERE user_id = ?
  AND solved_at >= '2026-01-01'
  AND solved_at < '2027-01-01'
GROUP BY DATE_FORMAT(solved_at, '%Y-%m-%d')
ORDER BY day
```

返回结果示例：

```
day          | cnt
-------------+----
2026-01-05   | 3
2026-01-06   | 1
2026-02-14   | 5
...
```

前端拿到的就是一组 `{ date, count }`，再根据 count 映射到颜色等级。

### count → 等级映射

```javascript
function countToLevel(count) {
  if (count <= 0) return 0  // 无记录
  if (count <= 2) return 1  // 轻度活跃
  if (count <= 4) return 2  // 中度活跃
  if (count <= 6) return 3  // 高度活跃
  return 4                  // 爆发日
}
```

---

## 二、前端实现

### 整体思路

1. 调接口拿数据
2. 把数据转成 `日期 → 记录` 的 Map
3. 生成 7行 × N列 的网格（每列 = 一周，每行 = 周一到周日）
4. 每个格子根据 level 染色
5. hover 时显示 tooltip

### 数据获取

```javascript
const response = await api.get(`/users/me/heatmap?year=${year}`)
const records = response.data.data.records

// 转成 Map，方便按日期查找
const contributionMap = new Map()
records.forEach(item => {
  contributionMap.set(item.date, { date: item.date, count: item.count, level: item.level })
})
```

### 网格生成算法

GitHub 风格热力图的布局规则：
- 每列代表一周
- 每行代表星期几（周一到周日）
- 年初补齐空白，年末补齐空白

```javascript
function generateHeatmapGrid(year, contributionMap) {
  const firstDay = new Date(year, 0, 1)
  const lastDay = new Date(year, 11, 31)

  // JS 的 getDay()：周日=0, 周一=1 ... 周六=6
  // 转换成：周一=0, 周二=1 ... 周日=6
  const firstWeekday = (firstDay.getDay() + 6) % 7

  const cells = []

  // 1. 补齐年初空白（1月1日之前的格子填 null）
  for (let i = 0; i < firstWeekday; i++) {
    cells.push(null)
  }

  // 2. 填充全年 365 天
  const current = new Date(year, 0, 1)
  while (current <= lastDay) {
    const dateStr = formatDate(current)  // "2026-01-05"
    cells.push(
      contributionMap.get(dateStr) || { date: dateStr, count: 0, level: 0 }
    )
    current.setDate(current.getDate() + 1)
  }

  // 3. 补齐年末空白，凑满整周（7 的倍数）
  while (cells.length % 7 !== 0) {
    cells.push(null)
  }

  // 4. 每 7 个一组切成列（每周一列）
  const weeks = []
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7))
  }

  return weeks  // weeks[weekIndex][dayIndex] = { date, count, level } 或 null
}
```

生成后的数据结构：

```
weeks = [
  [ null, null, null, {date:"2026-01-01", count:2, level:1}, ... ],  // 第1周
  [ {date:"2026-01-05", count:0, level:0}, ... ],                     // 第2周
  ...
]
```

### 月份标签定位

根据每周第一个有效日期判断属于哪个月份，记录首次出现的周索引：

```javascript
function getMonthPositions(weeks, year) {
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
  const positions = []
  let lastMonth = -1

  weeks.forEach((week, weekIndex) => {
    const firstValid = week.find(cell => cell !== null)
    if (!firstValid) return

    const month = Number(firstValid.date.split('-')[1]) - 1
    if (month !== lastMonth) {
      lastMonth = month
      positions.push({ month: months[month], weekIndex })
    }
  })

  return positions
}
```

### HTML 结构

```html
<div class="contributions-calendar">
  <!-- 月份标签 -->
  <div class="months-row">
    <div v-for="pos in monthPositions" class="month-label"
      :style="{ left: `${pos.weekIndex * 13 + 28}px` }">
      {{ pos.month }}
    </div>
  </div>

  <div class="calendar-body">
    <!-- 星期标签 -->
    <div class="weekdays-col">
      <div v-for="day in ['Mon','','Wed','','Fri','','']" class="weekday-label">
        {{ day }}
      </div>
    </div>

    <!-- 热力图网格 -->
    <div class="contributions-grid">
      <div v-for="week in heatmapGrid" class="week-column">
        <div v-for="cell in week" class="contribution-cell"
          :class="cell ? `level-${cell.level}` : 'is-empty'"
          @mouseenter="showTooltip($event, cell)"
          @mouseleave="hideTooltip">
        </div>
      </div>
    </div>
  </div>
</div>
```

### CSS 染色

5 个等级对应 5 种蓝色深浅：

```css
.contributions-section {
  --heatmap-level-0: #e8eef7;  /* 最浅 - 无记录 */
  --heatmap-level-1: #d3e0f6;  /* 浅蓝 */
  --heatmap-level-2: #b7cdf1;  /* 中蓝 */
  --heatmap-level-3: #7ea8eb;  /* 深蓝 */
  --heatmap-level-4: #2f6fe0;  /* 最深 - 爆发日 */
}

.contribution-cell {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background: var(--heatmap-level-0);
  cursor: pointer;
}

.contribution-cell.level-1 { background: var(--heatmap-level-1); }
.contribution-cell.level-2 { background: var(--heatmap-level-2); }
.contribution-cell.level-3 { background: var(--heatmap-level-3); }
.contribution-cell.level-4 { background: var(--heatmap-level-4); }

.contribution-cell.is-empty {
  background: transparent;
  cursor: default;
}
```

### Tooltip 实现

用 `Teleport` 把 tooltip 挂到 `body` 上，避免被父容器的 `overflow: hidden` 裁剪：

```html
<Teleport to="body">
  <div v-if="tooltip.show" class="heatmap-tooltip"
    :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
    <div class="tooltip-date">{{ tooltip.date }}</div>
    <div class="tooltip-content">
      <span>做题 {{ tooltip.count }} 道</span>
      <span :class="'level-' + tooltip.level">{{ levelText }}</span>
    </div>
  </div>
</Teleport>
```

定位逻辑：跟随鼠标，自动避让屏幕边缘。

---

## 三、数据流总结

```
用户做题 → 写入 t_user_problem_record
                ↓
    GET /api/users/me/heatmap?year=2026
                ↓
    SQL: GROUP BY DATE(solved_at) → [{ date, count }]
                ↓
    前端: count → level → 染色
                ↓
    生成 7×N 网格 → 渲染
```

整条链路很清晰：**写入一次做题记录 → SQL 按天聚合 → 前端映射颜色 → 渲染网格**。

---

## 四、踩过的坑

1. **周起始日**：JS 的 `getDay()` 周日是 0，但热力图周一是第一行，需要 `(getDay() + 6) % 7` 转换。

2. **年末补齐**：如果最后一天不是周日，右侧会出现不完整的列，需要补 null 到 7 的倍数。

3. **性能**：一年 365 条记录，直接全量返回即可，不需要分页。聚合查询走 `idx_user_solved_at` 索引，很快。

4. **Tooltip 层级**：热力图如果在 `overflow: hidden` 的容器里，tooltip 会被裁剪，所以用 `Teleport` 挂到 body。
