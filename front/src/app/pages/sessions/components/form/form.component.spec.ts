import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { afterEach, beforeEach, describe, expect, it, jest } from '@jest/globals';
import { By } from '@angular/platform-browser';
import { SessionService } from '../../../../core/service/session.service';
import { Session } from '../../../../core/models/session.interface';
import { Teacher } from '../../../../core/models/teacher.interface';

import { FormComponent } from './form.component';

/**
 * Plans de test
 * Création de session
 * ● La session est créée
 * ● L’affichage d’erreur en l’absence d’un champ obligatoire
 * Update de session
 * ● La session est modifiée
 * ● L’affichage d’erreur en l’absence d’un champ obligatoire
 *
 * Les tests sont classés en deux catégories :
 * - "Tests unitaires" : DOM une fois le formulaire (et les teachers) résolus (HTTP mocké,
 *   requêtes non vérifiées en détail).
 * - "Tests d'intégration" : vérifient le vrai flux SessionApiService/TeacherService →
 *   HttpClientTesting → composant, avec Router et MatSnackBar mockés.
 */
describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let httpMock: HttpTestingController;

  const mockSessionService: { sessionInformation: { admin: boolean } } = {
    sessionInformation: { admin: true }
  };

  const mockRouter = { url: '/sessions/create', navigate: jest.fn() };

  const mockMatSnackBar = { open: jest.fn() };

  const mockTeachers: Teacher[] = [
    { id: 1, firstName: 'John', lastName: 'Doe', createdAt: new Date('2023-01-01'), updatedAt: new Date('2023-01-01') },
    { id: 2, firstName: 'Jane', lastName: 'Smith', createdAt: new Date('2023-01-01'), updatedAt: new Date('2023-01-01') },
  ];

  const mockSession: Session = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing yoga session',
    date: new Date('2024-01-01'),
    teacher_id: 2,
    users: []
  };

  const validFormValue = {
    name: 'Yoga session',
    date: '2024-01-01',
    teacher_id: 1,
    description: 'A relaxing yoga session'
  };

  beforeEach(async () => {
    mockSessionService.sessionInformation = { admin: true };
    mockRouter.url = '/sessions/create';
    mockRouter.navigate.mockClear();
    mockMatSnackBar.open.mockClear();

    // FormComponent est standalone et importe MaterialModule → MatSnackBarModule, qui fournit
    // lui-même MatSnackBar dans ses `providers`. Un override au niveau du TestBed n'atteint donc
    // pas l'injecteur local du composant : il faut overrider directement le composant
    // (cf. detail.component.spec.ts pour le même contournement).
    TestBed.overrideComponent(FormComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: mockMatSnackBar }] }
    });

    await TestBed.configureTestingModule({
      declarations: [],
      imports: [FormComponent, MatSnackBarModule, ReactiveFormsModule],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } }
        },
        provideHttpClient(),
        provideHttpClientTesting(),
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // aucune requête HTTP en attente non vérifiée
  });

  /** Crée le composant en mode création et résout la requête GET des teachers. */
  function renderCreateForm(): void {
    mockRouter.url = '/sessions/create';

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();

    const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher' });
    teacherReq.flush(mockTeachers);

    fixture.detectChanges();
  }

  /** Crée le composant en mode update et résout les requêtes GET session puis GET teachers. */
  function renderUpdateForm(session: Session = mockSession): void {
    mockRouter.url = '/sessions/update/1';

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();

    const sessionReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
    sessionReq.flush(session);
    fixture.detectChanges();

    const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher' });
    teacherReq.flush(mockTeachers);

    fixture.detectChanges();
  }

  const formTitle = () => fixture.debugElement.query(By.css('[data-testid="form-title"]'));
  const saveButton = () => fixture.debugElement.query(By.css('[data-testid="save-button"]'));
  const nameInput = () => fixture.debugElement.query(By.css('input[formControlName="name"]'));
  const dateInput = () => fixture.debugElement.query(By.css('input[formControlName="date"]'));
  const descriptionTextarea = () => fixture.debugElement.query(By.css('textarea[formControlName="description"]'));

  describe('Tests unitaires (DOM, formulaire et teachers déjà résolus)', () => {

    it('should create', () => {
      renderCreateForm();
      expect(component).toBeTruthy();
    });

    it('should display "Create session" as the title in create mode', () => {
      renderCreateForm();

      expect((formTitle().nativeElement as HTMLElement).textContent).toContain('Create session');
    });

    it('should display "Update session" as the title in update mode', () => {
      renderUpdateForm();

      expect((formTitle().nativeElement as HTMLElement).textContent).toContain('Update session');
    });

    it('save button should be disabled while the form is invalid and enabled when valid', () => {
      renderCreateForm();

      component.sessionForm?.setValue({ ...validFormValue, name: '' });
      fixture.detectChanges();
      expect((saveButton().nativeElement as HTMLButtonElement).disabled).toBe(true);

      component.sessionForm?.setValue(validFormValue);
      fixture.detectChanges();
      expect((saveButton().nativeElement as HTMLButtonElement).disabled).toBe(false);
    });

    it('description should be invalid beyond 2000 characters', () => {
      renderCreateForm();

      const description = component.sessionForm?.get('description');
      description?.setValue('a'.repeat(2001));

      expect(description?.valid).toBe(false);
      expect(description?.hasError('maxlength')).toBe(true);
    });

    it.each([
      ['name', ''],
      ['date', ''],
      ['teacher_id', ''],
      ['description', ''],
    ])('%s should be invalid when empty', (controlName) => {
      renderCreateForm();

      const control = component.sessionForm?.get(controlName as string);
      control?.setValue('');

      expect(control?.valid).toBe(false);
      expect(control?.hasError('required')).toBe(true);
    });

    it('should prefill the form with the fetched session in update mode', () => {
      renderUpdateForm();

      expect(nameInput().nativeElement.value).toBe(mockSession.name);
      expect(dateInput().nativeElement.value).toBe('2024-01-01');
      expect(descriptionTextarea().nativeElement.value).toBe(mockSession.description);
      expect(component.sessionForm?.get('teacher_id')?.value).toBe(mockSession.teacher_id);
    });

    it('should render one option per teacher received from the API', () => {
      renderCreateForm();

      // ouverture du select.
      fixture.debugElement.query(By.css('[data-testid="teacher-select"]')).nativeElement.click();
      fixture.detectChanges();

      const options = document.querySelectorAll('mat-option');

      expect(options.length).toBe(mockTeachers.length);
      expect(options[0].textContent).toContain('John');
      expect(options[1].textContent).toContain('Jane');
    });

    it('should not send a create/update HTTP request when the form is invalid on submit', () => {
      renderCreateForm();

      component.sessionForm?.setValue({ ...validFormValue, name: '' });
      fixture.detectChanges();
      expect((saveButton().nativeElement as HTMLButtonElement).disabled).toBe(true);

      saveButton().nativeElement.click();
      fixture.detectChanges();

      httpMock.expectNone({ url: 'api/session' });
    });
  });

  describe("Tests d'intégration (SessionApiService/TeacherService réels + HttpClientTesting)", () => {

    it('should only fetch the teachers on init in create mode', () => {
      mockRouter.url = '/sessions/create';
      fixture = TestBed.createComponent(FormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      httpMock.expectNone({ url: 'api/session/1' });

      const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher' });
      expect(teacherReq.request.method).toBe('GET');
      teacherReq.flush(mockTeachers);
    });

    it('should fetch the session then the teachers on init in update mode', () => {
      mockRouter.url = '/sessions/update/1';
      fixture = TestBed.createComponent(FormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      const sessionReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
      expect(sessionReq.request.method).toBe('GET');
      sessionReq.flush(mockSession);
      fixture.detectChanges();

      const teacherReq: TestRequest = httpMock.expectOne({ url: 'api/teacher' });
      expect(teacherReq.request.method).toBe('GET');
      teacherReq.flush(mockTeachers);
    });

    it('should redirect a non-admin user to /sessions on init without fetching anything', () => {
      mockSessionService.sessionInformation.admin = false;
      mockRouter.url = '/sessions/create';

      fixture = TestBed.createComponent(FormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/sessions']);
      expect(component.sessionForm).toBeUndefined();
      httpMock.expectNone({ url: 'api/teacher' });
      httpMock.expectNone({ url: 'api/session/1' });
    });

    it('should create the session, show a snackbar and navigate to sessions on submit()', () => {
      renderCreateForm();

      component.sessionForm?.setValue(validFormValue);
      fixture.detectChanges();

      saveButton().nativeElement.click();

      const createReq: TestRequest = httpMock.expectOne({ url: 'api/session' });
      expect(createReq.request.method).toBe('POST');
      expect(createReq.request.body).toEqual(validFormValue);
      createReq.flush(mockSession);

      expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session created !', 'Close', { duration: 3000 });
      expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
    });

    it('should update the session, show a snackbar and navigate to sessions on submit()', () => {
      renderUpdateForm();

      component.sessionForm?.setValue(validFormValue);
      fixture.detectChanges();

      saveButton().nativeElement.click();

      const updateReq: TestRequest = httpMock.expectOne({ url: 'api/session/1' });
      expect(updateReq.request.method).toBe('PUT');
      expect(updateReq.request.body).toEqual(validFormValue);
      updateReq.flush(mockSession);

      expect(mockMatSnackBar.open).toHaveBeenCalledWith('Session updated !', 'Close', { duration: 3000 });
      expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
    });
  });
});
