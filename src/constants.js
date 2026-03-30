export const trackTextMap = {
  algo: '算法思维',
  ds: '数据结构',
  contest: '竞赛冲刺',
}

export const getTrackLabel = (trackCode) => {
  return trackTextMap[trackCode] || '算法思维'
}
