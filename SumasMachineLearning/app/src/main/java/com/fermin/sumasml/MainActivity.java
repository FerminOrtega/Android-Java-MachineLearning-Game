package com.fermin.sumasml;



import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import butterknife.ButterKnife;




public class MainActivity extends AppCompatActivity implements Lista.OnFragmentInteractionListener, Inicio.OnFragmentInteractionListener, Informacion.OnFragmentInteractionListener{

    public static ImageButton playButton;
    Lista lista = new Lista();
    Inicio inicio = new Inicio();
    public static FragmentTransaction transaction;
    public static FragmentTransaction transaction2;
    
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        transaction = getSupportFragmentManager().beginTransaction();
        playButton= (ImageButton) findViewById(R.id.playButton);
        //***************************
        transaction.replace(R.id.fragment, inicio);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    /**
     *
     * @param

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.playButton:
                Intent intent = new Intent(this, Juego_new.class);
                startActivity(intent);
                break;
            case R.id.imageButton2:
                linearLayout.setVisibility(View.VISIBLE);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.constanrt, lista);
                transaction.hide(lista);
                transaction.commit();
                break;
        }
    }

     **/
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}