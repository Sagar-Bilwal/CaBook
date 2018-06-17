package com.example.sagar.cabook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnRegister,btnSignIn;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference users;
    ConstraintLayout constraintLayout;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser)
    {
        startActivity(new Intent(this,WelcomeScreen.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Lobster_1.3.otf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);
        btnRegister=findViewById(R.id.btnRegister);
        btnSignIn=findViewById(R.id.btnSignIn);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=firebaseDatabase.getInstance();
        users=firebaseDatabase.getReference("Users");
        constraintLayout=findViewById(R.id.partnerLayout);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInDialog();
            }
        });
    }
    MaterialEditText signEmail,signPassword;
    private void showSignInDialog()
    {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Sign In");
        dialog.setMessage("Please use Email to Sign In");

        LayoutInflater layoutInflater=LayoutInflater.from(this);
        final View sign_in_layout=layoutInflater.inflate(R.layout.activity_sign_in,null);
        signEmail=sign_in_layout.findViewById(R.id.signInEmail);
        signPassword=sign_in_layout.findViewById(R.id.signInPassword);
        dialog.setView(sign_in_layout);
        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                btnSignIn.setEnabled(false);
                if(TextUtils.isEmpty(signEmail.getText().toString()))
                {
                    Snackbar.make(constraintLayout,"Please Enter Your Email",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(TextUtils.isEmpty(signPassword.getText().toString()))
                {
                    Snackbar.make(constraintLayout,"Please Enter the Password",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(signPassword.getText().toString().length()<8)
                {
                    Snackbar.make(constraintLayout,"The Password is too Short",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else
                {
                    final SpotsDialog alertDialog=new SpotsDialog(MainActivity.this);
                    alertDialog.show();
                    firebaseAuth.signInWithEmailAndPassword(signEmail.getText().toString(),signPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            alertDialog.dismiss();
                            startActivity(new Intent(MainActivity.this,WelcomeScreen.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            alertDialog.dismiss();
                            btnSignIn.setEnabled(true);
                            Snackbar.make(constraintLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    MaterialEditText regEmail,regPassword,regName,regPhoneNo;
    private void showRegisterDialog()
    {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please use Email to Register");

        LayoutInflater layoutInflater=LayoutInflater.from(this);
        final View register_layout=layoutInflater.inflate(R.layout.activity_register,null);
        regEmail=register_layout.findViewById(R.id.regEmail);
        regPassword=register_layout.findViewById(R.id.regPassword);
        regName=register_layout.findViewById(R.id.regName);
        regPhoneNo=register_layout.findViewById(R.id.regPhoneNo);
        dialog.setView(register_layout);
        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                if(TextUtils.isEmpty(regEmail.getText().toString()))
                {
                    Snackbar.make(constraintLayout,"Please Enter Your Email",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(TextUtils.isEmpty(regPassword.getText().toString()))
                {
                    Snackbar.make(constraintLayout,"Please Enter the Password",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(TextUtils.isEmpty(regName.getText().toString()))
                {
                    Snackbar.make(constraintLayout,"Please Enter Your Name",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(TextUtils.isEmpty(regPhoneNo.getText().toString()))
                {
                    Snackbar.make(constraintLayout,"Please Enter Your Phone No.",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(regPassword.getText().toString().length()<8)
                {
                    Snackbar.make(constraintLayout,"The Password is too Short",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if(regPhoneNo.getText().toString().length()!=10)
                {
                    Snackbar.make(constraintLayout,"Please Enter Correct Phone No.",Snackbar.LENGTH_LONG).show();
                    return;
                }
                else
                {
                    final SpotsDialog alertDialog=new SpotsDialog(MainActivity.this);
                    alertDialog.show();
                    firebaseAuth.createUserWithEmailAndPassword(regEmail.getText().toString(),regPassword.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult)
                                {
                                    alertDialog.dismiss();
                                    User user=new User();
                                    user.setEmail(regEmail.getText().toString());
                                    user.setName(regName.getText().toString());
                                    user.setPassword(regPassword.getText().toString());
                                    user.setPhoneNo(regPhoneNo.getText().toString());
                                    String email=user.getEmail();
                                    users.child(firebaseAuth.getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(constraintLayout,"Registration Successful",Snackbar.LENGTH_LONG).show();
                                            return;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            alertDialog.dismiss();
                                            Snackbar.make(constraintLayout,"Registration Failed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                                        return;
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(constraintLayout,"Registration Failed"+e.getMessage(),Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    });
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
