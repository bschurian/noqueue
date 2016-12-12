import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { IonicApp, IonicModule } from 'ionic-angular';
import { MyApp } from './app.component';
import { HomePage } from '../pages/home/home';
import { LoginPage } from '../pages/login/login';
import { MainPage } from '../pages/main/main';
import { Account } from '../pages/account/account';
import { MyService } from '../pages/my-service/my-service';
import { QueuePosition } from '../pages/queue-position/queue-position';
import { ServicesPage } from '../pages/services-page/services-page';
import { ForgotPassword } from '../pages/forgot-password/forgot-password';
import {SignUpPage} from "../pages/signup/signup";
import { SingleService } from "../pages/single-service/single-service";
import {ServicesData} from "../providers/data";
import { UsersProvider } from "../providers/users"
import { AuthenticationProvider } from "../providers/authentication";
import { HttpConfig } from "../providers/http-config";
import { ProfileInfoPage } from "../pages/profile-info/profile-info";
// import { Data } from '../providers/data.ts';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/filter';
import {HttpService} from "../providers/http-service";
// import { AUTH_PROVIDERS } from 'angular2-jwt';
import { AuthHttp, AuthConfig } from 'angular2-jwt';
import { Http } from '@angular/http';
import { Storage } from '@ionic/storage';
import {ServicesProvider} from "../providers/services-provider";
import {NewServicePage} from "../pages/new-service/new-service";
import { JwtHelper } from "angular2-jwt";

let storage = new Storage();

export function getAuthHttp(http) {
  return new AuthHttp(new AuthConfig({
    headerPrefix: "X-Auth-Token",
    noJwtError: true,
    globalHeaders: [{'Accept': 'application/json'}],
    tokenGetter: (() => storage.get('id_token')),
  }), http);
}

@NgModule({
  declarations: [
    MyApp,
    HomePage,
    LoginPage,
    MainPage,
    Account,
    MyService,
    QueuePosition,
    ServicesPage,
    SignUpPage,
    ForgotPassword,
    SingleService,
    ProfileInfoPage,
    NewServicePage
  ],
  imports: [
    HttpModule,
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    HomePage,
    MainPage,
    LoginPage,
    Account,
    MyService,
    QueuePosition,
    ServicesPage,
    SignUpPage,
    ForgotPassword,
    SingleService,
    ProfileInfoPage,
    NewServicePage
  ],
  providers: [Storage, HttpService, ServicesData, UsersProvider, ServicesProvider, JwtHelper, AuthenticationProvider, HttpConfig, {
    provide: AuthHttp,
    useFactory: getAuthHttp,
    deps: [Http]
  }]
})
export class AppModule {}
