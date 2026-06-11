import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Auth } from '../../../core/services/auth';
import { AuthValidators } from '../../../core/validators/auth-validators';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss'
})
export class ForgotPassword {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(Auth);
  readonly loading = signal(false);
  readonly ticketLoading = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly ticketMessage = signal('');
  readonly supportOpen = signal(false);
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, AuthValidators.notBlank, AuthValidators.email, Validators.maxLength(120)]]
  });
  readonly ticketForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, AuthValidators.notBlank, AuthValidators.email, Validators.maxLength(120)]],
    category: ['access'],
    description: ['', [Validators.required, AuthValidators.notBlank, Validators.minLength(10)]]
  });

  submit() {
    this.error.set('');
    this.message.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.loading.set(true);
    this.auth.forgotPassword({ email: value.email.trim().toLowerCase() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: response => this.message.set(response.message),
        error: error => this.error.set(this.readError(error))
      });
  }

  createTicket() {
    this.ticketMessage.set('');

    if (this.ticketForm.invalid) {
      this.ticketForm.markAllAsTouched();
      return;
    }

    const value = this.ticketForm.getRawValue();
    this.ticketLoading.set(true);
    this.auth.createPublicSupportTicket({
      subject: 'Password Access',
      description: value.description.trim(),
      email: value.email.trim().toLowerCase(),
      category: value.category as 'access' | 'accountActivation'
    }).pipe(finalize(() => this.ticketLoading.set(false))).subscribe({
      next: () => {
        this.ticketMessage.set('Ticket sent.');
        this.ticketForm.reset();
      },
      error: error => this.ticketMessage.set(this.readError(error))
    });
  }

  private readError(error: unknown) {
    const response = error as { error?: { message?: string } };
    return response.error?.message ?? 'Unable to reset this password.';
  }
}
