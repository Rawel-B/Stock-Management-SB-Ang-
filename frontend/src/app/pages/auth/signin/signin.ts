import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Auth } from '../../../core/services/auth';
import { AuthValidators } from '../../../core/validators/auth-validators';

@Component({
  selector: 'app-signin',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signin.html',
  styleUrl: './signin.scss'
})
export class Signin {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly submitted = signal(false);
  readonly form = this.fb.nonNullable.group({
    username: [this.auth.rememberedUsername(), [Validators.required, AuthValidators.notBlank]],
    password: ['', [Validators.required, AuthValidators.notBlank]],
    remember: [true]
  });

  submit() {
    this.error.set('');
    this.submitted.set(true);

    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    this.loading.set(true);
    this.auth.signIn({ username: value.username.trim(), password: value.password }, value.remember)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: error => this.error.set(this.readError(error))
      });
  }

  private readError(error: unknown) {
    const response = error as { error?: { message?: string } };
    return response.error?.message ?? 'Unable to sign in with these credentials.';
  }
}
