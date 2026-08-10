import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { afterEach, beforeEach, describe, expect, it } from '@jest/globals';
import { SessionService } from 'src/app/core/service/session.service';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { Session } from 'src/app/core/models/session.interface';
import { routes } from '../../../../app.routes';

import { ListComponent } from './list.component';

/**
 * Plans de test
 * ● Affichage de la liste des sessions
 * ● L’apparition des boutons Create et Edit si l’utilisateur connecté est un admin
 *
 * Tests unitaires : DOM une fois les sessions résolues (HTTP mocké, requête non vérifiée en détail).
 * Tests d'intégration : vérifient le vrai flux SessionApiService → HttpClientTesting → composant → DOM.
 */
describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let httpMock: HttpTestingController;

  const mockSessionService: { sessionInformation: { admin: boolean } } = {
    sessionInformation: { admin: true }
  };

  const mockSessions: Session[] = [
    { id: 1, name: 'Yoga session 1', description: 'Description 1', date: new Date('2024-01-01'), teacher_id: 1, users: [] },
    { id: 2, name: 'Yoga session 2', description: 'Description 2', date: new Date('2024-02-01'), teacher_id: 2, users: [] },
  ];

  beforeEach(async () => {
    mockSessionService.sessionInformation.admin = true;

    await TestBed.configureTestingModule({
      declarations: [],
      imports: [ListComponent, MatCardModule, MatIconModule],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // aucune requête HTTP en attente non vérifiée
  });

  /** Déclenche le rendu (souscription à sessions$, envoi de la requête HTTP) puis résout avec `sessions`. */
  function renderWithSessions(sessions: Session[] = mockSessions): TestRequest {
    fixture.detectChanges();
    const req = httpMock.expectOne({ url: 'api/session' });
    req.flush(sessions);
    fixture.detectChanges();
    return req;
  }

  const createButtons = () => fixture.debugElement.queryAll(By.css('[data-testid="create-button"]'));
  const sessionCards = () => fixture.debugElement.queryAll(By.css('[data-testid="session-card"]'));
  const editButtons = (card: DebugElement) => card.queryAll(By.css('[data-testid="edit-button"]'));
  const detailButtons = (card: DebugElement) => card.queryAll(By.css('[data-testid="detail-button"]'));

  describe('Tests unitaires (DOM, sessions déjà résolues)', () => {

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should display the Create button when the user is admin', () => {
      renderWithSessions();

      expect(createButtons().length).toBe(1);
      expect(createButtons()[0].nativeElement.textContent).toContain('Create');
    });

    it('should not display the Create button when the user is not admin', () => {
      mockSessionService.sessionInformation.admin = false;

      renderWithSessions();

      expect(createButtons().length).toBe(0);
    });

    it('should display the Edit button on each card when the user is admin', () => {
      renderWithSessions();

      for (const card of sessionCards()) {
        expect(editButtons(card).length).toBe(1);
      }
    });

    it('should not display the Edit button on any card when the user is not admin', () => {
      mockSessionService.sessionInformation.admin = false;

      renderWithSessions();

      for (const card of sessionCards()) {
        expect(editButtons(card).length).toBe(0);
      }
    });

    it('should always display the Detail button regardless of admin status', () => {
      mockSessionService.sessionInformation.admin = false;

      renderWithSessions();

      for (const card of sessionCards()) {
        expect(detailButtons(card).length).toBe(1);
      }
    });

    it('should render one card per session received', () => {
      renderWithSessions();

      expect(sessionCards().length).toBe(mockSessions.length);
    });

    it('should display the name, description and date of a session', () => {
      renderWithSessions([mockSessions[0]]);

      const card = sessionCards()[0];
      const title = card.query(By.css('mat-card-title')).nativeElement as HTMLElement;
      const subtitle = card.query(By.css('mat-card-subtitle')).nativeElement as HTMLElement;
      const description = card.query(By.css('mat-card-content')).nativeElement as HTMLElement;

      expect(title.textContent).toContain(mockSessions[0].name);
      expect(subtitle.textContent).toContain('Session on');
      expect(description.textContent).toContain(mockSessions[0].description);
    });

    it('should render no card when the sessions list is empty', () => {
      renderWithSessions([]);

      expect(sessionCards().length).toBe(0);
    });
  });

  describe("Tests d'intégration (SessionApiService réel + HttpClientTesting)", () => {

    it('should send a GET request to api/session on load', () => {
      fixture.detectChanges();

      const req: TestRequest = httpMock.expectOne({ url: 'api/session' });
      expect(req.request.method).toBe('GET');

      req.flush(mockSessions);
    });

    it('should render the sessions returned by the API', () => {
      renderWithSessions(mockSessions);

      expect(sessionCards().length).toBe(mockSessions.length);
    });

    it('should render no card when the API returns an error', () => {
      fixture.detectChanges();

      const req: TestRequest = httpMock.expectOne({ url: 'api/session' });
      req.flush({ message: 'Internal Server Error' }, { status: 500, statusText: 'Internal Server Error' });
      fixture.detectChanges();

      expect(sessionCards().length).toBe(0);
    });
  });
});
