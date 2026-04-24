<template>
  <div class="knowledge-graph-page">
    <div class="page-header">
      <div class="header-content">
        <h1>算法知识图谱</h1>
        <p>可视化展示数据结构与算法的学习路径和知识关联</p>
      </div>
    </div>

    <div class="graph-wrapper">
      <AlgorithmKnowledgeGraph />
    </div>

    <div class="page-footer">
      <div class="footer-stats">
        <div class="stat-item">
          <span class="stat-number">21</span>
          <span class="stat-label">知识节点</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">6</span>
          <span class="stat-label">知识分类</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">28</span>
          <span class="stat-label">学习路径</span>
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
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  display: flex;
  flex-direction: column;
}

.page-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 32px 40px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}

.header-content h1 {
  font-size: 32px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.header-content p {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.graph-wrapper {
  flex: 1;
  padding: 24px 40px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.graph-wrapper > * {
  height: calc(100vh - 280px);
  min-height: 600px;
}

.page-footer {
  background: white;
  border-top: 1px solid #e2e8f0;
  padding: 24px 40px;
}

.footer-stats {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 48px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 28px;
  font-weight: 800;
  color: #3b82f6;
}

.stat-label {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

@media (max-width: 768px) {
  .page-header {
    padding: 24px 20px;
  }

  .header-content h1 {
    font-size: 24px;
  }

  .graph-wrapper {
    padding: 16px 20px;
  }

  .page-footer {
    padding: 20px;
  }

  .footer-stats {
    gap: 24px;
  }

  .stat-number {
    font-size: 22px;
  }
}
</style>
