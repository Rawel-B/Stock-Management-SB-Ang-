import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { Api } from './api';
import { AuthResponse, AuthSession, ForgotPasswordRequest, MessageResponse, ProfileRequest, SignInRequest, SignUpRequest } from '../models/auth';
import { PublicSupportTicketRequest, SupportTicketResponse } from '../models/workspace';

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  private readonly storageKey = 'dsm.auth';
  private readonly rememberedUsernameKey = 'dsm.rememberedUsername';
  private readonly session = signal<AuthSession | null>(null);
  readonly user = computed(() => this.session());
  readonly isAuthenticated = computed(() => !!this.session()?.token);

  constructor() {
    this.session.set(this.readSession());
  }

  signIn(request: SignInRequest, remember: boolean) {
    return this.api.post<AuthResponse>('/auth/signin', request).pipe(tap(response => this.saveSession(response, remember)));
  }

  signUp(request: SignUpRequest) {
    return this.api.post<MessageResponse>('/auth/signup', request);
  }

  forgotPassword(request: ForgotPasswordRequest) {
    return this.api.post<MessageResponse>('/auth/forgot-password', request);
  }

  createPublicSupportTicket(request: PublicSupportTicketRequest) {
    return this.api.post<SupportTicketResponse>('/support/tickets/public', request);
  }

  profile() {
    return this.api.get<AuthResponse>('/auth/me').pipe(tap(response => this.saveSession(response, this.session()?.remembered ?? true)));
  }

  updateProfile(request: ProfileRequest) {
    return this.api.put<AuthResponse>('/auth/me', request).pipe(tap(response => this.saveSession(response, this.session()?.remembered ?? true)));
  }

  token() {
    return this.session()?.token ?? null;
  }

  rememberedUsername() {
    return localStorage.getItem(this.rememberedUsernameKey) ?? '';
  }

  signOut() {
    localStorage.removeItem(this.storageKey);
    sessionStorage.removeItem(this.storageKey);
    this.session.set(null);
    this.router.navigate(['/auth/signin']);
  }

  private saveSession(response: AuthResponse, remember: boolean) {
    const value: AuthSession = { ...response, remembered: remember };
    const target = remember ? localStorage : sessionStorage;
    const fallback = remember ? sessionStorage : localStorage;
    fallback.removeItem(this.storageKey);
    target.setItem(this.storageKey, JSON.stringify(value));
    if (remember) {
      localStorage.setItem(this.rememberedUsernameKey, response.username);
    } else {
      localStorage.removeItem(this.rememberedUsernameKey);
    }
    this.session.set(value);
  }

  private readSession() {
    const stored = localStorage.getItem(this.storageKey) ?? sessionStorage.getItem(this.storageKey);

    if (!stored) {
      return null;
    }

    try {
      return JSON.parse(stored) as AuthSession;
    } catch {
      localStorage.removeItem(this.storageKey);
      sessionStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
