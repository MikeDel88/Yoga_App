import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../models/user.interface';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private pathService = 'api/user';

  private httpClient: HttpClient = inject(HttpClient);

  public getById(id: string): Observable<User> {
    return this.httpClient.get<User>(`${this.pathService}/${id}`);
  }

  public delete(id: string): Observable<unknown> {
    return this.httpClient.delete(`${this.pathService}/${id}`)
  }
}
