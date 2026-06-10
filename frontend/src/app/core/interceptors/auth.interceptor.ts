import { HttpInterceptorFn } from '@angular/common/http';

const storageKey = 'dsm.auth';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const stored = localStorage.getItem(storageKey) ?? sessionStorage.getItem(storageKey);

  if (!stored) {
    return next(request);
  }

  try {
    const token = JSON.parse(stored).token;

    if (!token) {
      return next(request);
    }

    return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
  } catch {
    return next(request);
  }
};
