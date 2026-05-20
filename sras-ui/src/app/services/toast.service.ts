import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class ToastService {
  constructor(private snackBar: MatSnackBar) {}

  success(message: string, duration = 3000): void {
    this.snackBar.open(message, 'Close', {
      duration,
      panelClass: ['toast-success'],
      verticalPosition: 'top',
      horizontalPosition: 'right'
    });
  }

  error(message: string, duration = 3000): void {
    this.snackBar.open(message, 'Close', {
      duration,
      panelClass: ['toast-error'],
      verticalPosition: 'top',
      horizontalPosition: 'right'
    });
  }

  info(message: string, duration = 3000): void {
    this.snackBar.open(message, 'Close', {
      duration,
      panelClass: ['toast-info'],
      verticalPosition: 'top',
      horizontalPosition: 'right'
    });
  }
}
