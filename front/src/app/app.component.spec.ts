import { TestBed } from '@angular/core/testing';
import { ComponentFixture } from '@angular/core/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { provideRouter, Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { SessionService } from './core/service/session.service';
import { SessionInformation } from './core/models/sessionInformation.interface';

import { AppComponent } from './app.component';

/**
 * Plans de test
 * ● La création de l'application.
 * ● La déconnexion de l’utilisateur
 * ● Les routes de navigation en fonction s'il est loggé ou non.
 * ● L'affichage du nom de l'application
 */
describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let sessionService: SessionService;
  let router: Router;

  const mockSessionInformation: SessionInformation = {
    token: 'fake-jwt-token',
    type: 'Bearer',
    id: 1,
    username: 'testuser',
    firstName: 'Test',
    lastName: 'User',
    admin: false
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [],
      imports: [AppComponent, MatToolbarModule],
      providers: [
        // routes vides : RouterLink sur les <a> peut calculer un href via createUrlTree()
        // sans lever d'erreur, sans pour autant activer de composant routé.
        provideRouter([]),
      ],
    }).compileComponents();

    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    sessionService.logOut();
  });

  /** Simule un utilisateur connecté puis crée le composant. */
  function renderLoggedIn(): void {
    sessionService.logIn(mockSessionInformation);

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  /** Simule un utilisateur non connecté puis crée le composant. */
  function renderLoggedOut(): void {
    sessionService.logOut();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  const appTitle = () => fixture.debugElement.query(By.css('[data-testid="app-title"]'));
  const sessionsLink = () => fixture.debugElement.query(By.css('[data-testid="sessions-link"]'));
  const accountLink = () => fixture.debugElement.query(By.css('[data-testid="account-link"]'));
  const logoutLink = () => fixture.debugElement.query(By.css('[data-testid="logout-link"]'));
  const loginLink = () => fixture.debugElement.query(By.css('[data-testid="login-link"]'));
  const registerLink = () => fixture.debugElement.query(By.css('[data-testid="register-link"]'));

  it('should create', () => {
    renderLoggedOut();
    expect(component).toBeTruthy();
  });

  it('should display the application name', () => {
    renderLoggedOut();

    expect((appTitle().nativeElement as HTMLElement).textContent).toContain('Yoga app');
  });

  it('should display Sessions/Account/Logout and hide Login/Register when logged in', () => {
    renderLoggedIn();

    expect(sessionsLink()).toBeTruthy();
    expect(accountLink()).toBeTruthy();
    expect(logoutLink()).toBeTruthy();
    expect(loginLink()).toBeFalsy();
    expect(registerLink()).toBeFalsy();
  });

  it('should display Login/Register and hide Sessions/Account/Logout when logged out', () => {
    renderLoggedOut();

    expect(loginLink()).toBeTruthy();
    expect(registerLink()).toBeTruthy();
    expect(sessionsLink()).toBeFalsy();
    expect(accountLink()).toBeFalsy();
    expect(logoutLink()).toBeFalsy();
  });

  it('should log the user out and navigate to "" on logout()', () => {
    renderLoggedIn();

    logoutLink().nativeElement.click();

    expect(sessionService.isLogged).toBe(false);
    expect(sessionService.sessionInformation).toBeUndefined();
    expect(router.navigate).toHaveBeenCalledWith(['']);
  });
});
