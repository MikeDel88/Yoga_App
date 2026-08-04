import {Component, DestroyRef, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/service/auth.service';
import { RegisterRequest } from '../../core/models/registerRequest.interface';
import { MaterialModule } from "../../shared/material.module";
import { CommonModule } from "@angular/common";
import {FlexLayoutModule} from "@angular/flex-layout";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
@Component({
  selector: 'app-register',
  imports: [CommonModule, MaterialModule, FlexLayoutModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  private authService: AuthService = inject(AuthService);
  private fb: FormBuilder = inject(FormBuilder);
  private router: Router = inject(Router);
  public onError: boolean = false;
  private destroyRef: DestroyRef = inject(DestroyRef);

  public form: FormGroup = this.fb.group({
    email: [
      '',
      [
        Validators.required,
        Validators.email
      ]
    ],
    firstName: [
      '',
      [
        Validators.required,
        Validators.min(3),
        Validators.max(20)
      ]
    ],
    lastName: [
      '',
      [
        Validators.required,
        Validators.min(3),
        Validators.max(20)
      ]
    ],
    password: [
      '',
      [
        Validators.required,
        Validators.min(3),
        Validators.max(40)
      ]
    ]
  });


  public submit(): void {
    const registerRequest = this.form.value as RegisterRequest;
    this.authService.register(registerRequest)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (): Promise<boolean> => this.router.navigate(['/login']),
        error: (): boolean => this.onError = true,
      });
  }

}
