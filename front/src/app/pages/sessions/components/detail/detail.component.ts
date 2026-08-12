import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { Teacher } from '../../../../core/models/teacher.interface';
import { SessionService } from '../../../../core/service/session.service';
import { TeacherService } from '../../../../core/service/teacher.service';
import { Session } from '../../../../core/models/session.interface';
import { SessionApiService } from '../../../../core/service/session-api.service';
import { MaterialModule } from "../../../../shared/material.module";
import { CommonModule } from "@angular/common";
import {FlexLayoutModule} from "@angular/flex-layout";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";

@Component({
  selector: 'app-detail',
  imports: [CommonModule, MaterialModule, FlexLayoutModule],
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent implements OnInit {
  public session: Session | undefined;
  public teacher: Teacher | undefined;
  public isParticipate: boolean = false;
  public isAdmin: boolean = false;
  public sessionId: string;
  public userId: string;

  private route: ActivatedRoute = inject(ActivatedRoute);
  private sessionService: SessionService = inject(SessionService);
  private sessionApiService: SessionApiService = inject(SessionApiService);
  private teacherService: TeacherService = inject(TeacherService);
  private matSnackBar: MatSnackBar = inject(MatSnackBar);
  private router: Router = inject(Router);
  private destroyRef: DestroyRef = inject(DestroyRef);

  constructor() {
    this.sessionId = this.route.snapshot.paramMap.get('id')!;
    this.isAdmin = this.sessionService.sessionInformation!.admin;
    this.userId = this.sessionService.sessionInformation!.id.toString();
  }

  ngOnInit(): void {
    this.fetchSession();
  }

  public back(): void {
    window.history.back();
  }

  public delete(): void {
    this.sessionApiService
      .delete(this.sessionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((): void => {
          this.matSnackBar.open('Session deleted !', 'Close', { duration: 3000 });
          this.router.navigate(['sessions']);
        }
      );
  }

  public participate(): void {
    this.sessionApiService.participate(this.sessionId, this.userId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((): void => this.fetchSession());
  }

  public unParticipate(): void {
    this.sessionApiService.unParticipate(this.sessionId, this.userId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((): void => this.fetchSession());
  }

  private fetchSession(): void {
    this.sessionApiService
      .detail(this.sessionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((session: Session): void => {
        this.session = session;
        this.isParticipate = session.users.some((id: number) => id === this.sessionService.sessionInformation!.id);
        this.teacherService
          .detail(session.teacher_id.toString())
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe((teacher: Teacher): Teacher => this.teacher = teacher);
      });
  }

}
