import { request } from '../utils/request'

const API_BASE_URL = ''

export const aiConsultationApi = {
  createSession: (data) => request(`${API_BASE_URL}/api/ai-consultation/session`, {
    method: 'POST',
    data,
  }),

  sendMessage: (data) => request(`${API_BASE_URL}/api/ai-consultation/message`, {
    method: 'POST',
    data,
  }),

  getSessionDetail: (sessionId) => request(`${API_BASE_URL}/api/ai-consultation/session/${sessionId}`),

  getSessionMessages: (sessionId, params) => request(`${API_BASE_URL}/api/ai-consultation/session/${sessionId}/messages`, {
    params,
  }),

  closeSession: (sessionId, data) => request(`${API_BASE_URL}/api/ai-consultation/session/${sessionId}/close`, {
    method: 'POST',
    data,
  }),

  getMySessions: (params) => request(`${API_BASE_URL}/api/ai-consultation/session/my`, {
    params,
  }),

  getSessionRisk: (sessionId) => request(`${API_BASE_URL}/api/ai-consultation/session/${sessionId}/risk`),

  uploadFile: (data) => request(`${API_BASE_URL}/api/ai-consultation/upload`, {
    method: 'POST',
    data,
    headers: {
      Accept: 'application/json',
    },
  }),
}
