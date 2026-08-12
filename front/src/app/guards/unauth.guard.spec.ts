import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { SessionService } from '../core/service/session.service';

import { UnauthGuard } from './unauth.guard';

describe('UnauthGuard', () => {
  let guard: UnauthGuard;
  let mockSessionService: { isLogged: boolean };
  let mockRouter: { navigate: jest.Mock };

  beforeEach(() => {
    mockSessionService = { isLogged: false };
    mockRouter = { navigate: jest.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter },
      ],
    });

    guard = TestBed.inject(UnauthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow activation and not navigate when the user is not logged in', () => {
    mockSessionService.isLogged = false;

    expect(guard.canActivate()).toBe(true);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should deny activation and navigate to /sessions when the user is already logged in', () => {
    mockSessionService.isLogged = true;

    expect(guard.canActivate()).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
  });
});
