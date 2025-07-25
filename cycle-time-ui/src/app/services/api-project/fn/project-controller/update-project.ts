/* tslint:disable */
/* eslint-disable */
/* Code generated by ng-openapi-gen DO NOT EDIT. */

import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { ProjectRequest } from '../../models/project-request';
import { ProjectResponse } from '../../models/project-response';

export interface UpdateProject$Params {
  id: number;
      body: ProjectRequest
}

export function updateProject(http: HttpClient, rootUrl: string, params: UpdateProject$Params, context?: HttpContext): Observable<StrictHttpResponse<ProjectResponse>> {
  const rb = new RequestBuilder(rootUrl, updateProject.PATH, 'put');
  if (params) {
    rb.path('id', params.id, {});
    rb.body(params.body, 'application/json');
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<ProjectResponse>;
    })
  );
}

updateProject.PATH = '/api/projects/{id}';
