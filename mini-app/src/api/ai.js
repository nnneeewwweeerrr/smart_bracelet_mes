import { post, get } from '../utils/request'

export function aiChat(data) {
  return post('/ai/chat', data)
}

export function getRecentHistory(params) {
  return get('/ai/history/recent', params)
}

export function recognizeVoice(filePath) {
  return post('/ai/voice/recognize', { filePath })
}
