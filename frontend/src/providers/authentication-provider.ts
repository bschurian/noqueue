import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import { HttpProvider } from '../providers/http-provider';
import { Storage } from '@ionic/storage';
import { JwtHelper } from 'angular2-jwt';
import { Observable } from 'rxjs/Observable';

/*
  Generated class for the AuthenticationProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
export class AuthenticationProvider {

  private token: string;
  private userID: string;

  constructor(public http: Http, private httpProvider: HttpProvider, private storage: Storage, private jwtHelper: JwtHelper) {
    if(this.storage){
      this.storage.get('token').then(
        (token) => this.token = token
      );
    }
  }

  asyncSetup(){
    let auth = this;
    return new Promise(function(resolve, reject){
      auth.storage.keys().then(
        (keys) => {
          if(!keys.includes('token')){
            resolve();
          } else {
            auth.storage.get('token').then(
              (token) => {
                auth.token = token;
                auth.httpProvider.setToken(token);
                auth.decodeUserID();
                resolve()
              },
              () => reject()
            )
          }
        }
      );
    });
  }

  login(username: string, password: string): Promise<any>{
    let auth = this;
    let body = JSON.stringify({nutzerName: username, password});
    return new Promise(function(resolve, reject){
      auth.httpProvider.post(auth.httpProvider.ROUTES.authentication, body)
        .subscribe(
          (token) => {
            auth.storage.set('token', token);
            auth.token = token;
            auth.httpProvider.readToken();
            resolve("Logged In");
          },
          (error) => reject(error.message)
        )
    });
  }

  signup(username: string, email: string, password: string): Promise<any>{
    let auth = this;
    let body = JSON.stringify({nutzerName: username, nutzerEmail: email, password});
    return new Promise(function(resolve, reject){
      auth.httpProvider.post(auth.httpProvider.ROUTES.users, body)
        .subscribe(
          (token) => {
            auth.storage.set('token', token);
            auth.token = token;
            auth.httpProvider.readToken();
            resolve("Signed Up");
          },
          (error) => reject(error.message)
        )
    })
  }

  logout(){
    this.resetToken();
  }

  resetToken(){
    this.token = "";
    this.storage.remove('token');
    this.httpProvider.readToken();
  }

  isLoggedIn(): boolean{
    return this.token !== undefined && this.token !== "";
  }

  getToken(): string{
    return this.token;
  }

  decodeUserID(){
    this.userID = this.jwtHelper.decodeToken(this.token).userId;
  }

  getUserId(): any{
    return this.userID;
  }

}