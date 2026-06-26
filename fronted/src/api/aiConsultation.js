import { request } from '../utils/request'


export const aiConsultationApi = {
  createSession: (data) => request('/api/ai-consultation/session', {
    method: 'POST',
    data,
  }),

  sendMessage: (data) => request('/api/ai-consultation/message', {
    method: 'POST',
    data,
  }),

  getSessionDetail: (sessionId) => request(`/api/ai-consultation/session/${sessionId}`),

  getSessionMessages: (sessionId, params) => request(`/api/ai-consultation/session/${sessionId}/messages`, {
    params,
  }),

  closeSession: (sessionId, data) => request(`/api/ai-consultation/session/${sessionId}/close`, {
    method: 'POST',
    data,
  }),

  getMySessions: (params) => request('/api/ai-consultation/session/my', {
    params,
  }),

  getAllSessions: (params) => request('/api/ai-consultation/session/all', {
    params,
  }),

  getSessionRisk: (sessionId) => request(`/api/ai-consultation/session/${sessionId}/risk`),

  uploadFile: (data) => request('/api/ai-consultation/upload', {
    method: 'POST',
    data,
    headers: {
      Accept: 'application/json',
    },
  }),
}
