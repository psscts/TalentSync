export interface AuthResponse {
  token: string;
  email: string;
  role: 'EMPLOYEE' | 'PROJECT_MANAGER';
  userId: number;
}

export interface SignupRequest {
  email: string;
  password: string;
  role: 'EMPLOYEE' | 'PROJECT_MANAGER';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
