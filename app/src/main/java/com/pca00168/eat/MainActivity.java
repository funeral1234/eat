package com.pca00168.eat;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Person;
import java.io.IOException;
import java.util.Collections;
public class MainActivity extends Activity {
    GoogleSignInAccount google_user;
    GoogleSignInClient mGoogleSignInClient;
    private static final String CONTACTS_SCOPE = "https://www.googleapis.com/auth/user.gender.read";
    private static final String CONTACTS_SCOPE2 = "https://www.googleapis.com/auth/user.birthday.read";
    private static final String CONTACTS_SCOPE3 = "https://www.googleapis.com/auth/fitness.activity.read";
    private static final String CONTACTS_SCOPE4 = "https://www.googleapis.com/auth/fitness.activity.write";
    private static final String CONTACTS_SCOPE5 = "https://www.googleapis.com/auth/fitness.nutrition.read";
    private static final String CONTACTS_SCOPE6 = "https://www.googleapis.com/auth/fitness.nutrition.write";
    private static final String CONTACTS_SCOPE7 = "https://www.googleapis.com/auth/fitness.sleep.read";
    private static final String CONTACTS_SCOPE8 = "https://www.googleapis.com/auth/fitness.sleep.write";
    private static final int google_fit_const = 0;
    public void google_click(View view){
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        findViewById(R.id.start_or_not).setVisibility(View.INVISIBLE);
        findViewById(R.id.google_login).setVisibility(View.INVISIBLE);
        startActivityForResult( mGoogleSignInClient.getSignInIntent(), 1);
        }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtils.setWindowStatusBarColor(this,R.color.black);


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .requestScopes(new Scope(CONTACTS_SCOPE))
                .requestScopes(new Scope(CONTACTS_SCOPE2))
                .requestScopes(new Scope(CONTACTS_SCOPE3))
                .requestScopes(new Scope(CONTACTS_SCOPE4))
                .requestScopes(new Scope(CONTACTS_SCOPE5))
                .requestScopes(new Scope(CONTACTS_SCOPE6))
                .requestScopes(new Scope(CONTACTS_SCOPE7))
                .requestScopes(new Scope(CONTACTS_SCOPE8))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        if(!public_func.readData(this,"logged").isEmpty()){
            google_click(null);
        }
    }

    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                google_user = task.getResult(ApiException.class);
                if(google_user!=null){
                    if (GoogleSignIn.hasPermissions(google_user,new Scope(CONTACTS_SCOPE))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE2))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE3))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE4))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE5))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE6))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE7))&&
                            GoogleSignIn.hasPermissions(google_user, new Scope(CONTACTS_SCOPE8)) ) {
                        public_func.writeData(this,"name",google_user.getDisplayName());
                        try{
                            public_func.writeData(this,"avatar",google_user.getPhotoUrl().toString());
                        }catch (NullPointerException e){
                            public_func.writeData(this,"avatar","https://cdn.cybassets.com/media/W1siZiIsIjExMTE0L3Byb2R1Y3RzLzM3NTU1MDU4L1RBNzI2NjEtMF8zYjMyZmRmNmIyMmQ3YmUzMGI5Mi5qcGVnIl0sWyJwIiwidGh1bWIiLCI0ODB4NDgwIl1d.jpeg?sha=d97d6847e727ecff");
                        }
                        public_func.writeData(this,"email",google_user.getEmail());
                        requestAccountInfo(google_user);
                    } else
                        startActivityForResult( mGoogleSignInClient.getSignInIntent(), 1);
                }
            } catch (ApiException e) {
                err(e.getMessage()+"登入失敗");
                        findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                        findViewById(R.id.start_or_not).setVisibility(View.VISIBLE);
                        findViewById(R.id.google_login).setVisibility(View.VISIBLE);
            }
        }
    }
    private void requestAccountInfo(GoogleSignInAccount account) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(MainActivity.this, Collections.singleton(CONTACTS_SCOPE)).setSelectedAccount(account.getAccount());
                    PeopleService peopleService = new PeopleService.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), googleAccountCredential).build();
                    Person person = peopleService.people()
                            .get("people/me")
                            .setRequestMaskIncludeField("person.genders")
                            .setFields("genders")
                            .execute();
                    for (Gender gender : person.getGenders()) {
                        public_func.writeData(getBaseContext(),"sex",gender.getValue());
                    }

                     person = peopleService.people()
                            .get("people/me")
                            .setRequestMaskIncludeField("person.birthdays")
                            .setFields("birthdays")
                            .execute();
                    for (Birthday day : person.getBirthdays()) {
                        public_func.writeData(getBaseContext(),"birthday",
                               String.format ("%d/%d/%d",day.getDate().getYear(),day.getDate().getMonth(),day.getDate().getDay())
                        );
                    }
                    public_func.writeData(getBaseContext(),"logged","google");
                    logged_in();
                } catch (IOException e) { err("生日及性別取得失敗"); }
            }
        }).start();
    }

    private void logged_in(){
        startActivity(new Intent(this,Home.class));
        finish();
    }

    private void err(String msg){
        Toast.makeText(this,msg , Toast.LENGTH_LONG).show();
    }
}