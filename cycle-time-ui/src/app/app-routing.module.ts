import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {DashboardComponent} from "./shared/dashboard/dashboard.component";
import {DefaultLayoutComponent} from "./shared/layout/default-layout/default-layout.component";
import {authenticationGuard} from "./guards/authentication-guard.guard";
import {UnauthorizedComponent} from "./unauthorized/unauthorized.component";
import {AddUserComponent} from "./admin/add-user/add-user.component";
import {UsersComponent} from "./shared/users/users.component";
import {ActivateAccountComponent} from "./activate-account/activate-account.component";
import {ProjectsComponent} from "./shared/projects/projects.component";
import {authorizationGuard} from "./guards/authorization-guard.guard";
import {ProjectDetailsComponent} from "./shared/project-details/project-details.component";
import {MachinesComponent} from "./shared/machines/machines.component";
import {MachineDetailsComponent} from "./shared/machine-details/machine-details.component";
import {ProductionLineComponent} from "./shared/production-line/production-line.component";
import {ProductionLineDetailsComponent} from "./shared/production-line-details/production-line-details.component";
import {CycleTimesComponent} from "./shared/cycle-times/cycle-times.component";
import {UserDetailsComponent} from "./shared/user-details/user-details.component";
import {ProfileComponent} from "./shared/profile/profile.component";
import {CycleTimeDetailsComponent} from "./shared/cycle-time-details/cycle-time-details.component";
import {ImportComponent} from "./shared/import/import.component";


export const routes: Routes = [
  {
    path: 'login',
    component:LoginComponent,
    pathMatch: 'full'
  },
  {path: "activate-account", component: ActivateAccountComponent},

  { path: 'unauthorized',
    component: UnauthorizedComponent },
  {
    path: '',
    canActivate: [authenticationGuard],
    component: DefaultLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        component: DashboardComponent,
        data: { title: 'Dashboard' }
      },
      {
        path: 'profile',
        component: ProfileComponent,
        data: { title: 'profile' }
      },
      {
        path: 'users',
        component:UsersComponent,
        data: { title: 'users' },

        children:[
          {
            path: '',
            redirectTo: 'users',
            pathMatch: 'full'
          },
          {
            path: 'user-details/:email',
            component:UserDetailsComponent,
            data: { title: ':email' }

          }
        ]
      },
      {
        path: 'cycle-times',
        component:CycleTimesComponent,
        data: { title: 'cycle-times' },
        children: [
          {
            path: '',
            redirectTo: 'cycle-times',
            pathMatch: 'full'
          },
          {
            path: 'cycle-time-details/:id',
            component:CycleTimeDetailsComponent,
            data: { title: ':id' }

          }
        ]
      },
      {
        path: 'production-lines',
        component:ProductionLineComponent,
        data: { title: 'production-line' },

        children: [
          {
            path: '',
            redirectTo: 'production-lines',
            pathMatch: 'full'
          },
          {
            path: 'production-line-details/:id',
            component:ProductionLineDetailsComponent,
            data: { title: ':id' }

          }
        ]
      },
      {
        path: 'machines',
        component: MachinesComponent,
        data: {title: 'machines'},
        canActivate: [authenticationGuard],
        children: [
          {
            path: '',
            redirectTo: 'machines',
            pathMatch: 'full'
          },
          {
            path: 'machine-details/:id',
            component:MachineDetailsComponent,
            data: { title: ':id' }

          }
        ]
      },
      {
        path: 'projects',
        component:ProjectsComponent,
        data: { title: 'projects' },
        canActivate:[authenticationGuard],
        children:[
          {
            path: '',
            redirectTo: 'projects',
            pathMatch: 'full'
          },
          {
            path: 'project-details/:id',
            component:ProjectDetailsComponent,
            data: { title: ':id' }

          },

        ]
      },


      {
        path:'admin',
        canActivate:[authorizationGuard],
        data: { title: 'admin' },
        children:[
          {
            path: '',
            redirectTo: 'dashboard',
            data: { title: 'dashboard' },
            pathMatch: 'full'
          },
          {
            path: 'add-user',
            component:AddUserComponent,
            data: { title: 'add-user' },
            pathMatch: 'full'
          },
          {
            path: 'import',
            component:ImportComponent,
            data: { title: 'import' },
            pathMatch: 'full'
          },


        ]
      },
    ]
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
