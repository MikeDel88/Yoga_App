import { ComponentFixture, TestBed } from '@angular/core/testing';
import { formatDate } from '@angular/common';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { By } from '@angular/platform-browser';
import { SessionService } from 'src/app/core/service/session.service';
import { User } from 'src/app/core/models/user.interface';

import { MeComponent } from './me.component';

/**
 * Plans de test
 * ● Affichage des informations de l’utilisateur
 * ● Suppression de l'utilisateur
 * ● La déconnexion de l’utilisateur
 * ● Retour sur le click back
 *
 * Les tests sont classés en deux catégories :
 * - "Tests unitaires" : DOM une fois l'utilisateur résolu (HTTP mocké, requête non vérifiée
 *   en détail).
 * - "Tests d'intégration" : vérifient le vrai flux UserService → HttpClientTesting → composant,
 *   avec Router/SessionService/MatSnackBar mockés.
 */
describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let httpMock: HttpTestingController;

  const mockSessionService = {
    isLogged: true,
    sessionInformation: { id: 1 } as { id: number } | undefined,
    logOut: jest.fn()
  };

  const mockRouter = { navigate: jest.fn() };

  const mockMatSnackBar = { open: jest.fn() };

  const mockUser: User = {
    id: 1,
    email: 'john.doe@test.com',
    firstName: 'John',
    lastName: 'Doe',
    admin: false,
    password: 'password',
    createdAt: new Date('2023-01-01'),
    updatedAt: new Date('2023-01-02')
  };

  beforeEach(async () => {
    mockSessionService.isLogged = true;
    mockSessionService.sessionInformation = { id: 1 };
    mockSessionService.logOut.mockClear();
    // Reproduit le comportement réel de SessionService.logOut() (cf. session.service.ts) pour
    // pouvoir vérifier que l'utilisateur est effectivement déconnecté, pas juste que la méthode
    // a été appelée.
    mockSessionService.logOut.mockImplementation(() => {
      mockSessionService.isLogged = false;
      mockSessionService.sessionInformation = undefined;
    });
    mockRouter.navigate.mockClear();
    mockMatSnackBar.open.mockClear();

    // MeComponent est standalone et importe MaterialModule → MatSnackBarModule, qui fournit
    // lui-même MatSnackBar dans ses `providers`. Un override au niveau du TestBed n'atteint donc
    // pas l'injecteur local du composant : il faut overrider directement le composant
    // (cf. detail.component.spec.ts / form.component.spec.ts pour le même contournement).
    TestBed.overrideComponent(MeComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: mockMatSnackBar }] }
    });

    await TestBed.configureTestingModule({
      declarations: [],
      imports: [MeComponent, MatSnackBarModule],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter },
        provideHttpClient(),
        provideHttpClientTesting(),
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // aucune requête HTTP en attente non vérifiée
  });

  /** Déclenche ngOnInit puis résout la requête GET de l'utilisateur. */
  function renderWithUser(user: User = mockUser): void {
    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();

    const userReq: TestRequest = httpMock.expectOne({ url: 'api/user/1' });
    userReq.flush(user);

    fixture.detectChanges();
  }

  const userName = () => fixture.debugElement.query(By.css('[data-testid="user-name"]'));
  const userEmail = () => fixture.debugElement.query(By.css('[data-testid="user-email"]'));
  const adminBadge = () => fixture.debugElement.query(By.css('[data-testid="admin-badge"]'));
  const deleteButton = () => fixture.debugElement.query(By.css('[data-testid="delete-button"]'));
  const createdAt = () => fixture.debugElement.query(By.css('[data-testid="created-at"]'));
  const updatedAt = () => fixture.debugElement.query(By.css('[data-testid="updated-at"]'));

  describe('Tests unitaires (DOM, utilisateur déjà résolu)', () => {

    it('should create', () => {
      renderWithUser();
      expect(component).toBeTruthy();
    });

    it('should display the user name and email', () => {
      renderWithUser();

      expect((userName().nativeElement as HTMLElement).textContent).toContain(mockUser.firstName);
      expect((userEmail().nativeElement as HTMLElement).textContent).toContain(mockUser.email);
    });

    it('should display the last name in uppercase', () => {
      renderWithUser({ ...mockUser, lastName: 'Doe' });

      const text = (userName().nativeElement as HTMLElement).textContent as string;

      expect(text).toContain('DOE');
      expect(text).not.toContain('Doe');
    });

    it('should display "You are admin" and hide the Delete button when the user is admin', () => {
      renderWithUser({ ...mockUser, admin: true });

      expect(adminBadge()).toBeTruthy();
      expect(deleteButton()).toBeFalsy();
    });

    it('should display the Delete button and hide "You are admin" when the user is not admin', () => {
      renderWithUser({ ...mockUser, admin: false });

      expect(deleteButton()).toBeTruthy();
      expect(adminBadge()).toBeFalsy();
    });

    it('should display the created and updated dates in longDate format', () => {
      renderWithUser();

      expect((createdAt().nativeElement as HTMLElement).textContent)
        .toContain(formatDate(mockUser.createdAt, 'longDate', 'en-US'));
      expect((updatedAt().nativeElement as HTMLElement).textContent)
        .toContain(formatDate(mockUser.updatedAt!, 'longDate', 'en-US'));
    });

    it('should call window.history.back on back()', () => {
      renderWithUser();
      const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});

      component.back();

      expect(backSpy).toHaveBeenCalled();
      backSpy.mockRestore();
    });
  });

  describe("Tests d'intégration (UserService réel + HttpClientTesting)", () => {

    it('should fetch the user on init', () => {
      fixture = TestBed.createComponent(MeComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      const userReq: TestRequest = httpMock.expectOne({ url: 'api/user/1' });
      expect(userReq.request.method).toBe('GET');
      userReq.flush(mockUser);
    });

    it('should delete the account, show a snackbar, log out and navigate to / on delete()', () => {
      renderWithUser({ ...mockUser, admin: false });

      deleteButton().nativeElement.click();

      const deleteReq: TestRequest = httpMock.expectOne({ url: 'api/user/1' });
      expect(deleteReq.request.method).toBe('DELETE');
      deleteReq.flush(null);

      expect(mockMatSnackBar.open).toHaveBeenCalledWith('Your account has been deleted !', 'Close', { duration: 3000 });
      expect(mockSessionService.logOut).toHaveBeenCalled();
      expect(mockSessionService.isLogged).toBe(false);
      expect(mockSessionService.sessionInformation).toBeUndefined();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });
  });
});
