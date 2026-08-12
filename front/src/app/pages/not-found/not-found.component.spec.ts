import { ComponentFixture, TestBed } from '@angular/core/testing';
import {expect, it} from '@jest/globals';

import { NotFoundComponent } from './not-found.component';
import {By} from "@angular/platform-browser";

describe('NotFoundComponent', () => {
  let component: NotFoundComponent;
  let fixture: ComponentFixture<NotFoundComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotFoundComponent
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotFoundComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const pageNotFoundText = () => fixture.debugElement.query(By.css('[data-testid="not-found"]'));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display page not found', () => {
    expect((pageNotFoundText().nativeElement as HTMLElement).textContent).toContain('Page not found !');
  });
});
