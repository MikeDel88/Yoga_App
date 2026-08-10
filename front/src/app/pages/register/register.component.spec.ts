import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';

import { RegisterComponent } from './register.component';
import { AuthService } from "../../core/service/auth.service";
import { SessionService } from "../../core/service/session.service";
import { HttpTestingController, provideHttpClientTesting, TestRequest } from "@angular/common/http/testing";
import { provideHttpClient } from "@angular/common/http";
import { provideRouter, Router } from '@angular/router';
import { routes } from "../../app.routes";
import { RegisterRequest } from "../../core/models/registerRequest.interface";
import { By } from '@angular/platform-browser';

/**
 * Plan de test
 * ● La création de compte
 * ● L’affichage d’erreur en l’absence d’un champ obligatoire
 *
 * Les tests sont classés en deux catégories :
 * - "Tests unitaires" : n'exercent que le FormGroup / le DOM du composant, sans passer par
 *   AuthService ou une vraie requête HTTP.
 * - "Tests d'intégration" : passent par le vrai flux du composant (AuthService réel + Router réel),
 *   avec seulement la couche HTTP simulée via HttpClientTesting.
 */
describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockRegister: RegisterRequest = {
    email: 'test@test.com',
    firstName: 'John',
    lastName: 'Doe',
    password: 'password'
  };

  const submitButton = () =>
    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement as HTMLButtonElement;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      declarations: [],
      providers: [
        SessionService,
        { provide: AuthService },
        { provide: HttpTestingController, useClass: HttpTestingController },
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
      imports: [
        RegisterComponent,
        BrowserAnimationsModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify(); // aucune requête HTTP en attente non vérifiée
  });

  /** Soumet le formulaire avec `registerRequest` et résout la requête HTTP avec succès. */
  function submitSuccessfulRegister(registerRequest: RegisterRequest = mockRegister) {
    const registerSpy = jest.spyOn(authService, 'register');
    const navigateSpy = jest.spyOn(router, 'navigate');

    component.form.setValue(registerRequest);
    component.submit();

    const req: TestRequest = httpMock.expectOne({ url: '/api/auth/register' });
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerRequest);
    req.flush(null);

    return { registerSpy, navigateSpy };
  }

  /** Soumet le formulaire avec `registerRequest` et résout la requête HTTP avec une erreur. */
  function submitFailedRegister(
    registerRequest: RegisterRequest = mockRegister,
    status: number = 400,
    statusText: string = 'Bad Request'
  ) {
    const registerSpy = jest.spyOn(authService, 'register');
    const navigateSpy = jest.spyOn(router, 'navigate');

    component.form.setValue(registerRequest);
    component.submit();

    const req: TestRequest = httpMock.expectOne({ url: '/api/auth/register' });
    req.flush({ message: statusText }, { status, statusText });

    return { registerSpy, navigateSpy };
  }

  describe('Tests unitaires (FormGroup / DOM, sans HTTP ni services)', () => {

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it.each([
      ['', false, 'required'],
      ['test@test.com', true, 'email'],
      ['not-an-email', false, 'email'],
    ])('email "%s" → valid=%s, error=%s', (value, valid, expectedError) => {
      const email = component.form.get('email');
      email?.setValue(value);

      expect(email?.valid).toBe(valid);
      expect(email?.hasError(expectedError as string)).toBe(!valid);
    });

    it.each([
      ['', false, 'required'],
      ['Jo', false, 'minlength'],
      ['John', true, 'minlength'],
      ['a'.repeat(21), false, 'maxlength'],
    ])('firstName "%s" → valid=%s, error=%s', (value, valid, expectedError) => {
      const firstName = component.form.get('firstName');
      firstName?.setValue(value);

      expect(firstName?.valid).toBe(valid);
      expect(firstName?.hasError(expectedError as string)).toBe(!valid);
    });

    it.each([
      ['', false, 'required'],
      ['Do', false, 'minlength'],
      ['Doe', true, 'minlength'],
      ['a'.repeat(21), false, 'maxlength'],
    ])('lastName "%s" → valid=%s, error=%s', (value, valid, expectedError) => {
      const lastName = component.form.get('lastName');
      lastName?.setValue(value);

      expect(lastName?.valid).toBe(valid);
      expect(lastName?.hasError(expectedError as string)).toBe(!valid);
    });

    it.each([
      ['', false, 'required'],
      ['ab', false, 'minlength'],
      ['password', true, 'minlength'],
      ['a'.repeat(41), false, 'maxlength'],
    ])('password "%s" → valid=%s, error=%s', (value, valid, expectedError) => {
      const password = component.form.get('password');
      password?.setValue(value);

      expect(password?.valid).toBe(valid);
      expect(password?.hasError(expectedError as string)).toBe(!valid);
    });

    it('submit button should be disabled while the form is invalid and enabled when valid', () => {
      component.form.setValue({ ...mockRegister, email: '' });
      fixture.detectChanges();
      expect(submitButton().disabled).toBe(true);

      component.form.setValue(mockRegister);
      fixture.detectChanges();
      expect(submitButton().disabled).toBe(false);
    });

    it('form should be invalid when a required field is empty', () => {
      component.form.reset();
      fixture.detectChanges();
      expect(submitButton().disabled).toBe(true);
    });

    it('should display the error message in the DOM when onError is true', () => {
      component.onError = true;
      fixture.detectChanges();

      const errorElement = fixture.debugElement.query(By.css('.error')).nativeElement as HTMLElement;
      expect(errorElement).toBeDefined();
    });

    it('should not send an HTTP request when the form is invalid on submit', () => {
      component.form.reset();
      fixture.detectChanges();
      expect(submitButton().disabled).toBe(true);

      submitButton().click();
      fixture.detectChanges();

      httpMock.expectNone({ url: '/api/auth/register' });
    });
  });

  describe("Tests d'intégration (composant + AuthService/Router réels + HttpClientTesting)", () => {

    it('should register successfully and navigate to /login', () => {
      const { registerSpy, navigateSpy } = submitSuccessfulRegister();

      expect(registerSpy).toHaveBeenCalledWith(mockRegister);
      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
      expect(component.onError).toBe(false);
    });

    it('should set onError to true when registration fails', () => {
      const { registerSpy, navigateSpy } = submitFailedRegister();

      expect(registerSpy).toHaveBeenCalledWith(mockRegister);
      expect(navigateSpy).not.toHaveBeenCalled();
      expect(component.onError).toBe(true);
    });

    it('should set onError to true on a 500 server error', () => {
      submitFailedRegister(mockRegister, 500, 'Internal Server Error');

      expect(component.onError).toBe(true);
    });

    it('should reset onError to false after a successful registration following a previous error', () => {
      component.onError = true;

      submitSuccessfulRegister();

      expect(component.onError).toBe(false);
    });
  });
});
