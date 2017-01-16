import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Observable';
import { HttpProvider } from '../providers/http-provider';
import { AuthenticationProvider } from '../providers/authentication-provider';

/*
  Generated class for the UsersProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class UsersProvider {

  constructor(public http: Http, private httpProvider: HttpProvider, private auth: AuthenticationProvider) {
  }

  getUsersWithName(name: string) : Observable<any>{
    let searchOptions = { q: name };
    return this.httpProvider.get(this.httpProvider.ROUTES.users + "/directory", searchOptions);
  }

  getUser(id): Observable<any>{
    return this.httpProvider.get(this.httpProvider.ROUTES.users + "/directory/" + id);
  }

  getMe() : Observable<any>{
    // return this.httpProvider.get(this.httpProvider.ROUTES.users + "/" + this.auth.getUserId());
    return this.httpProvider.get(this.httpProvider.ROUTES.users);
  }

  changeProfileInfo(data: any) : Observable<any>{
    let body = {
      nutzerName: data.username,
      nutzerEmail: data.email,
      adresse: {
        plz: data.zip,
        stadt: data.city,
        strasse: data.street,
        hausNummer: data.streetNr
      }
    };
    return this.httpProvider.put(this.httpProvider.ROUTES.users, body);
  }

  changePassword(data: any) : Observable<any>{
    return this.httpProvider.put(this.httpProvider.ROUTES.users + "/password", {oldPassword: data.oldPassword, newPassword: data.newPassword, nutzerName: data.username, nutzerEmail: data.email});
  }
}
