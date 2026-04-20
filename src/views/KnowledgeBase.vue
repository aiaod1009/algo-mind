<template>
  <div class="knowledge-base-page">
    <div class="page-header">
      <h1>知识库</h1>
      <p class="page-desc">浏览和学习各类编程知识</p>
    </div>
    <div class="knowledge-grid">
      <div class="knowledge-card">
        <div class="card-icon">📚</div>
        <h3>算法基础</h3>
        <p>学习常用算法思想和应用场景</p>
      </div>
      <div class="knowledge-card">
        <div class="card-icon">💻</div>
        <h3>数据结构</h3>
        <p>掌握常见数据结构和操作</p>
      </div>
      <div class="knowledge-card">
        <div class="card-icon">🔧</div>
        <h3>设计模式</h3>
        <p>了解常用设计模式及其实现</p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.knowledge-base-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.knowledge-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.knowledge-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-icon {
  font-size: 36px;
  margin-bottom: 16px;
}

.knowledge-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.knowledge-card p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}
</style>
