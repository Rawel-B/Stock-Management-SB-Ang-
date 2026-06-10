import { AbstractControl, ValidationErrors, Validators } from '@angular/forms';

export class AuthValidators {
  static readonly email = Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/);
  static readonly username = Validators.pattern(/^[a-zA-Z0-9._-]+$/);

  static notBlank(control: AbstractControl<string>): ValidationErrors | null {
    return control.value.trim().length ? null : { blank: true };
  }
}
