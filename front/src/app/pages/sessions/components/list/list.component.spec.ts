import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { SessionService } from 'src/app/core/service/session.service';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
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
  let router: Router;

  const mockSessionService: { isLogged: boolean; sessionInformation: { admin: boolean } } = {
    isLogged: true,
    sessionInformation: { admin: true }
  };

  const mockSessions: Session[] = [
    { id: 1, name: 'Yoga session 1', description: 'Description 1', date: new Date('2024-01-01'), teacher_id: 1, users: [] },
    { id: 2, name: 'Yoga session 2', description: 'Description 2', date: new Date('2024-02-01'), teacher_id: 2, users: [] },
  ];

  beforeEach(async () => {
    mockSessionService.isLogged = true;
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
    router = TestBed.inject(Router);
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

    /**
     * Ces tests naviguent réellement vers /sessions (via RouterTestingHarness, qui active le
     * composant dans un vrai <router-outlet>) plutôt que de créer ListComponent directement :
     * les routerLink relatifs ("create", ['detail', id]) ne se résolvent correctement que dans
     * ce contexte de route activée.
     */
    it('should navigate to /sessions/create when clicking the Create button', async () => {
      const harness = await RouterTestingHarness.create('/sessions');
      httpMock.expectOne({ url: 'api/session' }).flush(mockSessions);
      harness.detectChanges();
      // Le clic déclenche une vraie navigation (via RouterLink) : on l'intercepte pour vérifier
      // l'URL calculée sans réellement activer FormComponent (ses propres dépendances ne sont
      // pas configurées dans ce test de ListComponent).
      const navigateByUrlSpy = jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

      const createButton = harness.routeDebugElement!.query(By.css('[data-testid="create-button"]'));
      createButton.nativeElement.click();

      expect(navigateByUrlSpy).toHaveBeenCalled();
      expect(navigateByUrlSpy.mock.calls[0][0].toString()).toBe('/sessions/create');
    });

    it("should navigate to the matching /sessions/detail/:id when clicking a card's Detail button", async () => {
      const harness = await RouterTestingHarness.create('/sessions');
      httpMock.expectOne({ url: 'api/session' }).flush(mockSessions);
      harness.detectChanges();
      const navigateByUrlSpy = jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

      const cards = harness.routeDebugElement!.queryAll(By.css('[data-testid="session-card"]'));
      detailButtons(cards[1])[0].nativeElement.click();

      expect(navigateByUrlSpy).toHaveBeenCalled();
      expect(navigateByUrlSpy.mock.calls[0][0].toString()).toBe(`/sessions/detail/${mockSessions[1].id}`);
    });

    it("should navigate to the matching /sessions/update/:id when clicking a card's Edit button", async () => {
      const harness = await RouterTestingHarness.create('/sessions');
      httpMock.expectOne({ url: 'api/session' }).flush(mockSessions);
      harness.detectChanges();
      const navigateByUrlSpy = jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

      const cards = harness.routeDebugElement!.queryAll(By.css('[data-testid="session-card"]'));
      editButtons(cards[0])[0].nativeElement.click();

      expect(navigateByUrlSpy).toHaveBeenCalled();
      expect(navigateByUrlSpy.mock.calls[0][0].toString()).toBe(`/sessions/update/${mockSessions[0].id}`);
    });

    it("should navigate to the matching /sessions/update/:id for the second card's Edit button too", async () => {
      const harness = await RouterTestingHarness.create('/sessions');
      httpMock.expectOne({ url: 'api/session' }).flush(mockSessions);
      harness.detectChanges();
      const navigateByUrlSpy = jest.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

      const cards = harness.routeDebugElement!.queryAll(By.css('[data-testid="session-card"]'));
      editButtons(cards[1])[0].nativeElement.click();

      expect(navigateByUrlSpy).toHaveBeenCalled();
      expect(navigateByUrlSpy.mock.calls[0][0].toString()).toBe(`/sessions/update/${mockSessions[1].id}`);
    });
  });
});
