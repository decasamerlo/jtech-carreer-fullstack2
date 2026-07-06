import api from './api'

export interface AuthApiResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
}

export async function loginApi(email: string, password: string): Promise<AuthApiResponse> {
  const { data } = await api.post<AuthApiResponse>('/api/v1/auth/login', {
    email,
    password,
  })
  return data
}

export async function registerApi(
  name: string,
  email: string,
  password: string,
): Promise<AuthApiResponse> {
  const { data } = await api.post<AuthApiResponse>('/api/v1/auth/register', {
    name,
    email,
    password,
  })
  return data
}
