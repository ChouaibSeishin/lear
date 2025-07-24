import { Injectable } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, filter } from 'rxjs';

export interface Breadcrumb {
  label: string;
  url: string;
}

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  private breadcrumbsSubject = new BehaviorSubject<Breadcrumb[]>([]);
  breadcrumbs$ = this.breadcrumbsSubject.asObservable();

  constructor(private router: Router, private route: ActivatedRoute) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        const breadcrumbs = this.buildBreadcrumbs(this.route.root);
        this.breadcrumbsSubject.next(breadcrumbs);
      });
  }

  private buildBreadcrumbs(
    route: ActivatedRoute,
    url: string = '',
    breadcrumbs: Breadcrumb[] = []
  ): Breadcrumb[] {
    const routeConfig = route.routeConfig;
    const routeData = route.snapshot.data;

    if (routeConfig?.path && routeData?.['title']) {
      let path = routeConfig.path;

      // Replace route params like :id with actual values
      const paramNames = path.match(/:([^/]+)/g) || [];
      for (const paramName of paramNames) {
        const cleanName = paramName.substring(1);
        const value = route.snapshot.paramMap.get(cleanName);
        if (value) {
          path = path.replace(paramName, value);
        }
      }

      url += `/${path}`;
      let label = routeData['title'];

      // Optional: if label contains ':id' or similar, replace with value
      if (label.includes(':')) {
        paramNames.forEach(param => {
          const clean = param.substring(1);
          const value = route.snapshot.paramMap.get(clean);
          if (value) {
            label = label.replace(param, value);
          }
        });
      }

      breadcrumbs.push({ label, url });
    }

    for (const child of route.children) {
      return this.buildBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }
  setCustomLabelForLast(label: string) {
    const breadcrumbs = this.breadcrumbsSubject.value;
    if (breadcrumbs.length > 0) {
      breadcrumbs[breadcrumbs.length - 1].label = label;
      this.breadcrumbsSubject.next([...breadcrumbs]); // emit a new array
    }
  }

}
