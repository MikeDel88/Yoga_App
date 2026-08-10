import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { By } from '@angular/platform-browser';
import { SessionService } from '../../../../core/service/session.service';
import { Session } from '../../../../core/models/session.interface';
import { Teacher } from '../../../../core/models/teacher.interface';
import { routes } from '../../../../app.routes';

import { DetailComponent } from './detail.component';

/**
 * Plans de test
 * ● Les informations de la session sont correctement affichées
 * ● Le bouton Delete apparaît si l'utilisateur connecté est un admin
 * ● La session est correctement supprimée
 * ● L'affichage du bouton participate ou do not participate en fonction de isParticipate
 *
 * Les tests sont classés en deux catégories :
 * - "Tests unitaires" : DOM une fois la session et le teacher résolus (HTTP mocké, requêtes
 *   non vérifiées en détail).
 * - "Tests d'intégration" : vérifient le vrai flux SessionApiService/TeacherService/Router
 *   → HttpClientTesting → composant → DOM.
 */
describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockSessionService: { sessionInformation: { admin: boolean; id: number } } = {
    sessionInformation: { admin: true, id: 1 }
  };

  const mockMatSnackBar = { open: jest.fn() };

  const mockSession: Session = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing yoga session',
    date: new Date('2024-01-01'),
    teacher_id: 1,
    users: []
  };

  const mockTeacher: Teacher = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    createdAt: new Date('2023-01-01'),
    updatedAt: new Date('2023-01-01')
  };

  beforeEach(async () => {
    mockSessionService.sessionInformation = { admin: true, id: 1 };
    mockMatSnackBar.open.mockClear();

    // DetailComponent est standalone et importe MaterialModule → MatSnackBarModule, qui fournit
    // lui-même MatSnackBar dans ses `providers`. Un override au niveau du TestBed n'atteint donc
    // pas l'injecteur local du composant : il faut overrider directement le composant.
    TestBed.overrideComponent(DetailComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: mockMatSnackBar }] }
    });

    await TestBed.configureTestingModule({
      declarations: [],
      imports: [DetailComponent, MatSnackBarModule],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } }
        },
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify(); // aucune requête HTTP en attente non vérifiée
  });

  /**
   * Crée le composant (isAdmin/userId sont lus dans le constructeur, donc `mockSessionService`
   * doit être configuré avant cet appel) puis déclenche ngOnInit et résout les requêtes GET
   * session et GET teacher.
   */
  function renderWithSession(session: Session = mockSession, teacher: Teacher = mockTeacher): void {
    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();

    const sessionReq = httpMock.expectOne({ url: `api/session/${session.id}` });
    sessionReq.flush(session);

    const teacherReq = httpMock.expectOne({ url: `api/teacher/${session.teacher_id}` });
    teacherReq.flush(teacher);

    fixture.detectChanges();
  }

  const deleteButton = () => fixture.debugElement.query(By.css('[data-testid="delete-button"]'));
  const participateButton = () => fixture.debugElement.query(By.css('[data-testid="participate-button"]'));
  const unParticipateButton = () => fixture.debugElement.query(By.css('[data-testid="unparticipate-button"]'));
  const sessionTitle = () => fixture.debugElement.query(By.css('[data-testid="session-title"]'));
  const sessionDescription = () => fixture.debugElement.query(By.css('[data-testid="session-description"]'));
  const attendeesCount = () => fixture.debugElement.query(By.css('[data-testid="attendees-count"]'));
  const teacherName = () => fixture.debugElement.query(By.css('[data-testid="teacher-name"]'));

  describe('Tests unitaires (DOM, session et teacher déjà résolus)', () => {

    it('should create', () => {
      renderWithSession();
      expect(component).toBeTruthy();
    });

    it('should display the session name, description and number of attendees', () => {
      renderWithSession({ ...mockSession, name: 'yoga session', users: [1, 2, 3] });

      expect((sessionTitle().nativeElement as HTMLElement).textContent).toContain('Yoga Session'); // titlecase pipe
      expect((sessionDescription().nativeElement as HTMLElement).textContent).toContain(mockSession.description);
      expect((attendeesCount().nativeElement as HTMLElement).textContent).toContain('3 attendees');
    });

    it('should display the teacher first name and uppercased last name', () => {
      renderWithSession();

      const subtitle = teacherName().nativeElement as HTMLElement;

      expect(subtitle.textContent).toContain('John');
      expect(subtitle.textContent).toContain('DOE');
    });

    it('should display the Delete button when the user is admin', () => {
      mockSessionService.sessionInformation.admin = true;

      renderWithSession();

      expect(deleteButton()).toBeTruthy();
    });

    it('should not display the Delete button when the user is not admin', () => {
      mockSessionService.sessionInformation.admin = false;

      renderWithSession();

      expect(deleteButton()).toBeFalsy();
    });

    it('should display the Participate button when the user is not admin and does not participate', () => {
      mockSessionService.sessionInformation.admin = false;

      renderWithSession({ ...mockSession, users: [] });

      expect(participateButton()).toBeTruthy();
      expect(unParticipateButton()).toBeFalsy();
    });

    it('should display the Do not participate button when the user is not admin and already participates', () => {
      mockSessionService.sessionInformation.admin = false;

      renderWithSession({ ...mockSession, users: [1] });

      expect(unParticipateButton()).toBeTruthy();
    });

    it('should display neither Participate nor Do not participate when the user is admin', () => {
      mockSessionService.sessionInformation.admin = true;

      renderWithSession();

      expect(participateButton()).toBeFalsy();
      expect(unParticipateButton()).toBeFalsy();
    });

    it('should call window.history.back on back()', () => {
      renderWithSession();
      const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});

      component.back();

      expect(backSpy).toHaveBeenCalled();
      backSpy.mockRestore();
    });
  });

  describe("Tests d'intégration (SessionApiService/TeacherService/Router réels + HttpClientTesting)", () => {

    it('should fetch the session then the teacher on init', () => {
      fixture = TestBed.createComponent(DetailComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      const sessionReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
      expect(sessionReq.request.method).toBe('GET');
      sessionReq.flush(mockSession);

      const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher/1' });
      expect(teacherReq.request.method).toBe('GET');
      teacherReq.flush(mockTeacher);
    });

    it('should delete the session, show a snackbar and navigate to sessions on delete()', () => {
      mockSessionService.sessionInformation.admin = true;
      renderWithSession();

      const navigateSpy = jest.spyOn(router, 'navigate');

      deleteButton().nativeElement.click();

      const deleteReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
      expect(deleteReq.request.method).toBe('DELETE');
      deleteReq.flush(null);

      expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session deleted !', 'Close', { duration: 3000 });
      expect(navigateSpy).toHaveBeenCalledWith(['sessions']);
    });

    it('should participate then refetch the session on participate()', () => {
      mockSessionService.sessionInformation.admin = false;
      renderWithSession({ ...mockSession, users: [] });

      expect(component.isParticipate).toBe(false);

      participateButton().nativeElement.click();

      const participateReq: TestRequest = httpMock.expectOne({ url: 'api/session/1/participate/1' });
      expect(participateReq.request.method).toBe('POST');
      participateReq.flush(null);

      const refetchReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
      refetchReq.flush({ ...mockSession, users: [1] });

      const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher/1' });
      teacherReq.flush(mockTeacher);

      expect(component.isParticipate).toBe(true);
    });

    it('should unParticipate then refetch the session on unParticipate()', () => {
      mockSessionService.sessionInformation.admin = false;
      renderWithSession({ ...mockSession, users: [1] });

      expect(component.isParticipate).toBe(true);

      unParticipateButton().nativeElement.click();

      const unParticipateReq: TestRequest = httpMock.expectOne({ url: 'api/session/1/participate/1' });
      expect(unParticipateReq.request.method).toBe('DELETE');
      unParticipateReq.flush(null);

      const refetchReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
      refetchReq.flush({ ...mockSession, users: [] });

      const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher/1' });
      teacherReq.flush(mockTeacher);

      expect(component.isParticipate).toBe(false);
    });
  });
});
