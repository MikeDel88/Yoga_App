import { HttpClient } from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import { Observable } from 'rxjs';
import { Teacher } from '../models/teacher.interface';

@Injectable({
  providedIn: 'root'
})
export class TeacherService {

  private pathService: string = 'api/teacher';

  private httpClient: HttpClient = inject(HttpClient);

  public all(): Observable<Teacher[]> {
    return this.httpClient.get<Teacher[]>(this.pathService);
  }

  public detail(id: string): Observable<Teacher> {
    return this.httpClient.get<Teacher>(`${this.pathService}/${id}`);
  }
}
