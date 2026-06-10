import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Auth } from '../../../core/services/auth';
import { AuthValidators } from '../../../core/validators/auth-validators';

@Component({
  selector: 'app-signup',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup.html',
  styleUrl: './signup.scss'
})
export class Signup {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, AuthValidators.notBlank, Validators.minLength(2), Validators.maxLength(80)]],
    email: ['', [Validators.required, AuthValidators.notBlank, AuthValidators.email, Validators.maxLength(120)]],
    username: ['', [Validators.required, AuthValidators.notBlank, Validators.minLength(3), Validators.maxLength(30), AuthValidators.username]],
    password: ['', [Validators.required, AuthValidators.notBlank, Validators.minLength(6), Validators.maxLength(72)]],
    remember: [true]
  });

  submit() {
    this.error.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.loading.set(true);
    this.auth.signUp({
      name: value.name.trim(),
      email: value.email.trim().toLowerCase(),
      username: value.username.trim(),
      password: value.password
    }, value.remember)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: error => this.error.set(this.readError(error))
      });
  }

  private readError(error: unknown) {
    const response = error as { error?: { message?: string } };
    return response.error?.message ?? 'Unable to create this account.';
  }
}
