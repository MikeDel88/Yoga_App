import {inject, Injectable} from "@angular/core";
import {CanActivate, Router} from "@angular/router";
import { SessionService } from "../core/service/session.service";

@Injectable({providedIn: 'root'})
export class UnauthGuard implements CanActivate {

  private router: Router = inject(Router)
  private sessionService: SessionService = inject(SessionService)

  public canActivate(): boolean {
    if (!this.sessionService.isLogged) {
      return true;
    }

    this.router.navigate(['sessions']);
    return false;
  }
}
