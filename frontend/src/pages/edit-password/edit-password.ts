import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { ToastController } from 'ionic-angular';
// custom providers
import { UsersProvider } from '../../providers/users-provider';
import { ValidatorProvider } from '../../providers/validator-provider';
import { ConnectivityProvider } from '../../providers/connectivity-provider';


/*
  Generated class for the EditPassword page.

  See http://ionicframework.com/docs/v2/components/#navigation for more info on
  Ionic pages and navigation.
*/
@Component({
  selector: 'page-edit-password',
  templateUrl: 'edit-password.html',
  providers: [ ValidatorProvider, UsersProvider, ConnectivityProvider ]
})
export class EditPasswordPage {

// declare variables used by the HTML template (ViewModel)

  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
  error: boolean = false;
  email: string = "";
  username: string = "";
  validationRules: any;
  isValid = {
    oldPassword: true,
    newPassword: true,
    confirmPassword: true,
    passwordsMatching: true,
    differentPasswords: true
  };
  allFieldsValid: boolean = false;

// constructor and lifecycle-events (chronological order)

  constructor(public navCtrl: NavController, public users: UsersProvider, public validator: ValidatorProvider, public connectivity: ConnectivityProvider,
  public toast: ToastController) {
    this.validationRules = {
      emptyPassword: this.validator.rules.emptyPassword,
      newPassword: this.validator.rules.newPassword,
      samePassword: this.validator.rules.samePassword,
      passwordsMatching: this.validator.rules.passwordMatching
    }
  }

  ionViewDidLoad() : void{
    this.resetError();

    this.users.getMe()
      .subscribe(
        (user) => {
            this.email = user.nutzerEmail;
            this.username = user.nutzerName;
        },
        (error) => {
          this.registerError("Error while fetching your information.");
        }
      )
  }

  ionViewWillEnter() : void{
    this.connectivity.checkNetworkConnection();
  }

// ViewModel logic (working with the data)

  checkOldPassword() : void{
    this.isValid.oldPassword = !this.validator.empty(this.oldPassword);
    this.checkDifferentPasswords();
    this.checkAllFields();
  }

  checkNewPassword() : void{
    this.isValid.newPassword = this.validator.password(this.newPassword);
    this.checkDifferentPasswords();
    this.checkPasswordsMatching();
  }

  checkConfirmPassword() : void{
    this.isValid.confirmPassword = this.validator.password(this.confirmPassword);
    this.checkPasswordsMatching();
  }

  checkPasswordsMatching() : void{
    this.isValid.passwordsMatching = this.validator.passwordsMatching(this.newPassword, this.confirmPassword);
    this.checkAllFields();
  }

  checkDifferentPasswords() : void{
    this.isValid.differentPasswords = !this.validator.passwordsMatching(this.oldPassword, this.newPassword);
  }

  checkAllFields() : void{
    var valid = true;
    if(this.validator.empty(this.oldPassword, this.newPassword, this.confirmPassword)){
      valid = false;
    } else {
      for(let attr in this.isValid){
        if(this.isValid[attr] == false) valid = false;
      }
    }
    this.allFieldsValid = valid;
  }

  checkInput() : void{
    this.checkOldPassword();
    this.checkNewPassword();
    this.checkConfirmPassword();
  }

  changePassword() : void{
    this.resetError();
    this.checkInput();
    if(!this.allFieldsValid) return;

    this.users.changePassword({username: this.username, email: this.email, oldPassword: this.oldPassword, newPassword: this.newPassword})
      .subscribe(
        () => this.navCtrl.pop(),
        (error) => {
          let jsonError = JSON.parse(error._body);
          if(jsonError.code == 500){
            this.registerError("False old password.");
          } else {
            this.registerError("Couldn't change the server. Please try again later.")
          }
        }
      );
  }

  registerError(message: string) : void{
    this.error = true;
    let toast = this.toast.create({
      message: message,
      duration: 3000
    });
    toast.present();
  }

  resetError() : void{
    this.error = false;
  }
}
