export interface SignInRequest {
  username: string;
  password: string;
}

export interface SignUpRequest {
  username: string;
  password: string;
  name: string;
  email: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ProfileRequest {
  username: string;
  name: string;
  email: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  name: string;
  email: string;
  role: string;
}

export interface MessageResponse {
  message: string;
}

export interface AuthSession extends AuthResponse {
  remembered: boolean;
}
