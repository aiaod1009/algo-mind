-- phpMyAdmin SQL Dump
-- version 5.0.4
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2026-04-21 22:43:08
-- 服务器版本： 10.5.27-MariaDB-log
-- PHP 版本： 8.1.32

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `algomind_db`
--

-- --------------------------------------------------------

--
-- 表的结构 `course_tags`
--

CREATE TABLE `course_tags` (
  `course_id` bigint(20) NOT NULL,
  `tags` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `level_options`
--

CREATE TABLE `level_options` (
  `level_id` bigint(20) NOT NULL,
  `options` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `t_ai_analysis_usage`
--

CREATE TABLE `t_ai_analysis_usage` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `error_id` bigint(20) DEFAULT NULL,
  `quota_date` date NOT NULL,
  `slot_code` varchar(16) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_ai_analysis_usage`
--

INSERT INTO `t_ai_analysis_usage` (`id`, `created_at`, `error_id`, `quota_date`, `slot_code`, `user_id`) VALUES
(1, '2026-04-14 11:31:10.000000', 12, '2026-04-14', 'pm', 1),
(4, '2026-04-14 19:42:03.000000', 12, '2026-04-15', 'am', 1),
(5, '2026-04-15 06:05:29.000000', 15, '2026-04-15', 'pm', 10),
(7, '2026-04-17 08:06:10.000000', 12, '2026-04-17', 'pm', 1),
(8, '2026-04-19 18:17:08.000000', NULL, '2026-04-19', 'pm', 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_code_snapshot`
--

CREATE TABLE `t_code_snapshot` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `level_id` bigint(20) NOT NULL COMMENT '关卡ID',
  `level_name` varchar(200) DEFAULT NULL COMMENT '关卡名称快照',
  `language` varchar(30) NOT NULL COMMENT '编程语言',
  `code` longtext NOT NULL COMMENT '源代码',
  `stdin_input` longtext DEFAULT NULL,
  `score` int(11) NOT NULL DEFAULT 0 COMMENT 'AI评测分数(0-100)',
  `stars` int(11) NOT NULL DEFAULT 0 COMMENT '星级(0-3)',
  `compile_passed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '编译是否通过',
  `ai_eval_passed` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'AI评测是否通过(>=70分)',
  `ai_analysis` longtext DEFAULT NULL COMMENT 'AI评测分析摘要',
  `ai_correctness` varchar(500) DEFAULT NULL COMMENT '正确性评价',
  `ai_quality` varchar(500) DEFAULT NULL COMMENT '代码质量评价',
  `ai_efficiency` varchar(500) DEFAULT NULL COMMENT '效率评价',
  `ai_suggestions_json` longtext DEFAULT NULL,
  `run_output` longtext DEFAULT NULL,
  `is_best` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为该题最佳版本',
  `saved_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '保存时间',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `ai_recommended_code` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='历史代码表';

--
-- 转存表中的数据 `t_code_snapshot`
--

INSERT INTO `t_code_snapshot` (`id`, `user_id`, `level_id`, `level_name`, `language`, `code`, `stdin_input`, `score`, `stars`, `compile_passed`, `ai_eval_passed`, `ai_analysis`, `ai_correctness`, `ai_quality`, `ai_efficiency`, `ai_suggestions_json`, `run_output`, `is_best`, `saved_at`, `created_at`, `ai_recommended_code`) VALUES
(3, 9, 10, '??????', 'Python', 'def binary_search(nums, target):\n    left, right = 0, len(nums)-1\n    while left <= right:\n        mid = left + (right-left)//2\n        if nums[mid]==target: return mid\n        elif nums[mid]<target: left=mid+1\n        else: right=mid-1\n    return -1', NULL, 95, 3, 1, 1, '????,??????', '????', '????', 'O(log n)', NULL, NULL, 1, '2026-04-13 20:51:41', '2026-04-13 20:51:41', NULL),
(6, 9, 10, '????', 'Python', 'def bs(a,t): pass', NULL, 95, 3, 1, 1, '??', '??', '??', 'O(logn)', NULL, NULL, 0, '2026-04-13 20:55:23', '2026-04-13 20:55:23', NULL),
(9, 9, 20, '????', 'Python', 'def bubble_sort(arr):\n    pass', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2026-04-13 21:12:52', '2026-04-13 21:12:52', NULL),
(10, 9, 20, '????', 'Python', 'def bubble_sort(arr):\n    n=len(arr)\n    for i in range(n):\n        pass', NULL, 0, 0, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2026-04-13 21:12:52', '2026-04-13 21:12:52', NULL),
(11, 9, 20, '????', 'Python', 'def bubble_sort(arr):\n    n=len(arr)\n    for i in range(n):\n        for j in range(0,n-i-1):\n            if arr[j]>arr[j+1]: arr[j],arr[j+1]=arr[j+1],arr[j]\n    return arr', NULL, 80, 2, 1, 1, '????', '??', '??', 'O(n^2)', NULL, NULL, 1, '2026-04-13 21:12:52', '2026-04-13 21:12:52', NULL),
(13, 1, 10, '实现二分查找', 'Java', 'import java.util.*;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner scanner = new Scanner(System.in);\n    // 在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。\n  }\n}\n', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2026-04-14 05:46:25', '2026-04-14 05:46:25', NULL),
(14, 1, 10, '实现二分查找', 'Python', 'def search(nums: list[int], target: int) -> int:\n    # 定义左右指针，采用 [left, right] 闭区间写法（最常用、最易理解）\n    left = 0\n    right = len(nums) - 1  # 右指针初始指向数组最后一个元素\n\n    # 当 left <= right 时，区间 [left, right] 有效，继续查找\n    while left <= right:\n        # 计算中间位置，用 // 做整数除法，避免溢出（Python 无溢出问题，写法通用）\n        mid = left + (right - left) // 2\n        \n        if nums[mid] == target:\n            # 找到目标值，直接返回下标\n            return mid\n        elif nums[mid] < target:\n            # 中间值小于目标，说明目标在右半区间，左指针右移\n            left = mid + 1\n        else:\n            # 中间值大于目标，说明目标在左半区间，右指针左移\n            right = mid - 1\n\n    # 循环结束仍未找到，返回 -1\n    return -1', NULL, 60, 1, 0, 0, 'AI 返回的评测结果格式不正确，已使用默认评测。原始响应：这段代码是实现有序数组中查找目标值的标准二分查找算法，采用[left, right]闭区间的经典写法，逻辑严谨且完全符合题目要求。\n\n从正确性来看，代码覆盖了所有关键边界场景：包括空数组（此时right初始值为-1，循环直接跳过返回-1）、目标值位于数组首尾位置、目标值不存在于数组中、数组仅含单个元素等情况，均能正确返回结果，完全满足题目要求。\n\n代码质量方面，结构清晰，变量命名（left、right、mid）遵循行业通用惯例，注释详细且精准，不仅解释了每一步的逻辑，还说明了闭区间写法的优势，使得代码的可读性和可维护性都非常出色。\n\n算法效率上，时间复杂度为O(log n)，严格符合题目对时间复杂度的要求；空间复杂度为O(1)，仅使用常数级额外空间，属于最优的二分查找实现。\n\n健壮性方面，代码能够处理各种异常或边界输入，比如目标值小于数组所有元素、大于数组所有元素、空输入等情况，均能正确返回-1，没有出现崩溃或错误结果的情况。\n\n表达规范上，代码缩进统一，注释风格清晰，每段逻辑都有对应的说明，完全符合Python的编码规范。\n\n整体而言，这是一份近乎完美的二分查找实现代码，无论是逻辑正确性还是代码质量都达到了很高的水准。\n\n---JSON---\n{\n  \"score\": 100,\n  \"stars\": 3,\n  \"output\": \"输入有序数组和目标值，若存在返回对应下标，不存在返回-1。例如：nums=[-1,0,3,5,9,12], target=9返回4；nums=[], target=0返回-1；nums=[5], target=5返回0；nums=[-1,0,3,5,9,12], target=2返回-1\",\n  \"analysis\": \"该代码是标准的闭区间二分查找实现，逻辑严谨，完全满足题目要求。优点包括：采用易理解的闭区间写法，注释详细清晰，覆盖所有边界场景，时间和空间复杂度均达到最优。整体代码质量极高，可作为二分查找的参考模板。\",\n  \"correctness\": \"完全正确，覆盖了所有关键边界条件，包括空数组、目标在首尾、目标不存在、单元素数组等场景，所有测试用例均能得到正确结果。\",\n  \"quality\": \"代码结构清晰，变量命名规范，注释详细且有针对性，可读性和可维护性极强，符合Python编码规范。\",\n  \"efficiency\": \"时间复杂度为O(log n)，满足题目要求；空间复杂度为O(1)，使用常数级额外空间，效率最优。\",\n  \"suggestions\": [\"可以添加针对空数组的显式判断语句，进一步提升代码的直观性\", \"可以在注释中对比闭区间与左闭右开写法的差异，增强代码的教学参考价值\", \"可以添加示例测试用例的代码注释，方便快速验证代码正确性\"]\n}', '需要重新评测以确认正确性', '结构化结果解析失败', '无法准确判断', '[\"稍后重试一次 AI 评测\",\"检查代码是否完整提交\"]', NULL, 0, '2026-04-14 06:13:22', '2026-04-14 06:13:22', NULL),
(15, 1, 10, '最短路径与回溯', 'Java', 'import java.util.*;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner scanner = new Scanner(System.in);\n    // 给定一个大小为 rows x cols 的二维高度矩阵 heights，你从左上角 (0, 0) 出发，目标是到达右下角 (rows-1, cols-1)。你每次可以上下左右移动。一条路径的“体力消耗”是该路径上相邻格子之间高度差绝对值的最大值。请实现一个函数，返回到达目标的最小体力消耗值，以及走过这条路径的完整坐标连线数组。\n  }\n}\n', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2026-04-14 15:50:35', '2026-04-14 15:50:35', NULL),
(16, 1, 10, '最短路径与回溯', 'Python', 'import heapq\n\ndef minimumEffort(heights):\n    r, c = len(heights), len(heights[0])\n    d = [[float(\'inf\')] * c for _ in range(r)]\n    parent = [[None] * c for _ in range(r)]\n    d[0][0] = 0\n    h = [(0, 0, 0)]\n    dirs = [(-1, 0), (1, 0), (0, -1), (0, 1)]\n\n    while h:\n        e, x, y = heapq.heappop(h)\n        if x == r - 1 and y == c - 1:\n            path = []\n            cx, cy = x, y\n            while (cx, cy) is not None:\n                path.append((cx, cy))\n                cx, cy = parent[cx][cy]\n            path.reverse()\n            return e, path\n        if e > d[x][y]:\n            continue\n        for dx, dy in dirs:\n            nx, ny = x + dx, y + dy\n            if 0 <= nx < r and 0 <= ny < c:\n                ne = max(e, abs(heights[x][y] - heights[nx][ny]))\n                if ne < d[nx][ny]:\n                    d[nx][ny] = ne\n                    parent[nx][ny] = (x, y)\n                    heapq.heappush(h, (ne, nx, ny))\n\nif __name__ == \"__main__\":\n    h = [[1,2,2],[3,8,2],[5,3,5]]\n    effort, path = minimumEffort(h)\n    print(effort)\n    print(path)', NULL, 95, 3, 0, 1, '这段代码基于Dijkstra算法正确实现了最小体力消耗路径求解，符合题目要求。通过最小堆优先扩展当前消耗最小的节点，用min_effort数组记录各节点最小消耗，parent数组回溯路径。处理了边界判断与重复节点剪枝，能正确返回最小消耗值和完整路径，测试用例输出正确。时间复杂度为O(MNlogMN)，是高效解法，结构清晰但可提升可读性。', NULL, NULL, NULL, '[\"增加关键逻辑注释，提升代码可维护性\",\"优化变量命名，增强语义直观性\"]', NULL, 1, '2026-04-14 19:38:25', '2026-04-14 19:38:25', 'importheapq\n\ndefminimumEffort(heights):\n  ifnotheightsornotheights[0]:\n      return0,[]\n  rows,cols=len(heights),len(heights[0])\n  #记录每个点的最小体力消耗值\n  min_effort=[[float(\'inf\')]*colsfor_inrange(rows)]\n  #记录每个点的父节点，用于回溯路径\n  parent=[[None]*colsfor_inrange(rows)]\n  start=(0,0)\n  end=(rows-1,cols-1)\n  min_effort[start[0]][start[1]]=0\n  #优先队列：(当前路径最大消耗,x坐标,y坐标)\n  heap=[(0,start[0],start[1])]\n  #上下左右四个移动方向\n  directions=[(-1,0),(1,0),(0,-1),(0,1)]\n\n  whileheap:\n      current_effort,x,y=heapq.heappop(heap)\n      #到达终点则回溯路径\n      if(x,y)==end:\n          path=[]\n          cx,cy=x,y\n          while(cx,cy)isnotNone:\n              path.append((cx,cy))\n              cx,cy=parent[cx][cy]\n          path.reverse()\n          returncurrent_effort,path\n      #已有更优路径，跳过当前节点\n      ifcurrent_effort>min_effort[x][y]:\n          continue\n      #遍历四个方向的相邻节点\n      fordx,dyindirections:\n          nx,ny=x+dx,y+dy\n          if0<=nx<rowsand0<=ny<cols:\n              #计算新路径的体力消耗\n              new_effort=max(current_effort,abs(heights[x][y]-heights[nx][ny]))\n              #发现更优路径则更新并加入堆\n              ifnew_effort<min_effort[nx][ny]:\n                  min_effort[nx][ny]=new_effort\n                  parent[nx][ny]=(x,y)\n                  heapq.heappush(heap,(new_effort,nx,ny))\n  #理论上不会执行到此处，确保函数有返回值\n  returnfloat(\'inf\'),[]\n\nif__name__==\"__main__\":\n  h=[[1,2,2],[3,8,2],[5,3,5]]\n  effort,path=minimumEffort(h)\n  print(\"最小体力消耗值：\",effort)\n  print(\"路径：\",path)'),
(17, 1, 10, '最短路径与回溯', 'Python', 'import heapq\n\ndef minimumEffort(heights):\n    r, c = len(heights), len(heights[0])\n    d = [[float(\'inf\')] * c for _ in range(r)]\n    parent = [[None] * c for _ in range(r)]\n    d[0][0] = 0\n    h = [(0, 0, 0)]\n    dirs = [(-1, 0), (1, 0), (0, -1), (0, 1)]\n\n    while h:\n        e, x, y = heapq.heappop(h)\n        if x == r - 1 and y == c - 1:\n            path = []\n            cx, cy = x, y\n            while (cx, cy) is not None:\n                path.append((cx, cy))\n                cx, cy = parent[cx][cy]\n            path.reverse()\n            return e, path\n        if e > d[x][y]:\n            continue\n        for dx, dy in dirs:\n            nx, ny = x + dx, y + dy\n            if 0 <= nx < r and 0 <= ny < c:\n                ne = max(e, abs(heights[x][y] - heights[nx][ny]))\n                if ne < d[nx][ny]:\n                    d[nx][ny] = ne\n                    parent[nx][ny] = (x, y)\n                    heapq.heappush(h, (ne, nx, ny))\n\nif __name__ == \"__main__\":\n    h = [[1,2,2],[3,8,2],[5,3,5]]\n    effort, path = minimumEffort(h)\n    print(effort)\n    print(path)', NULL, 92, 3, 0, 1, '代码正确实现了题目要求的最小体力消耗路径求解功能，核心采用Dijkstra算法解决最小瓶颈路径问题，逻辑严谨。通过优先队列选择当前最优路径扩展，维护每个点的最小最大高度差，利用parent数组准确回溯出最优路径。测试用例运行结果符合预期，算法时间复杂度为O(rows*colslog(rows*cols))，效率合理。不过变量命名较简洁（如d数组），缺少关键注释，一定程度影响可读性。', NULL, NULL, NULL, '[\"优化变量命名，增强代码可读性\",\"添加关键注释，说明算法逻辑与变量含义\"]', NULL, 0, '2026-04-14 20:31:06', '2026-04-14 20:31:06', 'importheapq\ndefminimumEffort(heights):\n  rows,cols=len(heights),len(heights[0])\n  #记录每个点的最小体力消耗（路径上最大高度差的最小值）\n  min_max_diff=[[float(\'inf\')]*colsfor_inrange(rows)]\n  #记录每个点的父节点，用于回溯路径\n  parent=[[None]*colsfor_inrange(rows)]\n  min_max_diff[0][0]=0\n  #优先队列：(当前路径的最大高度差,x坐标,y坐标)\n  heap=[(0,0,0)]\n  #上下左右四个移动方向\n  directions=[(-1,0),(1,0),(0,-1),(0,1)]\n\n  whileheap:\n      current_effort,x,y=heapq.heappop(heap)\n      #到达终点，回溯路径并返回结果\n      ifx==rows-1andy==cols-1:\n          path=[]\n          cx,cy=x,y\n          while(cx,cy)isnotNone:\n              path.append((cx,cy))\n              cx,cy=parent[cx][cy]\n          path.reverse()\n          returncurrent_effort,path\n      #已有更优路径到达该点，跳过\n      ifcurrent_effort>min_max_diff[x][y]:\n          continue\n      #遍历四个方向的邻居\n      fordx,dyindirections:\n          nx,ny=x+dx,y+dy\n          if0<=nx<rowsand0<=ny<cols:\n              #计算经过当前点到邻居的路径最大高度差\n              new_effort=max(current_effort,abs(heights[x][y]-heights[nx][ny]))\n              #如果新路径更优，更新并加入优先队列\n              ifnew_effort<min_max_diff[nx][ny]:\n                  min_max_diff[nx][ny]=new_effort\n                  parent[nx][ny]=(x,y)\n                  heapq.heappush(heap,(new_effort,nx,ny))\n\nif__name__==\"__main__\":\n  h=[[1,2,2],[3,8,2],[5,3,5]]\n  effort,path=minimumEffort(h)\n  print(\"最小体力消耗值:\",effort)\n  print(\"路径:\",path)'),
(29, 1, 11, '最短路径可视化器核心机制', 'Java', 'import java.util.*;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner scanner = new Scanner(System.in);\n    // 你需要实现一个核心寻路引擎。给定带权无向图的节点数 n、边列表 edges，以及起点 start 和终点 end，请实现 Dijkstra 算法计算最短距离，并返回完整的访问节点顺序和最后的最短路径。注意：不仅要算出最短距离，还必须记录算法从优先队列中取出节点的“访问顺序”，以供前端实现动画过程。\n  }\n}\n', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-04-20 05:33:21', '2026-04-20 05:33:21', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_completed_error_item`
--

CREATE TABLE `t_completed_error_item` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `level_id` bigint(20) DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `level_type` varchar(50) DEFAULT NULL,
  `question` longtext DEFAULT NULL,
  `user_answer` longtext DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `correct_answer` longtext DEFAULT NULL,
  `analysis_status` varchar(50) DEFAULT NULL,
  `analysis` varchar(2000) DEFAULT NULL,
  `source_error_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `last_error_at` datetime DEFAULT NULL,
  `completed_at` datetime NOT NULL,
  `analysis_data_json` longtext DEFAULT NULL,
  `analyzed_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `t_course`
--

CREATE TABLE `t_course` (
  `id` bigint(20) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `teacher` varchar(255) DEFAULT NULL,
  `track` varchar(255) DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `progress` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_course`
--

INSERT INTO `t_course` (`id`, `title`, `teacher`, `track`, `duration`, `progress`) VALUES
(1, '算法基础入门', '张老师', 'algo', '2小时', 30),
(2, '数据结构精讲', '李老师', 'ds', '3小时', 0),
(3, '竞赛算法进阶', '王老师', 'contest', '4小时', 0),
(4, '算法基础入门', '张老师', 'algo', '2小时', 30),
(5, '数据结构精讲', '李老师', 'ds', '3小时', 0),
(6, '竞赛算法进阶', '王老师', 'contest', '4小时', 0);

-- --------------------------------------------------------

--
-- 表的结构 `t_course_comment`
--

CREATE TABLE `t_course_comment` (
  `id` bigint(20) NOT NULL,
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(255) DEFAULT NULL,
  `user_avatar` varchar(255) DEFAULT NULL,
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID',
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程评论表';

-- --------------------------------------------------------

--
-- 表的结构 `t_course_tags`
--

CREATE TABLE `t_course_tags` (
  `course_id` bigint(20) NOT NULL,
  `tags` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程标签表';

--
-- 转存表中的数据 `t_course_tags`
--

INSERT INTO `t_course_tags` (`course_id`, `tags`) VALUES
(1, '基础'),
(1, '入门'),
(2, '数据结构'),
(2, '核心'),
(3, '竞赛'),
(3, '进阶');

-- --------------------------------------------------------

--
-- 表的结构 `t_error_item`
--

CREATE TABLE `t_error_item` (
  `id` bigint(20) NOT NULL,
  `level_id` bigint(20) DEFAULT NULL,
  `question` longtext DEFAULT NULL,
  `user_answer` longtext DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `analysis_status` varchar(255) DEFAULT NULL,
  `analysis` varchar(2000) DEFAULT NULL,
  `correct_answer` longtext DEFAULT NULL,
  `level_type` varchar(50) DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `analysis_data_json` longtext DEFAULT NULL,
  `analyzed_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_error_item`
--

INSERT INTO `t_error_item` (`id`, `level_id`, `question`, `user_answer`, `description`, `created_at`, `analysis_status`, `analysis`, `correct_answer`, `level_type`, `title`, `updated_at`, `user_id`, `analysis_data_json`, `analyzed_at`) VALUES
(3, 3, '快速排序的平均时间复杂度是？', 'O(n²)', '分治思想的典型代表。', '2026-04-08 11:24:02', '已分析', '', 'O(n log n)', 'single', '快速排序', '2026-04-10 07:36:08.000000', 4, NULL, NULL),
(5, 1, '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '0', '经典哈希表入门题。', '2026-04-08 13:53:23', '未分析', NULL, '哈希表', 'single', '两数之和', '2026-04-08 13:53:23.000000', 6, NULL, NULL),
(6, 3, '快速排序的平均时间复杂度是？', 'O(n²)', '分治思想的典型代表。', '2026-04-08 13:53:29', '已分析', '总结：这是一道需要回到题意和选项本身重新核对的题目。\n\n根因：对题意、边界或关键知识点的理解还不够稳定\n说明：建议先复盘题干，再对照自己的答案定位偏差点。\n\n建议：\n- 先做一次定向复盘：把正确答案、你的答案、错因三列写清楚。', 'O(n log n)', 'single', '快速排序', '2026-04-08 13:56:26.000000', 6, NULL, NULL),
(7, 4, '归并排序的额外空间复杂度通常是？', 'O(2^n)', '稳定排序，适合讲清楚分治流程。', '2026-04-08 13:53:35', '未分析', NULL, 'O(n)', 'single', '归并排序', '2026-04-08 13:53:35.000000', 6, NULL, NULL),
(8, 2, '在有序数组中查找目标值，二分查找的时间复杂度是？', 'O(n²)', '掌握边界写法比背模板更重要。', '2026-04-10 08:00:06', '已分析', '', 'O(log n)', 'single', '二分查找', '2026-04-10 08:01:30.000000', 4, NULL, NULL),
(11, 3, '快速排序的平均时间复杂度是？', 'O(n)', '分治思想的典型代表。', '2026-04-10 08:12:19', '已分析', '总结：这是一道需要回到题意和选项本身重新核对的题目。\n\n根因：对题意、边界或关键知识点的理解还不够稳定\n说明：建议先复盘题干，再对照自己的答案定位偏差点。\n\n建议：\n- 先做一次定向复盘：把正确答案、你的答案、错因三列写清楚。', 'O(n log n)', 'single', '快速排序', '2026-04-10 12:31:02.000000', 1, NULL, NULL),
(12, 10, '给定一个大小为 rows x cols 的二维高度矩阵 heights，你从左上角 (0, 0) 出发，目标是到达右下角 (rows-1, cols-1)。你每次可以上下左右移动。一条路径的“体力消耗”是该路径上相邻格子之间高度差绝对值的最大值。请实现一个函数，返回到达目标的最小体力消耗值，以及走过这条路径的完整坐标连线数组。', 'import heapq\n\ndef minEffort(h):\n    R, C = len(h), len(h[0])\n    # 队列: (当前最大落差, 行, 列, 走过的路径)\n    pq = [(0, 0, 0, [(0, 0)])]\n    seen = set()\n\n    while pq:\n        e, r, c, path = heapq.heappop(pq)\n        print(f\"到达: ({r},{c}) | 当前最大体力消耗: {e}\")\n        \n        if (r, c) == (R - 1, C - 1): \n            return e, path  # 到达右下角终点，直接返回\n            \n        if (r, c) in seen: continue\n        seen.add((r, c))\n\n        # 遍历上下左右邻居\n        for nr, nc in ((r+1, c), (r-1, c), (r, c+1), (r, c-1)):\n            if 0 <= nr < R and 0 <= nc < C and (nr, nc) not in seen:\n                ne = max(e, abs(h[r][c] - h[nr][nc]))\n                heapq.heappush(pq, (ne, nr, nc, path + [(nr, nc)]))\n\n# ========================\n# 运行测试\n# ========================\ngrid = [\n    [1, 2, 2],\n    [3, 8, 2],\n    [5, 3, 5]\n]\neffort, full_path = minEffort(grid)\nprint(f\"\\n最终结果 -> 消耗: {effort}, 路线: {full_path}\")', '2D矩阵上的变种 Dijkstra 寻路，寻找最大高度差最小的最优路径，并回溯打印完整移动路线。', '2026-04-10 11:25:08', 'ANALYZED', 'Summary: 先重新审题，再对比你的答案和正确思路之间的差异，优先找出最关键的误区。\n\nRoot cause: 可能遗漏了题目中的关键条件，或者对知识点理解还不够稳定。\nExplanation: 建议重新梳理题目要求，定位答案不匹配的具体步骤，并总结成一句可以复用的纠错提醒。\n\nSuggestions:\n- 做一次针对性复盘: 把正确思路、你的答案和错误原因并排写出来，找出最容易重复出错的步骤。', '使用优先队列（最小堆），队列元素维护 (当前最大落差, 行坐标, 列坐标, 当前累积的路径列表)。向四个方向扩展时，新落差等于 max(当前最大落差, 两个格子高度差的绝对值)。因为是最小堆，第一次弹出的右下角坐标就是最优解，直接返回对应记录的路径即可。', 'code', '最短路径与回溯', '2026-04-17 08:07:11.000000', 1, '{\"summary\":\"先重新审题，再对比你的答案和正确思路之间的差异，优先找出最关键的误区。\",\"errorAnalysis\":{\"rootCause\":\"可能遗漏了题目中的关键条件，或者对知识点理解还不够稳定。\",\"detailedExplanation\":\"建议重新梳理题目要求，定位答案不匹配的具体步骤，并总结成一句可以复用的纠错提醒。\",\"commonMistakes\":[\"遗漏边界条件\",\"概念混淆\",\"解题步骤不完整\"]},\"knowledgePoints\":[{\"name\":\"题意拆解\",\"description\":\"在动手作答前，先明确题目真正要求的目标、限制条件和判定标准。\",\"masteryLevel\":\"beginner\"}],\"suggestions\":[{\"title\":\"做一次针对性复盘\",\"description\":\"把正确思路、你的答案和错误原因并排写出来，找出最容易重复出错的步骤。\",\"priority\":\"high\",\"actionItems\":[\"重新审题\",\"对照正确思路\",\"记录错误模式\"]}],\"recommendedProblems\":[{\"levelId\":11,\"title\":\"最短路径可视化器核心机制\",\"question\":\"你需要实现一个核心寻路引擎。给定带权无向图的节点数 n、边列表 edges，以及起点 start 和终点 end，请实现 Dijkstra 算法计算最短距离，并返回完整的访问节点顺序和最后的最短路径。注意：不仅要算出最短距离，还必须记录算法从优先队列中取出节点的“访问顺序”，以供前端实现动画过程。\",\"type\":\"code\",\"difficulty\":\"medium\",\"reason\":\"题库中找到了和「题意拆解」相关的练习题，适合立刻巩固。\",\"fromQuestionBank\":true,\"options\":[]},{\"levelId\":1,\"title\":\"队列与优先队列\",\"question\":\"给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？\",\"type\":\"single\",\"difficulty\":\"medium\",\"reason\":\"题库中找到了和「题意拆解」相关的练习题，适合立刻巩固。\",\"fromQuestionBank\":true,\"options\":[\"哈希表\",\"暴力枚举\",\"二分查找\",\"二分查找\",\"2\",\"1\",\"1\"]},{\"levelId\":2,\"title\":\"二叉树的层序遍历\",\"question\":\"在有序数组中查找目标值，二分查找的时间复杂度是？\",\"type\":\"single\",\"difficulty\":\"medium\",\"reason\":\"题库中找到了和「题意拆解」相关的练习题，适合立刻巩固。\",\"fromQuestionBank\":true,\"options\":[\"O(n)\",\"O(n²)\",\"O(log n)\",\"O(n log n)\"]}]}', '2026-04-17 08:07:11.000000'),
(13, 4, '归并排序的额外空间复杂度通常是？', 'O(1)', '稳定排序，适合讲清楚分治流程。', '2026-04-10 12:23:30', '已分析', '总结：这是一道需要回到题意和选项本身重新核对的题目。\n\n根因：对题意、边界或关键知识点的理解还不够稳定\n说明：建议先复盘题干，再对照自己的答案定位偏差点。\n\n建议：\n- 先做一次定向复盘：把正确答案、你的答案、错因三列写清楚。', 'O(n)', 'single', '归并排序', '2026-04-10 12:24:37.000000', 1, NULL, NULL),
(14, 3, '快速排序的平均时间复杂度是？', 'O(n²)', '分治思想的典型代表。', '2026-04-10 15:12:28', '未分析', NULL, 'O(n log n)', 'single', '快速排序', '2026-04-10 15:13:32.000000', 8, NULL, NULL),
(15, NULL, '1', '2', '123456', '2026-04-15 06:04:40', 'ANALYZED', 'Summary: 先重新审题，再对比你的答案和正确思路之间的差异，优先找出最关键的误区。\n\nRoot cause: 可能遗漏了题目中的关键条件，或者对知识点理解还不够稳定。\nExplanation: 建议重新梳理题目要求，定位答案不匹配的具体步骤，并总结成一句可以复用的纠错提醒。\n\nSuggestions:\n- 做一次针对性复盘: 把正确思路、你的答案和错误原因并排写出来，找出最容易重复出错的步骤。', NULL, NULL, NULL, '2026-04-15 06:06:29.000000', 10, '{\"summary\":\"先重新审题，再对比你的答案和正确思路之间的差异，优先找出最关键的误区。\",\"errorAnalysis\":{\"rootCause\":\"可能遗漏了题目中的关键条件，或者对知识点理解还不够稳定。\",\"detailedExplanation\":\"建议重新梳理题目要求，定位答案不匹配的具体步骤，并总结成一句可以复用的纠错提醒。\",\"commonMistakes\":[\"遗漏边界条件\",\"概念混淆\",\"解题步骤不完整\"]},\"knowledgePoints\":[{\"name\":\"题意拆解\",\"description\":\"在动手作答前，先明确题目真正要求的目标、限制条件和判定标准。\",\"masteryLevel\":\"beginner\"}],\"suggestions\":[{\"title\":\"做一次针对性复盘\",\"description\":\"把正确思路、你的答案和错误原因并排写出来，找出最容易重复出错的步骤。\",\"priority\":\"high\",\"actionItems\":[\"重新审题\",\"对照正确思路\",\"记录错误模式\"]}],\"recommendedProblems\":[{\"levelId\":6,\"title\":\"无权迷宫的逃生路线\",\"question\":\"一次可以爬 1 或 2 阶，爬到第 n 阶的方案数，本质上接近哪类问题？\",\"type\":\"single\",\"difficulty\":\"medium\",\"reason\":\"题库中找到了和「题意拆解」相关的练习题，适合立刻巩固。\",\"fromQuestionBank\":true,\"options\":[]},{\"levelId\":8,\"title\":\"单词接龙\",\"question\":\"0-1 背包的经典 DP 状态转移，时间复杂度通常写作？\",\"type\":\"single\",\"difficulty\":\"medium\",\"reason\":\"题库中找到了和「题意拆解」相关的练习题，适合立刻巩固。\",\"fromQuestionBank\":true,\"options\":[]},{\"levelId\":10,\"title\":\"最短路径与回溯\",\"question\":\"给定一个大小为 rows x cols 的二维高度矩阵 heights，你从左上角 (0, 0) 出发，目标是到达右下角 (rows-1, cols-1)。你每次可以上下左右移动。一条路径的“体力消耗”是该路径上相邻格子之间高度差绝对值的最大值。请实现一个函数，返回到达目标的最小体力消耗值，以及走过这条路径的完整坐标连线数组。\",\"type\":\"code\",\"difficulty\":\"medium\",\"reason\":\"题库中找到了和「题意拆解」相关的练习题，适合立刻巩固。\",\"fromQuestionBank\":true,\"options\":[]}]}', '2026-04-15 06:06:29.000000');

-- --------------------------------------------------------

--
-- 表的结构 `t_error_item_option_snapshot`
--

CREATE TABLE `t_error_item_option_snapshot` (
  `id` bigint(20) NOT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  `is_selected` bit(1) DEFAULT NULL,
  `option_content` varchar(1000) DEFAULT NULL,
  `option_key` varchar(10) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  `error_item_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_error_item_option_snapshot`
--

INSERT INTO `t_error_item_option_snapshot` (`id`, `is_correct`, `is_selected`, `option_content`, `option_key`, `sort_order`, `error_item_id`) VALUES
(12, b'0', b'0', '-1', 'A', 1, 5),
(13, b'0', b'1', '0', 'B', 2, 5),
(14, b'0', b'0', '3', 'C', 3, 5),
(15, b'0', b'0', '4', 'D', 4, 5),
(16, b'0', b'0', '5', 'E', 5, 5),
(17, b'0', b'0', '9', 'F', 6, 5),
(18, b'0', b'0', '12', 'G', 7, 5),
(19, b'0', b'0', 'O(1)', 'A', 1, 6),
(20, b'0', b'0', 'O(n)', 'B', 2, 6),
(21, b'0', b'0', 'O(log n)', 'C', 3, 6),
(22, b'0', b'1', 'O(n²)', 'D', 4, 6),
(23, b'0', b'0', 'O(1)', 'A', 1, 7),
(24, b'1', b'0', 'O(n)', 'B', 2, 7),
(25, b'0', b'0', 'O(n²)', 'C', 3, 7),
(26, b'0', b'1', 'O(2^n)', 'D', 4, 7),
(54, b'0', b'0', 'O(1)', 'A', 1, 3),
(55, b'0', b'0', 'O(n)', 'B', 2, 3),
(56, b'0', b'0', 'O(log n)', 'C', 3, 3),
(57, b'0', b'1', 'O(n²)', 'D', 4, 3),
(58, b'0', b'0', 'O(n)', 'A', 1, 8),
(59, b'0', b'1', 'O(n²)', 'B', 2, 8),
(60, b'1', b'0', 'O(log n)', 'C', 3, 8),
(61, b'0', b'0', 'O(n log n)', 'D', 4, 8),
(89, b'0', b'0', 'O(1)', 'A', 1, 11),
(90, b'0', b'1', 'O(n)', 'B', 2, 11),
(91, b'0', b'0', 'O(log n)', 'C', 3, 11),
(92, b'0', b'0', 'O(n²)', 'D', 4, 11),
(93, b'0', b'1', 'O(1)', 'A', 1, 13),
(94, b'1', b'0', 'O(n)', 'B', 2, 13),
(95, b'0', b'0', 'O(n²)', 'C', 3, 13),
(96, b'0', b'0', 'O(2^n)', 'D', 4, 13),
(129, b'0', b'0', 'O(1)', 'A', 1, 14),
(130, b'0', b'0', 'O(n)', 'B', 2, 14),
(131, b'0', b'0', 'O(log n)', 'C', 3, 14),
(132, b'0', b'1', 'O(n²)', 'D', 4, 14);

-- --------------------------------------------------------

--
-- 表的结构 `t_forum_comment`
--

CREATE TABLE `t_forum_comment` (
  `id` bigint(20) NOT NULL,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `author_level` varchar(255) DEFAULT NULL,
  `content` varchar(2000) NOT NULL,
  `likes` int(11) DEFAULT 0,
  `parent_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `company` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_forum_comment`
--

INSERT INTO `t_forum_comment` (`id`, `post_id`, `user_id`, `author`, `avatar`, `author_level`, `content`, `likes`, `parent_id`, `created_at`, `company`) VALUES
(4, 4, 5, '一只爱刷题的熊', '/api/uploads/avatars/avatar_5_cc9712a4.jpg', 'Lv.1', '😭😭超级共情，dfs太难了', 0, NULL, '2026-04-10 00:36:50', NULL),
(5, 4, 6, 'Molic', '/api/uploads/avatars/avatar_6_78be5ceb.jpg', 'Lv.1', '最好是啊你', 0, NULL, '2026-04-10 02:00:43', NULL),
(6, 5, 4, 'aiaod', '/api/uploads/avatars/avatar_4_5712669e.jpg', 'Lv.2', '向大佬膜拜', 0, NULL, '2026-04-10 04:46:46', NULL),
(7, 3, 4, '一只爱刷题的熊', '/api/uploads/avatars/avatar_5_cc9712a4.jpg', '青藤 Lv.1', '大一首战太牛了！我也是 Python 组，考完只想好好睡一觉，不管结果如何，这段备考的日子就已经超棒了！✨', 0, NULL, '2026-04-14 04:16:10', NULL),
(9, 10, 12, '大鸡巴哥', NULL, '青藤 Lv.1', '可以可以，很强', 0, NULL, '2026-04-17 10:03:45', NULL),
(10, 10, 12, '大鸡巴哥', NULL, '青藤 Lv.1', '认可了', 0, NULL, '2026-04-17 10:03:49', NULL),
(11, 9, 1, 'aiaod', '/api/uploads/avatars/avatar_1_a8d05152.jpg', '青藤 Lv.1', '狠狠点了👍', 0, NULL, '2026-04-17 11:26:40', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_forum_post`
--

CREATE TABLE `t_forum_post` (
  `id` bigint(20) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `author_level` varchar(255) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `content` varchar(2000) NOT NULL,
  `quote` varchar(255) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `likes` int(11) DEFAULT 0,
  `comments` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `location` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_forum_post`
--

INSERT INTO `t_forum_post` (`id`, `author`, `author_level`, `avatar`, `topic`, `content`, `quote`, `tag`, `likes`, `comments`, `created_at`, `location`, `user_id`) VALUES
(1, 'aiaod', '青藤 Lv.1', '/api/uploads/avatars/avatar_1_e4ae7c25.jpg', 'DFS太难了', 'DFS真的是算法入门最大的坎吧…代码就几行，逻辑绕到爆炸，递归栈执行顺序完全理不清，debug全靠蒙，提交全靠赌。明明回溯、剪枝的概念都懂，一到写题就卡壳，终止条件、回溯时机、剪枝逻辑全是坑。有没有大佬分享一下，到底怎么才能彻底搞懂DFS？求学习方法、刷题顺序，救救被递归折磨的孩子🙏😂', '', '# 学习交流', 1, 2, '2026-04-10 00:20:44', '未知', 1),
(3, 'aiaod', '青藤 Lv.1', '/api/uploads/avatars/avatar_1_a8d05152.jpg', '2026 蓝桥杯省赛考完复盘｜大一软件新生首战心得 & 避坑指南', '刚考完 2026 蓝桥杯 Python 组，整体难度适中，基础题占比很大。\n给后面备考的同学一点小提醒：\nPython 语法一定要熟，输入输出、字符串处理、列表操作是高频考点。\n不要死磕难题，先把简单题、中档题稳稳拿分，性价比最高。\n时间分配很重要，一道题卡太久很容易后面做不完。\n基础算法（排序、枚举、贪心、二分）一定要练熟，省赛非常实用。\n第一次参赛，不管结果如何，也算完整走完一轮，收获很多。\n祝大家都能取得满意的成绩！', '', '#  蓝桥杯', 3, 1, '2026-04-14 04:05:44', '内网IP', 1),
(9, 'Molic', '青藤 Lv.1', '/api/uploads/avatars/avatar_6_78be5ceb.jpg', 'AlgoMind 真的太懂算法学习者了！', '用了一段时间 AlgoMind，真的被这个平台狠狠圈粉！不管是智能错题分析帮我精准定位知识盲区，还是个性化学习规划帮我理清刷题思路，都完美解决了我之前算法学习的各种痛点，刷题效率直接拉满，再也不用盲目啃题了，真心推荐给每一个在学算法的同学！', '', '# 学习交流', 2, 2, '2026-04-14 04:20:56', '内网IP', 2),
(12, 'aiaod', '青藤 Lv.1', '/api/uploads/avatars/avatar_1_a8d05152.jpg', '我想上岸', '我想上岸', '', '# 学习交流', 1, 0, '2026-04-19 09:05:41', '内网IP', 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_knowledge_article`
--

CREATE TABLE `t_knowledge_article` (
  `id` bigint(20) NOT NULL,
  `badge` varchar(255) DEFAULT NULL,
  `checklist_json` longtext DEFAULT NULL,
  `code_blocks_json` longtext DEFAULT NULL,
  `complexity` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by_user_id` bigint(20) DEFAULT NULL,
  `english_title` varchar(255) DEFAULT NULL,
  `insights_json` longtext DEFAULT NULL,
  `lead` text DEFAULT NULL,
  `learning_objectives_json` longtext DEFAULT NULL,
  `published` bit(1) DEFAULT NULL,
  `read_time` varchar(255) DEFAULT NULL,
  `related_slugs_text` text DEFAULT NULL,
  `section_description` text DEFAULT NULL,
  `section_id` varchar(120) NOT NULL,
  `section_title` varchar(120) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  `spotlight_accent` varchar(255) DEFAULT NULL,
  `spotlight_description` text DEFAULT NULL,
  `spotlight_enabled` bit(1) DEFAULT NULL,
  `spotlight_eyebrow` varchar(255) DEFAULT NULL,
  `spotlight_title` varchar(255) DEFAULT NULL,
  `strategy_steps_json` longtext DEFAULT NULL,
  `summary` text DEFAULT NULL,
  `tags_text` text DEFAULT NULL,
  `title` varchar(160) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by_user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_knowledge_article`
--

INSERT INTO `t_knowledge_article` (`id`, `badge`, `checklist_json`, `code_blocks_json`, `complexity`, `created_at`, `created_by_user_id`, `english_title`, `insights_json`, `lead`, `learning_objectives_json`, `published`, `read_time`, `related_slugs_text`, `section_description`, `section_id`, `section_title`, `slug`, `sort_order`, `spotlight_accent`, `spotlight_description`, `spotlight_enabled`, `spotlight_eyebrow`, `spotlight_title`, `strategy_steps_json`, `summary`, `tags_text`, `title`, `updated_at`, `updated_by_user_id`) VALUES
(1, '刷题核心', '[\"写转移前，先把状态定义写成一句自然语言。\",\"确认初始化覆盖了最小规模的子问题。\",\"检查遍历顺序是否满足依赖方向。\",\"能够口头说出时间复杂度和空间复杂度。\"]', '[{\"language\":\"Java\",\"title\":\"爬楼梯的一维 DP 模板\",\"code\":\"public int climbStairs(int n) {\\n    if (n <= 2) {\\n        return n;\\n    }\\n\\n    int[] dp = new int[n + 1];\\n    dp[1] = 1;\\n    dp[2] = 2;\\n    for (int i = 3; i <= n; i++) {\\n        dp[i] = dp[i - 1] + dp[i - 2];\\n    }\\n    return dp[n];\\n}\\n\",\"callouts\":[\"dp[i] 表示到达第 i 阶的方法数。\",\"遍历顺序必须从小到大。\"]}]', '常见为 O(n) 到 O(n^2)', '2026-04-21 13:55:02.000000', 1, 'Dynamic Programming', '[{\"title\":\"面试表达\",\"description\":\"先讲状态定义，再讲转移来源，最后补复杂度，表达会明显更稳。\",\"accent\":\"emerald\"},{\"title\":\"常见误区\",\"description\":\"状态里塞了太多信息，会让维度暴涨。先从最少必要信息开始。\",\"accent\":\"amber\"},{\"title\":\"优化思路\",\"description\":\"只有依赖方向非常清楚时，才建议做滚动数组优化。\",\"accent\":\"cyan\"}]', '当题目出现最值、方案数、可达性等信号时，优先尝试写出状态定义，并用小样例验证转移方向。', '[\"能用自然语言说清楚 dp[i] 或 dp[i][j] 的语义。\",\"知道转移、初始化和遍历顺序为什么要成套出现。\",\"能判断什么时候适合做空间优化。\"]', b'1', '7 min read', 'greedy-strategy,binary-search-answer', '适合建立状态设计、转移推导和复杂度表达的基础能力。', 'algorithm-basics', '算法基础', 'dp-basics', 10, 'emerald', '适合临近面试时快速过一遍 DP 的思考路径和典型表达方式。', b'1', '面试速刷', '先定义状态，再谈转移', '[{\"index\":\"01\",\"title\":\"先定义状态\",\"description\":\"优先回答“当前这个位置的答案代表什么”，避免一上来就写公式。\",\"badge\":\"思维入口\"},{\"index\":\"02\",\"title\":\"找依赖来源\",\"description\":\"把当前答案依赖的更小问题写出来，再翻译成代码里的下标关系。\",\"badge\":\"转移\"},{\"index\":\"03\",\"title\":\"补边界与顺序\",\"description\":\"很多 DP 出错不是转移错，而是初始化缺了或遍历方向反了。\",\"badge\":\"实现\"}]', '动态规划最重要的不是记住模板，而是先把状态定义清楚，再明确当前答案依赖哪些更小子问题。', '状态设计,转移方程,初始化,滚动数组', '动态规划入门', '2026-04-21 13:55:02.000000', 1),
(2, '面试高频', '[\"先确认题目是否存在单调的局部选择。\",\"思考排序字段和目标函数是否一致。\",\"别只背模板，要准备一句正确性说明。\"]', '[]', '通常为 O(n log n)', '2026-04-21 13:55:02.000000', 1, 'Greedy Strategy', '[{\"title\":\"高频题型\",\"description\":\"区间调度、最少箭射气球、跳跃游戏、股票题都常见贪心。\",\"accent\":\"emerald\"},{\"title\":\"风险点\",\"description\":\"如果局部最优会锁死后续选择，通常就该回到 DP 或二分。\",\"accent\":\"amber\"},{\"title\":\"答题建议\",\"description\":\"先给出策略，再补“为什么它成立”，会比只报结论更有说服力。\",\"accent\":\"cyan\"}]', '看到区间调度、最少覆盖、跳跃类题目时，先观察是否能通过排序把决策空间变成单调结构。', '[\"识别常见的贪心信号：可排序、可局部决策、可证明不后悔。\",\"理解为什么排序常常是贪心成立的前提。\",\"会用一句话解释策略为什么成立。\"]', b'1', '6 min read', 'dp-basics,binary-search-answer', '帮助你判断什么时候可以放心地做局部最优选择。', 'algorithm-basics', '算法基础', 'greedy-strategy', 20, 'emerald', '', b'0', '', '', '[{\"index\":\"01\",\"title\":\"观察能否排序\",\"description\":\"很多贪心题都要先按结束时间、收益或代价排序。\",\"badge\":\"预处理\"},{\"index\":\"02\",\"title\":\"定义局部选择\",\"description\":\"每一步到底选最早结束还是最小代价，要和目标严格对齐。\",\"badge\":\"决策\"},{\"index\":\"03\",\"title\":\"补正确性解释\",\"description\":\"说明为什么当前选择不会让后续更差，这是答案能站住的关键。\",\"badge\":\"证明\"}]', '贪心不是拍脑袋选局部最优，而是要证明这种选择不会破坏全局最优。', '排序,局部最优,正确性证明', '贪心策略', '2026-04-21 13:55:02.000000', 1),
(3, '易错专题', '[\"先问自己：答案是否具备单调性。\",\"判定函数只做“是否可行”的判断。\"]', '[]', 'O(log range * check)', '2026-04-21 13:55:02.000000', 1, 'Binary Search on Answer', '[{\"title\":\"常见搭配\",\"description\":\"二分答案经常和贪心、前缀和、图搜索一起出现。\",\"accent\":\"emerald\"},{\"title\":\"高危细节\",\"description\":\"边界循环条件、mid 取法和最终返回值最容易出错。\",\"accent\":\"amber\"}]', '当你能写出一个随答案变化而单调的判定函数时，就可以考虑把枚举答案改成二分答案。', '[\"能判断题目是否具备可二分的单调性。\",\"会设计 check(mid) 并解释其返回值含义。\"]', b'1', '6 min read', 'greedy-strategy,graph-shortest-path', '适合处理“求最小可行值 / 最大可行值”这类题目。', 'algorithm-basics', '算法基础', 'binary-search-answer', 30, 'cyan', '', b'0', '', '', '[{\"index\":\"01\",\"title\":\"找答案空间\",\"description\":\"先明确二分的是数值范围还是候选结果集合。\",\"badge\":\"范围\"},{\"index\":\"02\",\"title\":\"写判定函数\",\"description\":\"check(mid) 只负责判断当前 mid 是否可行。\",\"badge\":\"判定\"},{\"index\":\"03\",\"title\":\"收缩边界\",\"description\":\"根据要找的是最小可行值还是最大可行值决定更新规则。\",\"badge\":\"边界\"}]', '二分的不一定是数组下标，也可以是满足条件的数值区间或某个候选答案。', '单调性,判定函数,边界', '二分答案', '2026-04-21 13:55:02.000000', 1),
(4, '高频结构', '[\"统一使用一套区间表示方式，如 [l, r]。\",\"区间修改时再考虑懒标记，不要一上来就写复杂。\"]', '[]', '单次操作 O(log n)', '2026-04-21 13:55:02.000000', 1, 'Segment Tree', '[{\"title\":\"适用场景\",\"description\":\"数组持续变化，同时要频繁查询区间聚合值时，线段树明显优于前缀和。\",\"accent\":\"emerald\"},{\"title\":\"常见失误\",\"description\":\"mid 划分、递归边界和下标范围最容易写错。\",\"accent\":\"amber\"}]', '当你既要查区间和、区间最值，又不能接受每次线性扫描时，线段树通常是最稳的选择。', '[\"理解每个节点维护的区间语义。\",\"能写出 build、query、update 的核心骨架。\"]', b'1', '8 min read', 'graph-shortest-path,dp-basics', '围绕区间查询和区间更新整理的一篇高频结构文档。', 'data-structures', '数据结构与图论', 'segment-tree', 10, 'cyan', '如果你在刷题里频繁碰到“区间最值 / 区间求和 + 修改”，这篇值得反复看。', b'1', '高频结构', '区间问题的主力武器', '[{\"index\":\"01\",\"title\":\"明确节点语义\",\"description\":\"每个节点维护区间和、最大值还是最小值，决定了聚合方式。\",\"badge\":\"建模\"},{\"index\":\"02\",\"title\":\"递归划分区间\",\"description\":\"不断把区间二分到叶子节点，再自底向上合并信息。\",\"badge\":\"建树\"},{\"index\":\"03\",\"title\":\"只访问相关路径\",\"description\":\"查询和更新只递归访问与目标区间相交的节点。\",\"badge\":\"复杂度\"}]', '线段树擅长处理数组频繁修改且需要大量区间查询的场景，是区间问题里的高频主力结构。', '区间查询,区间更新,懒标记', '线段树', '2026-04-21 13:55:02.000000', 1),
(5, '图论专题', '[\"先确认边权是否非负。\",\"先想清楚建图结构，再写算法。\"]', '[]', 'Dijkstra 常见为 O((n + m) log n)', '2026-04-21 13:55:02.000000', 1, 'Shortest Path', '[{\"title\":\"面试常问\",\"description\":\"为什么 Dijkstra 遇到负权边会失效，这是很高频的追问。\",\"accent\":\"emerald\"},{\"title\":\"易错点\",\"description\":\"重复入堆是正常现象，弹出时要判断状态是否过期。\",\"accent\":\"amber\"}]', '无权图优先 BFS，非负权图常用 Dijkstra，存在负权边时再考虑 Bellman-Ford 或相关变体。', '[\"知道 BFS、Dijkstra、Bellman-Ford 的典型边界。\",\"会写优先队列版本的最短路模板。\"]', b'1', '8 min read', 'segment-tree,binary-search-answer', '帮助你快速判断 BFS、Dijkstra 和 Bellman-Ford 的适用边界。', 'data-structures', '数据结构与图论', 'graph-shortest-path', 20, 'cyan', '', b'0', '', '', '[{\"index\":\"01\",\"title\":\"先判断图类型\",\"description\":\"看清楚是有向图还是无向图，边权是否存在、是否可能为负。\",\"badge\":\"建模\"},{\"index\":\"02\",\"title\":\"选择算法\",\"description\":\"无权图用 BFS，非负权图用 Dijkstra，有负权时再换方案。\",\"badge\":\"选型\"},{\"index\":\"03\",\"title\":\"维护距离数组\",\"description\":\"dist 记录当前最短距离，优先队列负责每次扩展最优状态。\",\"badge\":\"实现\"}]', '最短路题最重要的不是背算法名，而是先判断图是否带权、边权是否非负，以及图是稀疏还是稠密。', '建图,堆优化,非负权边', '最短路算法', '2026-04-21 13:55:02.000000', 1),
(6, '底层补位', '[\"明确对象在栈上还是堆上。\",\"每次访问前做空指针和边界检查。\"]', '[]', '重点在风险控制而不是复杂度', '2026-04-21 13:55:02.000000', 1, 'Pointer & Memory', '[{\"title\":\"迁移价值\",\"description\":\"理解内存模型后，看 Java、Go、Rust 的抽象边界会更轻松。\",\"accent\":\"emerald\"},{\"title\":\"工程提醒\",\"description\":\"释放资源后立刻置空，是规避 use-after-free 的简单习惯。\",\"accent\":\"amber\"}]', '即使主语言不是 C/C++，理解堆、栈、空指针和悬空引用这些概念，也会帮助你写出更安全的代码。', '[\"理解栈内存和堆内存的分工。\",\"把边界检查当成实现的一部分。\"]', b'1', '7 min read', 'graph-shortest-path,dp-basics', '把刷题思维延伸到底层实现、生命周期和边界意识。', 'engineering-foundation', '工程实现', 'pointer-memory', 10, 'amber', '当你刷题没问题，但表达和实现细节不够稳时，这篇最容易补差距。', b'1', '工程补位', '补齐底层与实现细节', '[{\"index\":\"01\",\"title\":\"区分栈和堆\",\"description\":\"局部变量通常在栈上，动态申请对象在堆上，两者生命周期完全不同。\",\"badge\":\"内存模型\"},{\"index\":\"02\",\"title\":\"明确所有权\",\"description\":\"谁申请、谁释放，或者是否显式转移所有权，要在设计阶段说清楚。\",\"badge\":\"所有权\"},{\"index\":\"03\",\"title\":\"访问前做边界判断\",\"description\":\"空指针、越界和释放后继续访问，都是底层题里最致命的错误来源。\",\"badge\":\"安全性\"}]', '很多底层 bug 不在语法层，而在生命周期、所有权和边界意识上。理解指针与内存模型，会让你的实现更稳。', '栈与堆,所有权,空指针', '指针与内存', '2026-04-21 13:55:02.000000', 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_knowledge_base_config`
--

CREATE TABLE `t_knowledge_base_config` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `default_article_slug` varchar(255) DEFAULT NULL,
  `empty_state_description` text DEFAULT NULL,
  `empty_state_title` varchar(255) DEFAULT NULL,
  `quick_searches_text` text DEFAULT NULL,
  `site_subtitle` text DEFAULT NULL,
  `site_title` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_knowledge_base_config`
--

INSERT INTO `t_knowledge_base_config` (`id`, `created_at`, `default_article_slug`, `empty_state_description`, `empty_state_title`, `quick_searches_text`, `site_subtitle`, `site_title`, `updated_at`) VALUES
(1, '2026-04-21 13:55:02.000000', 'dp-basics', '可以试试搜索“动态规划”“最短路”“线段树”等关键词，或者直接从左侧目录开始阅读。', '没有找到匹配的知识主题', '动态规划,图论最短路,线段树,二分答案,指针与内存', '支持后台维护、文章发布和运营配置的算法知识中心。', 'AlgoMind 知识库', '2026-04-21 13:55:02.000000');

-- --------------------------------------------------------

--
-- 表的结构 `t_learning_plan`
--

CREATE TABLE `t_learning_plan` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `track` varchar(255) DEFAULT NULL,
  `track_label` varchar(255) DEFAULT NULL,
  `weekly_goal` int(11) DEFAULT 10,
  `week_goals_json` text DEFAULT NULL,
  `daily_tasks_json` text DEFAULT NULL,
  `recommendations_json` text DEFAULT NULL,
  `is_ai_generated` bit(1) DEFAULT NULL,
  `ai_analysis` varchar(4000) DEFAULT NULL,
  `generated_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_learning_plan`
--

INSERT INTO `t_learning_plan` (`id`, `user_id`, `track`, `track_label`, `weekly_goal`, `week_goals_json`, `daily_tasks_json`, `recommendations_json`, `is_ai_generated`, `ai_analysis`, `generated_at`, `created_at`, `updated_at`) VALUES
(1, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"基础巩固练习\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"新知识点学习\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-07 15:21:21', '2026-04-07 15:21:21', '2026-04-07 15:21:21'),
(2, 4, 'algo', '算法思维赛道', 11, '[{\"progress\":0,\"id\":1,\"title\":\"基础巩固练习\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"新知识点学习\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-07 16:48:58', '2026-04-07 16:48:58', '2026-04-07 16:48:58'),
(3, 6, 'algo', '算法思维赛道', 11, '[{\"progress\":0,\"id\":1,\"title\":\"基础巩固练习\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"新知识点学习\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-08 13:06:16', '2026-04-08 13:06:16', '2026-04-08 13:06:16'),
(4, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 08:48:11', '2026-04-09 08:48:11', '2026-04-09 08:48:11'),
(5, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 08:55:10', '2026-04-09 08:55:10', '2026-04-09 08:55:10'),
(6, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 09:02:36', '2026-04-09 09:02:36', '2026-04-09 09:02:36'),
(7, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 09:40:00', '2026-04-09 09:40:00', '2026-04-09 09:40:00'),
(8, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 09:57:44', '2026-04-09 09:57:44', '2026-04-09 09:57:44'),
(9, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 10:30:27', '2026-04-09 10:30:27', '2026-04-09 10:30:27'),
(10, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 10:41:57', '2026-04-09 10:41:57', '2026-04-09 10:41:57'),
(11, 7, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：Dynamic Programming\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"Dynamic Programming 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 00:25:20', '2026-04-10 00:25:20', '2026-04-10 00:25:20'),
(12, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 00:25:28', '2026-04-10 00:25:28', '2026-04-10 00:25:28'),
(13, 7, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：Dynamic Programming\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"Dynamic Programming 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 00:26:29', '2026-04-10 00:26:29', '2026-04-10 00:26:29'),
(14, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"description\":\"覆盖数组、字符串、链表及single类薄弱知识点的入门关卡，达成每周既定目标\",\"id\":1,\"title\":\"完成10个算法关卡\",\"target\":10},{\"progress\":0,\"description\":\"包括哈希表统计、异或运算等基础解法，突破薄弱环节\",\"id\":2,\"title\":\"掌握3种single类知识点核心解题思路\",\"target\":3},{\"progress\":0,\"description\":\"培养学习连贯性，提升坚持指数\",\"id\":3,\"title\":\"实现连续3天打卡\",\"target\":3},{\"progress\":0,\"description\":\"记录擅长领域及薄弱点的解题框架、易错点，便于复习\",\"id\":4,\"title\":\"整理1份入门解题思路笔记\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"description\":\"掌握数组遍历、双指针等基础解题模板，巩固擅长领域\",\"title\":\"学习数组基础题型解题框架\",\"type\":\"learn\",\"priority\":\"high\"},{\"duration\":30,\"description\":\"应用所学框架完成题目，熟悉解题流程\",\"title\":\"练习2道数组入门关卡题目\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":20,\"description\":\"了解single类问题的核心特征，掌握哈希表统计的基础解法\",\"title\":\"学习single类知识点基础概念与入门解法\",\"type\":\"learn\",\"priority\":\"medium\"},{\"duration\":15,\"description\":\"记录数组题型框架及single类解法要点，便于后续复习\",\"title\":\"整理当天解题思路与学习笔记\",\"type\":\"review\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":25,\"description\":\"掌握字符串翻转、子串匹配等常用技巧，巩固擅长领域\",\"title\":\"学习字符串基础变形题型解法\",\"type\":\"learn\",\"priority\":\"high\"},{\"duration\":35,\"description\":\"结合擅长领域知识点，逐步提升解题熟练度\",\"title\":\"练习2道字符串+1道链表入门关卡题目\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":20,\"description\":\"应用哈希表解法完成题目，初步突破薄弱点\",\"title\":\"练习1道single类入门关卡题目\",\"type\":\"practice\",\"priority\":\"medium\"},{\"duration\":15,\"description\":\"复盘解题过程，总结single类题目易错点\",\"title\":\"复习当天解题及薄弱点思路\",\"type\":\"review\",\"priority\":\"medium\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":25,\"description\":\"掌握链表反转、快慢指针等技巧，深化擅长领域能力\",\"title\":\"学习链表进阶题型核心思路\",\"type\":\"learn\",\"priority\":\"high\"},{\"duration\":30,\"description\":\"综合应用擅长知识点，保持解题节奏\",\"title\":\"练习1道数组+1道链表入门关卡题目\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"description\":\"尝试用异或运算等新解法完成题目，强化薄弱点掌握\",\"title\":\"练习2道single类基础关卡题目\",\"type\":\"practice\",\"priority\":\"medium\"},{\"duration\":15,\"description\":\"整理3天的解题笔记，梳理擅长领域及薄弱点的知识框架\",\"title\":\"复盘前3天学习内容\",\"type\":\"review\",\"priority\":\"medium\"}]}]', '[{\"description\":\"针对single类知识点，从基础到进阶讲解哈希表、异或等解法，精准突破薄弱点\",\"source\":\"B站-代码随想录\",\"type\":\"video\",\"title\":\"算法入门：Single类问题核心解法精讲\",\"priority\":\"high\"},{\"description\":\"系统梳理擅长领域的核心解题模板，巩固基础提升解题效率\",\"source\":\"力扣官方题解\",\"type\":\"article\",\"title\":\"数组/字符串/链表入门题型解题框架总结\",\"priority\":\"high\"},{\"description\":\"包含5道入门级single类题目，针对性练习薄弱点，逐步建立解题信心\",\"source\":\"LeetCode\",\"type\":\"practice\",\"title\":\"LeetCode入门Single专题训练\",\"priority\":\"medium\"}]', b'1', '你属于算法思维赛道纯新手，累计解题量为0，无连续打卡记录，坚持指数处于中等水平（50/100）。擅长数组、字符串、链表基础题型，但对single类知识点掌握不足，暂无具体错题积累，需从入门级内容逐步推进，兼顾基础巩固与薄弱点突破，同时培养学习连贯性。', '2026-04-10 00:33:53', '2026-04-10 00:33:53', '2026-04-10 00:33:53'),
(15, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 04:45:57', '2026-04-10 04:45:57', '2026-04-10 04:45:57'),
(16, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 05:22:10', '2026-04-10 05:22:10', '2026-04-10 05:22:10'),
(17, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:01:17', '2026-04-10 06:01:17', '2026-04-10 06:01:17'),
(18, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:03:19', '2026-04-10 06:03:19', '2026-04-10 06:03:19'),
(19, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:06:45', '2026-04-10 06:06:45', '2026-04-10 06:06:45'),
(20, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:13:20', '2026-04-10 06:13:20', '2026-04-10 06:13:20'),
(21, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:30:53', '2026-04-10 06:30:53', '2026-04-10 06:30:53'),
(22, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:32:40', '2026-04-10 06:32:40', '2026-04-10 06:32:40'),
(23, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:34:51', '2026-04-10 06:34:51', '2026-04-10 06:34:51'),
(24, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 07:19:53', '2026-04-10 07:19:53', '2026-04-10 07:19:53'),
(25, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：Dynamic Programming\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"Dynamic Programming 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:36:38', '2026-04-10 10:36:38', '2026-04-10 10:36:38'),
(26, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:39:02', '2026-04-10 10:39:02', '2026-04-10 10:39:02'),
(27, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:42:11', '2026-04-10 10:42:11', '2026-04-10 10:42:11'),
(28, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:43:09', '2026-04-10 10:43:09', '2026-04-10 10:43:09'),
(29, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:48:17', '2026-04-10 10:48:17', '2026-04-10 10:48:17'),
(30, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 12:25:33', '2026-04-10 12:25:33', '2026-04-10 12:25:33'),
(31, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 12:33:18', '2026-04-10 12:33:18', '2026-04-10 12:33:18'),
(32, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 13:18:02', '2026-04-10 13:18:02', '2026-04-10 13:18:02');
INSERT INTO `t_learning_plan` (`id`, `user_id`, `track`, `track_label`, `weekly_goal`, `week_goals_json`, `daily_tasks_json`, `recommendations_json`, `is_ai_generated`, `ai_analysis`, `generated_at`, `created_at`, `updated_at`) VALUES
(33, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 13:18:10', '2026-04-10 13:18:10', '2026-04-10 13:18:10'),
(34, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 13:19:24', '2026-04-10 13:19:24', '2026-04-10 13:19:24'),
(35, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:06:24', '2026-04-12 13:06:24', '2026-04-12 13:06:24'),
(36, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:36:33', '2026-04-12 13:36:33', '2026-04-12 13:36:33'),
(37, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:46:32', '2026-04-12 13:46:32', '2026-04-12 13:46:32'),
(38, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:53:06', '2026-04-12 13:53:06', '2026-04-12 13:53:06'),
(39, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:53:18', '2026-04-12 13:53:18', '2026-04-12 13:53:18'),
(40, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 14:12:49', '2026-04-12 14:12:49', '2026-04-12 14:12:49'),
(41, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 11:40:40', '2026-04-13 11:40:40', '2026-04-13 11:40:40'),
(42, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 13:30:15', '2026-04-13 13:30:15', '2026-04-13 13:30:15'),
(43, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 14:46:20', '2026-04-13 14:46:20', '2026-04-13 14:46:20'),
(44, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 17:13:39', '2026-04-13 17:13:39', '2026-04-13 17:13:39'),
(45, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 17:39:06', '2026-04-13 17:39:06', '2026-04-13 17:39:06'),
(46, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-17 08:55:08', '2026-04-17 08:55:08', '2026-04-17 08:55:08'),
(47, 1, 'algorithm', '算法', 5, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":3},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":2},{\"progress\":0,\"id\":3,\"title\":\"竞赛技巧提升\",\"target\":2},{\"progress\":2,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：背包问题\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"竞赛策略与技巧\",\"type\":\"learn\",\"priority\":\"medium\"},{\"duration\":30,\"title\":\"限时模拟训练\",\"type\":\"practice\",\"priority\":\"high\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":40,\"title\":\"algorithm 进阶知识点\",\"type\":\"learn\",\"priority\":\"medium\"},{\"duration\":35,\"title\":\"综合练习题\",\"type\":\"practice\",\"priority\":\"medium\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":45,\"title\":\"连续打卡奖励：挑战题\",\"type\":\"challenge\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"背包问题 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"技巧提升\",\"type\":\"video\",\"title\":\"竞赛算法精讲\",\"priority\":\"medium\"},{\"source\":\"实战演练\",\"type\":\"practice\",\"title\":\"Codeforces真题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-19 10:20:03', '2026-04-19 10:20:03', '2026-04-19 10:20:03');

-- --------------------------------------------------------

--
-- 表的结构 `t_level`
--

CREATE TABLE `t_level` (
  `id` bigint(20) NOT NULL,
  `track` varchar(255) DEFAULT NULL,
  `sort_order` int(11) DEFAULT 0,
  `name` varchar(255) DEFAULT NULL,
  `is_unlocked` bit(1) DEFAULT NULL,
  `reward_points` int(11) DEFAULT 10,
  `type` varchar(255) DEFAULT NULL,
  `question` varchar(1000) NOT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `creator_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_level`
--

INSERT INTO `t_level` (`id`, `track`, `sort_order`, `name`, `is_unlocked`, `reward_points`, `type`, `question`, `answer`, `description`, `creator_id`) VALUES
(1, 'algo', 1, '队列与优先队列', b'1', 10, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '哈希表', '经典哈希表入门题。', NULL),
(2, 'algo', 2, '二叉树的层序遍历', b'1', 10, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', 'O(log n)', '掌握边界写法比背模板更重要。', NULL),
(3, 'algo', 3, '图的表示', b'1', 15, 'single', '快速排序的平均时间复杂度是？', 'O(n log n)', '分治思想的典型代表。', NULL),
(4, 'algo', 4, '归并连通路径排序', b'1', 15, 'single', '归并排序的额外空间复杂度通常是？', 'O(n)', '稳定排序，适合讲清楚分治流程。', NULL),
(5, 'algo', 5, '岛屿数量与最大面积', b'1', 20, 'single', '求斐波那契数列时，使用标准 DP 写法的时间复杂度是？', 'O(n)', '先定义状态，再找转移。', NULL),
(6, 'algo', 6, '无权迷宫的逃生路线', b'0', 20, 'single', '一次可以爬 1 或 2 阶，爬到第 n 阶的方案数，本质上接近哪类问题？', '斐波那契数列', '动态规划最常见的转化之一。', NULL),
(7, 'algo', 7, '课程表问题', b'0', 25, 'single', 'Kadane 算法求最大子数组和的时间复杂度是？', 'O(n)', '理解“以当前位置结尾”的状态定义很关键。', NULL),
(8, 'algo', 8, '单词接龙', b'0', 30, 'single', '0-1 背包的经典 DP 状态转移，时间复杂度通常写作？', 'O(nW)', 'n 是物品数，W 是背包容量。', NULL),
(9, 'algo', 9, '初识带权图', b'0', 30, 'code', '给定 nums 和 target，返回两数之和对应的下标。要求时间复杂度 O(n)。', '用哈希表记录已经遍历过的数字与下标，边遍历边判断 target - nums[i] 是否出现过。', '把思路写成清晰的代码实现。', NULL),
(10, 'algo', 10, '最短路径与回溯', b'1', 25, 'code', '给定一个大小为 rows x cols 的二维高度矩阵 heights，你从左上角 (0, 0) 出发，目标是到达右下角 (rows-1, cols-1)。你每次可以上下左右移动。一条路径的“体力消耗”是该路径上相邻格子之间高度差绝对值的最大值。请实现一个函数，返回到达目标的最小体力消耗值，以及走过这条路径的完整坐标连线数组。', '使用优先队列（最小堆），队列元素维护 (当前最大落差, 行坐标, 列坐标, 当前累积的路径列表)。向四个方向扩展时，新落差等于 max(当前最大落差, 两个格子高度差的绝对值)。因为是最小堆，第一次弹出的右下角坐标就是最优解，直接返回对应记录的路径即可。', '2D矩阵上的变种 Dijkstra 寻路，寻找最大高度差最小的最优路径，并回溯打印完整移动路线。', NULL),
(11, 'algo', 11, '最短路径可视化器核心机制', b'1', 25, 'code', '你需要实现一个核心寻路引擎。给定带权无向图的节点数 n、边列表 edges，以及起点 start 和终点 end，请实现 Dijkstra 算法计算最短距离，并返回完整的访问节点顺序和最后的最短路径。注意：不仅要算出最短距离，还必须记录算法从优先队列中取出节点的“访问顺序”，以供前端实现动画过程。', '使用优先队列（最小堆）维护当前最短距离，同时用 visited 数组严格记录出队先后顺序，最后通过 parent 数组回溯生成从起点到终点的完整 path，一并返回。', '输入地图节点后动态展示 BFS/Dijkstra 搜索过程，适合作为图算法实战入门。', NULL),
(12, 'ds', 1, '数组基础', b'1', 10, 'single', '数组支持按下标随机访问，平均时间复杂度是？', 'O(1)', '数据结构基础题。', NULL),
(13, 'ds', 2, '链表插入', b'1', 10, 'single', '在单链表头部插入一个节点，时间复杂度通常是？', 'O(1)', '理解指针改动即可。', NULL),
(14, 'ds', 3, '栈的特性', b'0', 15, 'single', '栈最典型的访问顺序是？', '后进先出', 'LIFO 是核心关键词。', NULL),
(15, 'ds', 4, '队列的特性', b'0', 15, 'single', '队列最典型的访问顺序是？', '先进先出', 'FIFO 是核心关键词。', NULL),
(16, 'ds', 5, '二叉树遍历', b'0', 20, 'single', '二叉树前序遍历的顺序是？', '根左右', '先访问根，再递归左右子树。', NULL),
(17, 'ds', 6, '堆的性质', b'0', 25, 'single', '最大堆的堆顶元素满足什么性质？', '是当前堆中的最大值', '优先队列的常见底层结构。', NULL),
(18, 'ds', 7, '哈希表查询', b'0', 20, 'single', '哈希表平均查询时间复杂度通常是？', 'O(1)', '别忘了这是平均复杂度。', NULL),
(19, 'ds', 8, '平衡二叉树', b'0', 30, 'single', 'AVL 树保持平衡的关键条件通常描述为？', '左右子树高度差不超过 1', '理解“平衡”比记名词更重要。', NULL),
(20, 'ds', 9, '实现链表反转', b'0', 30, 'code', '反转单链表并返回新的头节点。要求额外空间复杂度 O(1)。', '使用 pre、cur、next 三个指针原地迭代反转。', '基础但很考察指针细节。', NULL),
(21, 'ds', 10, '实现前序遍历', b'0', 25, 'code', '返回二叉树的前序遍历结果，可以使用递归或显式栈。', '前序顺序为根、左、右；递归或栈模拟都可以。', '适合巩固树和栈的结合。', NULL),
(22, 'contest', 1, '滑动窗口', b'1', 15, 'single', '滑动窗口最适合解决哪类问题？', '连续子数组或子串问题', '看到“连续”通常就要敏感起来。', NULL),
(23, 'contest', 2, '前缀和', b'0', 15, 'single', '前缀和最常用于优化哪类计算？', '区间求和', '预处理后快速回答查询。', NULL),
(24, 'contest', 3, '单调栈', b'0', 20, 'single', '单调栈常见应用是？', '寻找下一个更大元素', '理解“维护单调性”比背题名更重要。', NULL),
(25, 'contest', 4, '并查集', b'0', 25, 'single', '并查集最擅长处理哪类问题？', '连通性', '动态合并集合时非常好用。', NULL),
(26, 'contest', 5, '线段树', b'0', 30, 'single', '线段树最常用于支持什么操作？', '区间查询与区间更新', '竞赛中非常常见的区间数据结构。', NULL),
(27, 'contest', 6, '字典树', b'0', 25, 'single', '字典树最适合处理什么场景？', '字符串前缀查询', '尤其适合批量单词前缀检索。', NULL),
(28, 'contest', 7, '状态压缩 DP', b'0', 35, 'single', '状态压缩 DP 通常使用什么表示状态？', '二进制位掩码', '当状态数量不大但组合很多时很常见。', NULL),
(29, 'contest', 8, '数位 DP', b'0', 35, 'single', '数位 DP 常用于解决哪类问题？', '满足条件的数字统计', '通常会和“按位枚举”一起出现。', NULL),
(30, 'contest', 9, '实现最长无重复子串', b'0', 35, 'code', '给定字符串 s，返回不含重复字符的最长子串长度。要求时间复杂度 O(n)。', '用滑动窗口维护当前无重复区间，并记录字符最近出现位置。', '竞赛与面试都很高频。', NULL),
(31, 'contest', 10, '实现三数之和', b'0', 40, 'code', '给定数组 nums，返回所有和为 0 的不重复三元组。', '先排序，再固定一个数，剩余部分使用双指针去重查找。', '双指针综合题。', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_level_options`
--

CREATE TABLE `t_level_options` (
  `level_id` bigint(20) NOT NULL,
  `options` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_level_options`
--

INSERT INTO `t_level_options` (`level_id`, `options`) VALUES
(1, '哈希表'),
(1, '暴力枚举'),
(1, '二分查找'),
(1, '二分查找'),
(2, 'O(n)'),
(2, 'O(n²)'),
(2, 'O(log n)'),
(2, 'O(n log n)'),
(3, 'O(1)'),
(3, 'O(n)'),
(3, 'O(log n)'),
(3, 'O(n²)'),
(4, 'O(1)'),
(4, 'O(n)'),
(4, 'O(n²)'),
(4, 'O(2^n)'),
(1, '2'),
(1, '1'),
(1, '1');

-- --------------------------------------------------------

--
-- 表的结构 `t_question_attempt`
--

CREATE TABLE `t_question_attempt` (
  `id` bigint(20) NOT NULL,
  `first_submitted_at` datetime(6) DEFAULT NULL,
  `last_submitted_at` datetime(6) DEFAULT NULL,
  `latest_status` varchar(20) NOT NULL,
  `latest_user_answer` longtext DEFAULT NULL,
  `level_id` bigint(20) NOT NULL,
  `level_type` varchar(50) DEFAULT NULL,
  `question_content` longtext DEFAULT NULL,
  `question_title` varchar(200) DEFAULT NULL,
  `submit_count` int(11) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_question_attempt`
--

INSERT INTO `t_question_attempt` (`id`, `first_submitted_at`, `last_submitted_at`, `latest_status`, `latest_user_answer`, `level_id`, `level_type`, `question_content`, `question_title`, `submit_count`, `user_id`) VALUES
(1, '2026-04-08 11:05:21.000000', '2026-04-10 08:00:06.000000', 'WRONG', 'O(n²)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 6, 4),
(2, '2026-04-08 11:06:33.000000', '2026-04-08 11:06:33.000000', 'CORRECT', '用哈希表记录已经遍历过的数字与下标，边遍历边判断 target - nums[i] 是否出现过。', 9, 'code', '给定 nums 和 target，返回两数之和对应的下标。要求时间复杂度 O(n)。', '实现两数之和', 1, 4),
(3, '2026-04-08 11:22:28.000000', '2026-04-10 07:17:56.000000', 'CORRECT', '哈希表', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 4, 4),
(4, '2026-04-08 11:24:02.000000', '2026-04-10 06:31:09.000000', 'WRONG', 'O(n²)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 5, 4),
(5, '2026-04-08 11:29:42.000000', '2026-04-09 10:21:27.000000', 'CORRECT', '使用 while (left <= right) 的写法，注意 mid 和左右边界更新。', 10, 'code', '在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。', '实现二分查找', 5, 4),
(6, '2026-04-08 13:53:16.000000', '2026-04-08 13:53:16.000000', 'CORRECT', 'O(log n)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 1, 6),
(7, '2026-04-08 13:53:23.000000', '2026-04-08 13:53:23.000000', 'WRONG', '0', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 1, 6),
(8, '2026-04-08 13:53:29.000000', '2026-04-08 13:53:29.000000', 'WRONG', 'O(n²)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 1, 6),
(9, '2026-04-08 13:53:35.000000', '2026-04-08 13:53:35.000000', 'WRONG', 'O(2^n)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 6),
(10, '2026-04-08 13:55:53.000000', '2026-04-08 13:55:53.000000', 'CORRECT', '维护前两项或 DP 数组，自底向上迭代计算即可。', 11, 'code', '实现一个函数，返回斐波那契数列第 n 项。要求使用迭代或 DP 思路。', '实现斐波那契数列', 1, 6),
(11, '2026-04-10 06:31:18.000000', '2026-04-10 06:31:18.000000', 'CORRECT', 'O(n)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 4),
(12, '2026-04-10 08:06:58.000000', '2026-04-10 08:15:17.000000', 'CORRECT', '哈希表', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 5, 1),
(13, '2026-04-10 08:07:48.000000', '2026-04-13 13:42:16.000000', 'CORRECT', 'O(log n)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 4, 1),
(14, '2026-04-10 08:12:19.000000', '2026-04-10 12:23:25.000000', 'WRONG', 'O(n)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 5, 1),
(15, '2026-04-10 11:25:08.000000', '2026-04-14 08:06:23.000000', 'WRONG', 'import heapq\n\ndef minEffort(h):\n    R, C = len(h), len(h[0])\n    # 队列: (当前最大落差, 行, 列, 走过的路径)\n    pq = [(0, 0, 0, [(0, 0)])]\n    seen = set()\n\n    while pq:\n        e, r, c, path = heapq.heappop(pq)\n        print(f\"到达: ({r},{c}) | 当前最大体力消耗: {e}\")\n        \n        if (r, c) == (R - 1, C - 1): \n            return e, path  # 到达右下角终点，直接返回\n            \n        if (r, c) in seen: continue\n        seen.add((r, c))\n\n        # 遍历上下左右邻居\n        for nr, nc in ((r+1, c), (r-1, c), (r, c+1), (r, c-1)):\n            if 0 <= nr < R and 0 <= nc < C and (nr, nc) not in seen:\n                ne = max(e, abs(h[r][c] - h[nr][nc]))\n                heapq.heappush(pq, (ne, nr, nc, path + [(nr, nc)]))\n\n# ========================\n# 运行测试\n# ========================\ngrid = [\n    [1, 2, 2],\n    [3, 8, 2],\n    [5, 3, 5]\n]\neffort, full_path = minEffort(grid)\nprint(f\"\\n最终结果 -> 消耗: {effort}, 路线: {full_path}\")', 10, 'code', '给定一个大小为 rows x cols 的二维高度矩阵 heights，你从左上角 (0, 0) 出发，目标是到达右下角 (rows-1, cols-1)。你每次可以上下左右移动。一条路径的“体力消耗”是该路径上相邻格子之间高度差绝对值的最大值。请实现一个函数，返回到达目标的最小体力消耗值，以及走过这条路径的完整坐标连线数组。', '最短路径与回溯', 28, 1),
(16, '2026-04-10 12:23:30.000000', '2026-04-10 12:23:30.000000', 'WRONG', 'O(1)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 1),
(17, '2026-04-10 15:11:36.000000', '2026-04-10 15:11:53.000000', 'CORRECT', '哈希表', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 2, 8),
(18, '2026-04-10 15:12:14.000000', '2026-04-10 15:12:14.000000', 'CORRECT', 'O(log n)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 1, 8),
(19, '2026-04-10 15:12:28.000000', '2026-04-10 15:13:32.000000', 'WRONG', 'O(n²)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 9, 8),
(20, '2026-04-10 15:14:16.000000', '2026-04-10 15:14:16.000000', 'CORRECT', 'O(n)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 8);

-- --------------------------------------------------------

--
-- 表的结构 `t_user`
--

CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `points` int(11) DEFAULT 0,
  `target_track` varchar(255) DEFAULT NULL,
  `weekly_goal` int(11) DEFAULT 10,
  `avatar` varchar(500) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `github` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `status_emoji` varchar(255) DEFAULT NULL,
  `status_mood` varchar(255) DEFAULT NULL,
  `is_busy` bit(1) DEFAULT NULL,
  `busy_auto_reply` varchar(255) DEFAULT NULL,
  `busy_end_time` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `author_level_code` varchar(255) DEFAULT NULL,
  `author_level_updated_at` datetime(6) DEFAULT NULL,
  `author_score` int(11) DEFAULT NULL,
  `is_admin` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_user`
--

INSERT INTO `t_user` (`id`, `name`, `email`, `password`, `points`, `target_track`, `weekly_goal`, `avatar`, `bio`, `gender`, `github`, `website`, `status_emoji`, `status_mood`, `is_busy`, `busy_auto_reply`, `busy_end_time`, `created_at`, `updated_at`, `author_level_code`, `author_level_updated_at`, `author_score`, `is_admin`) VALUES
(1, 'aiaod', 'admin@example.com', '$2a$10$O8UQPqD1iNfnQNvMIl9Mn.rmZ/jMadkqu/wabolilPPgRwdn3hCXS', 210, 'algo', 10, '/api/uploads/avatars/avatar_1_a8d05152.jpg', 'Hello World', 'female', '', 'https://www.usloa.cn/profile', '🎯', '学习中', b'0', NULL, NULL, '2026-04-06 16:17:02', '2026-04-21 22:28:01', 'seed', '2026-04-19 09:05:45.000000', 79, b'1'),
(2, 'Molic', '898@196.com', '$2a$10$50t0ZfYYx3p8eeZeZtfvTeEAQzuWLnHU5KJFg/hIdCNzzuOeTl8hW', 135, 'algo', 10, '/api/uploads/avatars/avatar_6_78be5ceb.jpg', 'Keep a steady pace.', 'unknown', '', '', NULL, NULL, NULL, NULL, NULL, '2026-04-08 12:39:50', '2026-04-10 15:54:21', 'seed', '2026-04-17 11:26:41.000000', 19, NULL),
(3, '只想睡大觉', 'test@example.com', '$2a$10$/9ry2L0W1zsQcCJuyqPRx.D3QSDkEbnQPmOv0AaebEVMJYldesora', 100, 'algo', 10, '/api/uploads/avatars/avatar_1_e4ae7c25.jpg', 'Keep a steady pace.', 'unknown', '', '', '🎯', '学习中', b'0', NULL, NULL, '2026-04-06 16:11:39', '2026-04-10 15:54:21', 'seed', '2026-04-17 09:39:55.000000', 0, NULL),
(4, '一只爱刷题的熊', 'aaa@qq.com', '$2a$10$FUsFAQLstBkWey5WqPQCPu1hYUu5P17bJz7lhHraMXeCW7iI8a/9q', 20, 'algo', 10, '/api/uploads/avatars/avatar_5_cc9712a4.jpg', 'Keep a steady pace.', 'unknown', '', '', NULL, NULL, NULL, NULL, NULL, '2026-04-08 05:17:48', '2026-04-10 15:54:21', 'seed', '2026-04-14 04:11:42.000000', 105, NULL),
(5, 'Day', '3630714512@qq.com', '$2a$10$qUvVM9k6mRdid84OCphF..AcBix2HF.e3ZfsEWcVAjU0GnUpVLF.W', 120, 'algo', 10, '/api/uploads/avatars/avatar_7_c613d00f.jpg', '吃漂亮饭，睡觉', 'unknown', '', '', NULL, NULL, NULL, NULL, NULL, '2026-04-10 00:18:05', '2026-04-10 15:54:21', 'seed', '2026-04-14 10:42:06.000000', 0, NULL),
(8, 'cqx', '110@qq.com', '$2a$10$HU4oxhd0yI8K2vqZ0ghr/uQZE6nw83hFgTwTA4dAKqUToea/F.8pa', 35, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-10 15:10:37', '2026-04-10 15:10:37', NULL, NULL, NULL, NULL),
(9, 'CodeSnapTester', 'test_codesnap@test.com', '$2a$10$z1hQoD.grlCTM8ODpfc/0OCTcs04nHJ82ZWFyqGjcqyxSzSrpkNgO', 0, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-13 20:46:52', '2026-04-13 20:46:52', 'seed', '2026-04-13 20:46:52.000000', 0, NULL),
(10, 'ohyf', '255775@qq.com', '$2a$10$vRN0rzVjFgSlotg1lZ5Suuf9Dw2L77O0Y0vTfBKUd8Od8Ehv3bXPy', 0, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-15 06:01:23', '2026-04-15 06:01:23', 'seed', '2026-04-15 06:01:23.000000', 0, NULL),
(11, '啫啫煲', '3685443400@qq.com', '$2a$10$EpxdL7NHqED81Hz4dzL17eF4gGS6AByF2vqLDqD3dTwYelQ65g1JG', 0, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-17 08:02:20', '2026-04-17 08:02:20', 'seed', '2026-04-17 08:02:20.000000', 0, NULL),
(12, '大鸡巴哥', '9178666@鸡扒.com', '$2a$10$ZdlLnmMp2LM/Av6Ylh0m1urU.GioJ6e1CZ.VFij6N7rVIksXMUpfi', 0, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-17 09:59:43', '2026-04-17 09:59:43', 'seed', '2026-04-17 10:03:49.000000', 18, NULL),
(13, 'hamoo', '1341427526@qq.com', '$2a$10$ZIpXpb.GoCNOCsg1S9eNQ.2dvY9Yt6S6Bn0i7/fMNymO4vl50W.nW', 0, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-18 06:49:27', '2026-04-18 06:49:27', 'seed', '2026-04-18 06:49:27.000000', 0, NULL),
(14, 'CC', '1@33.com', '$2a$10$4eSJu.j/9TEswWAazisoYuznQG0.3QaNfudJXJ92LUCv.Th.hdfci', 0, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-18 21:01:16', '2026-04-18 21:01:16', 'seed', '2026-04-18 21:01:16.000000', 0, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_user_author_level_history`
--

CREATE TABLE `t_user_author_level_history` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `current_level_code` varchar(32) NOT NULL,
  `current_score` int(11) NOT NULL,
  `metrics_json` text DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `previous_level_code` varchar(32) DEFAULT NULL,
  `previous_score` int(11) NOT NULL,
  `trigger_ref_id` bigint(20) DEFAULT NULL,
  `trigger_type` varchar(50) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_user_author_level_history`
--

INSERT INTO `t_user_author_level_history` (`id`, `created_at`, `current_level_code`, `current_score`, `metrics_json`, `note`, `previous_level_code`, `previous_score`, `trigger_ref_id`, `trigger_type`, `user_id`) VALUES
(1, '2026-04-10 11:09:38.000000', 'seed', 70, '{\"total\":70,\"solving\":{\"label\":\"做题成长\",\"score\":52,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":2,\"firstTrySolved\":0,\"accuracyRate\":45,\"totalAttempts\":11}},\"posting\":{\"label\":\"发帖影响力\",\"score\":18,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":2,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":70}', '登录刷新作者等级', 'seed', 0, NULL, 'LOGIN', 1),
(2, '2026-04-14 04:05:45.000000', 'seed', 78, '{\"total\":78,\"solving\":{\"label\":\"做题成长\",\"score\":30,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":2,\"firstTrySolved\":0,\"accuracyRate\":18,\"totalAttempts\":34}},\"posting\":{\"label\":\"发帖影响力\",\"score\":48,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":3,\"receivedLikes\":1,\"receivedComments\":2,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":78}', '发布帖子', 'seed', 70, 7, 'FORUM_POST_CREATED', 1),
(3, '2026-04-14 04:09:38.000000', 'seed', 16, '{\"total\":16,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":16,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":0,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":16}', '帖子收到点赞', 'seed', 0, 2, 'FORUM_POST_LIKED', 2),
(4, '2026-04-14 04:11:42.000000', 'seed', 105, '{\"total\":105,\"solving\":{\"label\":\"做题成长\",\"score\":105,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":4,\"firstTrySolved\":2,\"accuracyRate\":67,\"totalAttempts\":3}},\"posting\":{\"label\":\"发帖影响力\",\"score\":0,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":0,\"receivedLikes\":0,\"receivedComments\":0,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":105}', '登录刷新作者等级', 'seed', 0, NULL, 'LOGIN', 4),
(5, '2026-04-14 04:35:17.000000', 'seed', 17, '{\"total\":17,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":17,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":1,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":17}', '帖子收到评论', 'seed', 16, 9, 'FORUM_POST_COMMENTED', 2),
(6, '2026-04-14 10:42:29.000000', 'seed', 18, '{\"total\":18,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":18,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":2,\"receivedComments\":1,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":18}', '帖子收到点赞', 'seed', 17, 9, 'FORUM_POST_LIKED', 2),
(7, '2026-04-17 10:03:33.000000', 'seed', 15, '{\"total\":15,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":15,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":0,\"receivedComments\":0,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":15}', '发布帖子', 'seed', 0, 10, 'FORUM_POST_CREATED', 12),
(8, '2026-04-17 10:03:37.000000', 'seed', 16, '{\"total\":16,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":16,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":0,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":16}', '帖子收到点赞', 'seed', 15, 10, 'FORUM_POST_LIKED', 12),
(9, '2026-04-17 10:03:45.000000', 'seed', 17, '{\"total\":17,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":17,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":1,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":17}', '帖子收到评论', 'seed', 16, 10, 'FORUM_POST_COMMENTED', 12),
(10, '2026-04-17 10:03:49.000000', 'seed', 18, '{\"total\":18,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":18,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":2,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":18}', '帖子收到评论', 'seed', 17, 10, 'FORUM_POST_COMMENTED', 12),
(11, '2026-04-17 11:26:41.000000', 'seed', 19, '{\"total\":19,\"solving\":{\"label\":\"做题成长\",\"score\":0,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":0,\"firstTrySolved\":0,\"accuracyRate\":0,\"totalAttempts\":0}},\"posting\":{\"label\":\"发帖影响力\",\"score\":19,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":2,\"receivedComments\":2,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":19}', '帖子收到评论', 'seed', 18, 9, 'FORUM_POST_COMMENTED', 2),
(12, '2026-04-19 09:05:45.000000', 'seed', 79, '{\"total\":79,\"solving\":{\"label\":\"做题成长\",\"score\":27,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":2,\"firstTrySolved\":0,\"accuracyRate\":14,\"totalAttempts\":42}},\"posting\":{\"label\":\"发帖影响力\",\"score\":52,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":3,\"receivedLikes\":5,\"receivedComments\":3,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":79}', '帖子收到点赞', 'seed', 78, 12, 'FORUM_POST_LIKED', 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_user_follow`
--

CREATE TABLE `t_user_follow` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `follower_id` bigint(20) NOT NULL,
  `following_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `t_user_post_like`
--

CREATE TABLE `t_user_post_like` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_user_post_like`
--

INSERT INTO `t_user_post_like` (`id`, `created_at`, `post_id`, `user_id`) VALUES
(20, '2026-04-10 09:19:29.000000', 4, 2),
(21, '2026-04-10 09:20:58.000000', 5, 2),
(36, '2026-04-10 13:14:32.000000', 5, 1),
(39, '2026-04-14 04:09:36.000000', 2, 1),
(41, '2026-04-14 04:14:29.000000', 3, 4),
(44, '2026-04-14 10:42:28.000000', 9, 5),
(45, '2026-04-14 10:42:31.000000', 3, 5),
(51, '2026-04-17 10:03:38.000000', 10, 12),
(52, '2026-04-17 10:06:40.000000', 11, 12),
(54, '2026-04-18 20:48:36.000000', 3, 1),
(56, '2026-04-19 09:05:52.000000', 9, 1),
(57, '2026-04-21 07:28:52.000000', 12, 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_user_problem_record`
--

CREATE TABLE `t_user_problem_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `level_id` bigint(20) NOT NULL COMMENT '关卡ID',
  `level_name` varchar(200) DEFAULT NULL COMMENT '关卡名称',
  `track` varchar(20) DEFAULT NULL COMMENT '赛道',
  `is_correct` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否答对',
  `status` varchar(20) DEFAULT NULL COMMENT '作答状态',
  `stars` int(11) NOT NULL DEFAULT 0 COMMENT '星级',
  `attempt_no` int(11) NOT NULL DEFAULT 1 COMMENT '第几次尝试',
  `solve_time_ms` int(11) DEFAULT NULL COMMENT '耗时毫秒',
  `solved_at` datetime NOT NULL COMMENT '解题时间',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户题目答题记录表';

--
-- 转存表中的数据 `t_user_problem_record`
--

INSERT INTO `t_user_problem_record` (`id`, `user_id`, `level_id`, `level_name`, `track`, `is_correct`, `status`, `stars`, `attempt_no`, `solve_time_ms`, `solved_at`, `created_at`) VALUES
(1, 4, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 4, 4155, '2026-04-10 07:17:56', '2026-04-10 07:17:56'),
(2, 4, 2, '二分查找', 'algo', 1, 'CORRECT', 1, 5, 3059, '2026-04-10 07:21:11', '2026-04-10 07:21:11'),
(3, 4, 2, '二分查找', 'algo', 0, 'WRONG', 0, 6, 3337, '2026-04-10 08:00:06', '2026-04-10 08:00:06'),
(4, 1, 1, '两数之和', 'algo', 0, 'WRONG', 0, 1, 1762, '2026-04-10 08:06:58', '2026-04-10 08:06:58'),
(5, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 2, 1883, '2026-04-10 08:07:34', '2026-04-10 08:07:34'),
(6, 1, 2, '二分查找', 'algo', 0, 'WRONG', 0, 1, 5634, '2026-04-10 08:07:48', '2026-04-10 08:07:48'),
(7, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 3, 2550, '2026-04-10 08:08:10', '2026-04-10 08:08:10'),
(8, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 4, 2236, '2026-04-10 08:11:09', '2026-04-10 08:11:09'),
(9, 1, 2, '二分查找', 'algo', 1, 'CORRECT', 1, 2, 2178, '2026-04-10 08:12:13', '2026-04-10 08:12:14'),
(10, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 1, 1871, '2026-04-10 08:12:19', '2026-04-10 08:12:19'),
(11, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 2, 5251, '2026-04-10 08:12:22', '2026-04-10 08:12:22'),
(12, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 3, 8581, '2026-04-10 08:12:25', '2026-04-10 08:12:25'),
(13, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 4, 12324, '2026-04-10 08:12:29', '2026-04-10 08:12:29'),
(14, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 5, 2315, '2026-04-10 08:15:17', '2026-04-10 08:15:17'),
(15, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 1, 33360, '2026-04-10 11:25:08', '2026-04-10 11:25:08'),
(16, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 2, 49240, '2026-04-10 11:26:39', '2026-04-10 11:26:39'),
(17, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 3, 38996, '2026-04-10 11:29:23', '2026-04-10 11:29:23'),
(18, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 4, 232264, '2026-04-10 11:32:36', '2026-04-10 11:32:36'),
(19, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 5, 2072, '2026-04-10 12:23:25', '2026-04-10 12:23:25'),
(20, 1, 4, '归并排序', 'algo', 0, 'WRONG', 0, 1, 1536, '2026-04-10 12:23:30', '2026-04-10 12:23:30'),
(21, 1, 2, '二分查找', 'algo', 1, 'CORRECT', 3, 3, 8270, '2026-04-10 12:39:28', '2026-04-10 12:39:28'),
(22, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 5, 58803, '2026-04-10 12:44:34', '2026-04-10 12:44:34'),
(23, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 6, 77967, '2026-04-10 13:12:28', '2026-04-10 13:12:28'),
(24, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 7, 35350, '2026-04-11 16:47:59', '2026-04-11 16:47:59'),
(25, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 8, 39403, '2026-04-11 17:55:30', '2026-04-11 17:55:30'),
(26, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 9, 37564, '2026-04-12 11:21:46', '2026-04-12 11:21:46'),
(27, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 10, 70853, '2026-04-12 11:22:19', '2026-04-12 11:22:19'),
(28, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 11, 98804, '2026-04-12 11:22:47', '2026-04-12 11:22:47'),
(29, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 12, 122994, '2026-04-12 11:23:11', '2026-04-12 11:23:11'),
(30, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 13, 52635, '2026-04-12 11:29:06', '2026-04-12 11:29:06'),
(31, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 14, 138456, '2026-04-12 11:30:32', '2026-04-12 11:30:32'),
(32, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 15, 52669, '2026-04-12 11:52:30', '2026-04-12 11:52:30'),
(33, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 16, 455193, '2026-04-12 11:59:13', '2026-04-12 11:59:13'),
(34, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 17, 67401, '2026-04-12 12:18:20', '2026-04-12 12:18:20'),
(35, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 18, 141007, '2026-04-12 12:19:34', '2026-04-12 12:19:34'),
(36, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 19, 47036, '2026-04-13 11:46:00', '2026-04-13 11:46:00'),
(37, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 20, 57398, '2026-04-13 11:53:18', '2026-04-13 11:53:18'),
(39, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 21, 538863, '2026-04-14 05:55:28', '2026-04-14 05:55:28'),
(42, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 22, 320118, '2026-04-14 06:26:36', '2026-04-14 06:26:36'),
(44, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 23, 529897, '2026-04-14 07:04:18', '2026-04-14 07:04:18'),
(45, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 24, 832841, '2026-04-14 07:09:21', '2026-04-14 07:09:21'),
(46, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 25, 1276864, '2026-04-14 07:16:45', '2026-04-14 07:16:45'),
(47, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 26, 115313, '2026-04-14 07:26:39', '2026-04-14 07:26:39'),
(48, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 27, 131944, '2026-04-14 07:27:33', '2026-04-14 07:27:33'),
(49, 1, 10, '最短路径与回溯', 'algo', 0, 'WRONG', 0, 28, 36029, '2026-04-14 08:06:23', '2026-04-14 08:06:23');

--
-- 转储表的索引
--

--
-- 表的索引 `course_tags`
--
ALTER TABLE `course_tags`
  ADD KEY `FKp5efy36nmeh0845xdrrqh1gex` (`course_id`);

--
-- 表的索引 `level_options`
--
ALTER TABLE `level_options`
  ADD KEY `FK6hl9xqxytss729ge4t024jnxa` (`level_id`);

--
-- 表的索引 `t_ai_analysis_usage`
--
ALTER TABLE `t_ai_analysis_usage`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_ai_analysis_usage_user_date_slot` (`user_id`,`quota_date`,`slot_code`),
  ADD KEY `idx_ai_analysis_usage_user_date` (`user_id`,`quota_date`),
  ADD KEY `idx_ai_analysis_usage_user_time` (`user_id`,`created_at`);

--
-- 表的索引 `t_code_snapshot`
--
ALTER TABLE `t_code_snapshot`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_code_snapshot_user` (`user_id`),
  ADD KEY `idx_code_snapshot_level` (`level_id`),
  ADD KEY `idx_code_snapshot_user_level` (`user_id`,`level_id`),
  ADD KEY `idx_code_snapshot_saved_at` (`user_id`,`saved_at`);

--
-- 表的索引 `t_completed_error_item`
--
ALTER TABLE `t_completed_error_item`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_completed_error_user_completed` (`user_id`,`completed_at`),
  ADD KEY `idx_completed_error_user_level` (`user_id`,`level_id`);

--
-- 表的索引 `t_course`
--
ALTER TABLE `t_course`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_course_comment`
--
ALTER TABLE `t_course_comment`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_course_tags`
--
ALTER TABLE `t_course_tags`
  ADD KEY `idx_course_id` (`course_id`);

--
-- 表的索引 `t_error_item`
--
ALTER TABLE `t_error_item`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_error_item_option_snapshot`
--
ALTER TABLE `t_error_item_option_snapshot`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKgi48l2at6hq4qnkm19m7qayis` (`error_item_id`);

--
-- 表的索引 `t_forum_comment`
--
ALTER TABLE `t_forum_comment`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_forum_post`
--
ALTER TABLE `t_forum_post`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_knowledge_article`
--
ALTER TABLE `t_knowledge_article`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKroalqmi741ln62vx2alc982xm` (`slug`);

--
-- 表的索引 `t_knowledge_base_config`
--
ALTER TABLE `t_knowledge_base_config`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_learning_plan`
--
ALTER TABLE `t_learning_plan`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_level`
--
ALTER TABLE `t_level`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_level_options`
--
ALTER TABLE `t_level_options`
  ADD KEY `idx_level_id` (`level_id`);

--
-- 表的索引 `t_question_attempt`
--
ALTER TABLE `t_question_attempt`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_question_attempt_user_level` (`user_id`,`level_id`);

--
-- 表的索引 `t_user`
--
ALTER TABLE `t_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- 表的索引 `t_user_author_level_history`
--
ALTER TABLE `t_user_author_level_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_author_history_user_created` (`user_id`,`created_at`);

--
-- 表的索引 `t_user_follow`
--
ALTER TABLE `t_user_follow`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`),
  ADD KEY `idx_follow_following` (`following_id`);

--
-- 表的索引 `t_user_post_like`
--
ALTER TABLE `t_user_post_like`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKg1dtdes05y2ybooaaymtrilca` (`user_id`,`post_id`);

--
-- 表的索引 `t_user_problem_record`
--
ALTER TABLE `t_user_problem_record`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_solved_at` (`user_id`,`solved_at`),
  ADD KEY `idx_user_level` (`user_id`,`level_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `t_ai_analysis_usage`
--
ALTER TABLE `t_ai_analysis_usage`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- 使用表AUTO_INCREMENT `t_code_snapshot`
--
ALTER TABLE `t_code_snapshot`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- 使用表AUTO_INCREMENT `t_completed_error_item`
--
ALTER TABLE `t_completed_error_item`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `t_course`
--
ALTER TABLE `t_course`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `t_course_comment`
--
ALTER TABLE `t_course_comment`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `t_error_item`
--
ALTER TABLE `t_error_item`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- 使用表AUTO_INCREMENT `t_error_item_option_snapshot`
--
ALTER TABLE `t_error_item_option_snapshot`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=133;

--
-- 使用表AUTO_INCREMENT `t_forum_comment`
--
ALTER TABLE `t_forum_comment`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- 使用表AUTO_INCREMENT `t_forum_post`
--
ALTER TABLE `t_forum_post`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- 使用表AUTO_INCREMENT `t_knowledge_article`
--
ALTER TABLE `t_knowledge_article`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `t_knowledge_base_config`
--
ALTER TABLE `t_knowledge_base_config`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- 使用表AUTO_INCREMENT `t_learning_plan`
--
ALTER TABLE `t_learning_plan`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;

--
-- 使用表AUTO_INCREMENT `t_level`
--
ALTER TABLE `t_level`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- 使用表AUTO_INCREMENT `t_question_attempt`
--
ALTER TABLE `t_question_attempt`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- 使用表AUTO_INCREMENT `t_user`
--
ALTER TABLE `t_user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- 使用表AUTO_INCREMENT `t_user_author_level_history`
--
ALTER TABLE `t_user_author_level_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- 使用表AUTO_INCREMENT `t_user_follow`
--
ALTER TABLE `t_user_follow`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `t_user_post_like`
--
ALTER TABLE `t_user_post_like`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;

--
-- 使用表AUTO_INCREMENT `t_user_problem_record`
--
ALTER TABLE `t_user_problem_record`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID', AUTO_INCREMENT=50;

--
-- 限制导出的表
--

--
-- 限制表 `course_tags`
--
ALTER TABLE `course_tags`
  ADD CONSTRAINT `FKp5efy36nmeh0845xdrrqh1gex` FOREIGN KEY (`course_id`) REFERENCES `t_course` (`id`);

--
-- 限制表 `level_options`
--
ALTER TABLE `level_options`
  ADD CONSTRAINT `FK6hl9xqxytss729ge4t024jnxa` FOREIGN KEY (`level_id`) REFERENCES `t_level` (`id`);

--
-- 限制表 `t_error_item_option_snapshot`
--
ALTER TABLE `t_error_item_option_snapshot`
  ADD CONSTRAINT `FKgi48l2at6hq4qnkm19m7qayis` FOREIGN KEY (`error_item_id`) REFERENCES `t_error_item` (`id`);

--
-- 限制表 `t_level_options`
--
ALTER TABLE `t_level_options`
  ADD CONSTRAINT `FKhxjbea8688ipuri7evmadokr6` FOREIGN KEY (`level_id`) REFERENCES `t_level` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
