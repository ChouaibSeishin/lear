import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import {CodeInputModule} from "angular-code-input";
import { NgScrollbar } from 'ngx-scrollbar';
import { DashboardComponent } from './shared/dashboard/dashboard.component';



import { HeaderComponent } from './shared/layout/header/header.component';
import { FooterComponent } from './shared/layout/footer/footer.component';
import { SidebarComponent } from './shared/layout/sidebar/sidebar.component';
import { DefaultLayoutComponent } from './shared/layout/default-layout/default-layout.component';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AuthInterceptor } from './interceptor/auth-interceptor.interceptor';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';
import { AddUserComponent } from './admin/add-user/add-user.component';
import {MatInput, MatInputModule} from "@angular/material/input";
import {MatButton, MatButtonModule} from "@angular/material/button";
import {MatSelect, MatSelectModule} from "@angular/material/select";
import {FlexLayoutModule} from "@angular/flex-layout";
import { UsersComponent } from './shared/users/users.component';
import {MatTable, MatTableModule} from "@angular/material/table";
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";
import { ActivateAccountComponent } from './activate-account/activate-account.component';
import {ToastrModule} from "ngx-toastr";
import { ProjectsComponent } from './shared/projects/projects.component';
import { ProjectDetailsComponent } from './shared/project-details/project-details.component';
import { MachinesComponent } from './shared/machines/machines.component';
import { MachineDetailsComponent } from './shared/machine-details/machine-details.component';
import { ProductionLineComponent } from './shared/production-line/production-line.component';
import { ProductionLineDetailsComponent } from './shared/production-line-details/production-line-details.component';
import { CycleTimesComponent } from './shared/cycle-times/cycle-times.component';
import { StepsComponent } from './shared/steps/steps.component';
import {BaseChartDirective, provideCharts, withDefaultRegisterables} from "ng2-charts";
import { UserDetailsComponent } from './shared/user-details/user-details.component';
import { ProfileComponent } from './shared/profile/profile.component';
import {NgbModule, NgbPopoverModule} from '@ng-bootstrap/ng-bootstrap';
import {MatBadgeModule} from "@angular/material/badge";
import { CycleTimeDetailsComponent } from './shared/cycle-time-details/cycle-time-details.component';
import { ImportComponent } from './shared/import/import.component';
import {DragDropModule} from "@angular/cdk/drag-drop";
import {ChatBotModalComponent} from "./shared/chat-bot-modal/chat-bot-modal.component";
import {Nl2brPipe} from "./services/api-chat-bot/pipes/nl2br.pipe";



@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HeaderComponent,
    FooterComponent,
    SidebarComponent,
    DefaultLayoutComponent,
    UnauthorizedComponent,
    AddUserComponent,
    UsersComponent,
    ActivateAccountComponent,
    ProjectsComponent,
    ProjectDetailsComponent,
    MachinesComponent,
    MachineDetailsComponent,
    ProductionLineComponent,
    ProductionLineDetailsComponent,
    CycleTimesComponent,
    StepsComponent,
    DashboardComponent,
    UserDetailsComponent,
    ProfileComponent,
    CycleTimeDetailsComponent,
    ImportComponent,
    ChatBotModalComponent,

  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    RouterModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    NgScrollbar,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    FlexLayoutModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    CodeInputModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(),
    BaseChartDirective,
    NgbModule,
    NgbPopoverModule,
    MatBadgeModule,
    DragDropModule,
    Nl2brPipe,


  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    provideCharts(withDefaultRegisterables())
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  bootstrap: [AppComponent]
})
export class AppModule { }
