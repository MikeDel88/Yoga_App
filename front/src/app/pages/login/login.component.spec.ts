import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter, Router } from '@angular/router';
import { beforeEach, afterEach, describe, expect, it, jest } from '@jest/globals';
import { AuthService } from 'src/app/core/service/auth.service';
import { SessionService } from 'src/app/core/service/session.service';
import { LoginComponent } from './login.component';
import {HttpTestingController, provideHttpClientTesting, TestRequest} from "@angular/common/http/testing";
import {provideHttpClient} from "@angular/common/http";
import {routes} from "../../app.routes";
import {SessionInformation} from "../../core/models/sessionInformation.interface";
import {LoginRequest} from "../../core/models/loginRequest.interface";
import { By } from '@angular/platform-browser'

/**
 * Plan de test
 * ● La connexion
 * ● La gestion des erreurs en cas de mauvais login / password
 * ● L’affichage d’erreur en l’absence d’un champ obligatoire
 *
 * Les tests sont classés en deux catégories :
 * - "Tests unitaires" : n'exercent que le FormGroup / le DOM du composant, sans passer par
 *   AuthService, SessionService ou une vraie requête HTTP.
 * - "Tests d'intégration" : passent par le vrai flux du composant (AuthService réel + Router réel
 *   + SessionService réel), avec seulement la couche HTTP simulée via HttpClientTesting.
 */

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: AuthService;
  let sessionService: SessionService;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockLogin: LoginRequest = { email: 'test@test.com', password: 'password' };
  const wrongLogin: LoginRequest = { email: 'test@test.com', password: 'wrongpassword' };
  const submitButton = () =>
    fixture.debugElement.query(By.css('[data-testid="submit"]')).nativeElement as HTMLButtonElement;
  const mockSession: SessionInformation = {
    token: 'fake-jwt-token',
    type: 'Bearer',
    id: 1,
    username: 'testuser',
    firstName: 'Test',
    lastName: 'User',
    admin: false
  } as SessionInformation;

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
        LoginComponent,
        BrowserAnimationsModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    sessionService = TestBed.inject(SessionService);
  });

  afterEach(() => {
    httpMock.verify(); // aucune requête HTTP en attente non vérifiée
  });

  /** Soumet le formulaire avec `loginRequest` et résout la requête HTTP avec `session`. */
  function submitSuccessfulLogin(
    loginRequest: LoginRequest = mockLogin,
    session: SessionInformation = mockSession
  ) {
    const loginSpy = jest.spyOn(authService, 'login');
    const navigateSpy = jest.spyOn(router, 'navigate');
    const sessionLoginSpy = jest.spyOn(sessionService, 'logIn');

    component.form.setValue(loginRequest);
    component.submit();  // ← déclenche le vrai flux du composant

    const req: TestRequest = httpMock.expectOne({ url: '/api/auth/login' });
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
    req.flush(session);  // ← résout le subscribe DU COMPOSANT, donc déclenche navigate()

    return { loginSpy, navigateSpy, sessionLoginSpy };
  }

  /** Soumet le formulaire avec `loginRequest` et résout la requête HTTP avec une erreur. */
  function submitFailedLogin(
    loginRequest: LoginRequest = wrongLogin,
    status: number = 401,
    statusText: string = 'Unauthorized'
  ) {
    const loginSpy = jest.spyOn(authService, 'login');
    const navigateSpy = jest.spyOn(router, 'navigate');
    const sessionLoginSpy = jest.spyOn(sessionService, 'logIn');

    component.form.setValue(loginRequest);
    component.submit();

    const req: TestRequest = httpMock.expectOne({ url: '/api/auth/login' });
    req.flush({ message: statusText }, { status, statusText });

    return { loginSpy, navigateSpy, sessionLoginSpy };
  }

  describe('Tests unitaires (FormGroup / DOM, sans HTTP ni services)', () => {

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('submit button should be disabled while the form is invalid and enabled when valid', () => {
      component.form.setValue({email: "test@test.com", password: ""});
      fixture.detectChanges()
      expect(submitButton().disabled).toBe(true);

      component.form.setValue({email: "", password: "password"});
      fixture.detectChanges()
      expect(submitButton().disabled).toBe(true);

      component.form.setValue(mockLogin);
      fixture.detectChanges()
      expect(submitButton().disabled).toBe(false);
    });

    it('should display the error message in the DOM when onError is true', () => {
      component.onError = true;
      fixture.detectChanges();

      const errorElement = fixture.debugElement.query(By.css('.error')).nativeElement as HTMLElement;
      expect(errorElement).toBeDefined();
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
      ['azerty', true, 'minLength'],
      ['abc', false, 'minlength'],
    ])('password "%s" → valid=%s, error=%s', (value, valid, expectedError) => {
      const password = component.form.get('password');
      password?.setValue(value);

      expect(password?.valid).toBe(valid);
      expect(password?.hasError(expectedError as string)).toBe(!valid);
    });

    it('should not send an HTTP request when the form is invalid on submit', () => {
      component.form.reset();
      fixture.detectChanges();
      expect(submitButton().disabled).toBe(true);

      submitButton().click();
      fixture.detectChanges();

      httpMock.expectNone({ url: '/api/auth/login' });
    });

    it('form should be invalid when both email and password are empty', () => {
      component.form.reset();
      fixture.detectChanges();
      expect(submitButton().disabled).toBe(true);
    });
  });

  describe("Tests d'intégration (composant + AuthService/SessionService/Router réels + HttpClientTesting)", () => {

    it('should connexion is success', () => {
      const { loginSpy, navigateSpy } = submitSuccessfulLogin();

      expect(loginSpy).toHaveBeenCalledWith(mockLogin);
      expect(navigateSpy).toHaveBeenCalledWith(['/sessions']);
      expect(component.onError).toBe(false);
    });

    it('should reset onError to false after a successful login following a previous error', () => {
      component.onError = true;

      submitSuccessfulLogin();

      expect(component.onError).toBe(false);
    });

    it('should call sessionService.logIn with the received session information', () => {
      const { sessionLoginSpy } = submitSuccessfulLogin();

      expect(sessionLoginSpy).toHaveBeenCalledWith(mockSession);
    });

    it('error when login or password is wrong', () => {
      const { loginSpy, navigateSpy } = submitFailedLogin();

      expect(loginSpy).toHaveBeenCalledWith(wrongLogin);
      expect(navigateSpy).not.toHaveBeenCalled();
      expect(component.onError).toBe(true);
    });

    it('should set onError to true on a 500 server error', () => {
      submitFailedLogin(wrongLogin, 500, 'Internal Server Error');
      fixture.detectChanges()
      expect(component.onError).toBe(true);
    });

    it('should not call sessionService.logIn when login fails', () => {
      const { sessionLoginSpy } = submitFailedLogin();
      expect(sessionLoginSpy).not.toHaveBeenCalled();
    });

    it('should not call sessionService.logIn on a 500 server error', () => {
      const { sessionLoginSpy } = submitFailedLogin(wrongLogin, 500, 'Internal Server Error');
      expect(sessionLoginSpy).not.toHaveBeenCalled();
    });

    it('should keep the entered email and password in the form after a failed login', () => {
      submitFailedLogin();

      expect(component.form.value).toEqual(wrongLogin);
    });
  });
});
