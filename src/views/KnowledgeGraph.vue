<template>
  <div class="knowledge-graph-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-badge">Knowledge Graph</div>
        <h1>算法知识图谱</h1>
        <p>可视化展示数据结构与算法的学习路径和知识关联</p>
      </div>
    </div>

    <div class="graph-wrapper">
      <AlgorithmKnowledgeGraph />
    </div>

    <div class="page-footer">
      <div class="footer-content">
        <div class="footer-stats">
          <div class="stat-item">
            <span class="stat-number">21</span>
            <span class="stat-label">知识节点</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">6</span>
            <span class="stat-label">知识分类</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">24</span>
            <span class="stat-label">学习路径</span>
          </div>
        </div>
        <div class="footer-hint">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 16v-4M12 8h.01" />
          </svg>
          点击节点查看详情 · 拖拽节点调整位置 · 滚轮缩放画布
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import AlgorithmKnowledgeGraph from '../components/AlgorithmKnowledgeGraph.vue'
</script>

<style scoped>
.knowledge-graph-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f7 100%);
  display: flex;
  flex-direction: column;
}

.page-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 28px 40px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}

.header-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border-radius: 6px;
  margin-bottom: 12px;
}

.header-content h1 {
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.header-content p {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.graph-wrapper {
  flex: 1;
  padding: 20px 40px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.graph-wrapper > * {
  height: calc(100vh - 260px);
  min-height: 520px;
}

.page-footer {
  background: white;
  border-top: 1px solid #e2e8f0;
  padding: 20px 40px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-number {
  font-size: 24px;
  font-weight: 800;
  color: #3b82f6;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #94a3b8;
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: #e2e8f0;
}

.footer-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px;
  }

  .header-content h1 {
    font-size: 22px;
  }

  .header-content p {
    font-size: 13px;
  }

  .graph-wrapper {
    padding: 12px;
  }

  .graph-wrapper > * {
    height: calc(100vh - 240px);
    min-height: 400px;
  }

  .page-footer {
    padding: 16px 20px;
  }

  .footer-content {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .footer-stats {
    gap: 16px;
  }

  .stat-number {
    font-size: 20px;
  }

  .footer-hint {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .graph-wrapper {
    padding: 8px;
  }

  .graph-wrapper > * {
    height: calc(100vh - 220px);
    min-height: 350px;
  }
}
</style>
