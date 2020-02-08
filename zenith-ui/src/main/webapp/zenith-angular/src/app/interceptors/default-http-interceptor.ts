import { Injectable } from "@angular/core";
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Observable } from "rxjs";

const APPLICATION_JSON: string = 'application/json';

@Injectable()
export class DefaultHttpInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    request = request.clone({headers: request.headers.set('Content-Type', APPLICATION_JSON)});
    request = request.clone({headers: request.headers.set('Accept', APPLICATION_JSON)});

    return next.handle(request);
  }
}
