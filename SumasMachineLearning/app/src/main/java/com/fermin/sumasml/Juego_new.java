package com.fermin.sumasml;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.fingerpaintview.FingerPaintView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Juego_new extends AppCompatActivity {

    private static final String LOG_TAG = Juego.class.getSimpleName();

    @BindView(R.id.btn_detect)
    Button detectar;

    @BindView(R.id.fpv_paint)
    FingerPaintView mFpvPaint;
    @BindView(R.id.tv_prediction)
    TextView mTvPrediction;
    @BindView(R.id.tv_probability) TextView mTvProbability;
    @BindView(R.id.tv_timecost) TextView mTvTimeCost;
    @BindView(R.id.Operacion) TextView operacion;
    @BindView(R.id.Comprobacion) TextView comprobacion;
    @BindView(R.id.highscoretext) TextView highscoretext;
    @BindView(R.id.score) TextView score;
    @BindView(R.id.nivel) TextView nivel;




    private Clasificador mClassifier;

    private static String dificultad;

    public static Context context;

    private String operamat;
    private String resultado;
    private static int aciertos;
    private static int mediadibujo;
    boolean newgame = false;
    public static boolean bandera = false;

    private static String user;



    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    Handler handler = new Handler();
    Runnable dormir = new Runnable() {

        @Override
        public void run() {
            comprobacion.setVisibility(View.INVISIBLE);
            Limpiar();
            lecturaOperaciones();
        }
    };

    CountDownTimer timer = new CountDownTimer(30000, 1000) {
        public void onTick(long millisUntilFinished) {
            highscoretext.setText("Segundos restantes: " + millisUntilFinished / 1000);
        }
        public void onFinish() {
            highscoretext.setText("Tiempo agotado!!!");
            timer.cancel();
            detectar.setEnabled(false);
            operacion.setVisibility(View.INVISIBLE);
            score.setVisibility(View.INVISIBLE);
            showChangeLangDialog("Has perdido\n"+getEmojiByUnicode(0x1F4A9)+getEmojiByUnicode(0x1F4A9)+getEmojiByUnicode(0x1F4A9), "#9C6310");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_game);
        ButterKnife.bind(this);
        init();

        context=getApplication().getBaseContext();
        dificultad = "CALCULOSN1";
        establecerNivel();
        lecturaOperaciones();
        newgame = true;
        mediadibujo = 0;

        contadorAciertos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClassifier.close();
    }

    @OnClick(R.id.btn_detect)
    void onDetectClick(){
        if (mClassifier == null) {
            Log.e(LOG_TAG, "onDetectClick(): Clasificador is not initialized");
            return;
        } else if (mFpvPaint.isEmpty()) {
            Toast.makeText(this, R.string.please_write_a_digit, Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap image = mFpvPaint.exportToBitmap(
                Clasificador.DIM_IMG_SIZE_WIDTH, Clasificador.DIM_IMG_SIZE_HEIGHT);
        Bitmap inverted = ImgUtilidad.invert(image);
        Resultado result = mClassifier.classify(inverted);
        renderResult(result);
    }

    @OnClick(R.id.btn_clear)
    void onClearClick() {
        mFpvPaint.clear();
        mTvPrediction.setText(R.string.empty);
        mTvProbability.setText(R.string.empty);
        mTvTimeCost.setText(R.string.empty);
    }

    private void Limpiar() {
        mFpvPaint.clear();
        mTvPrediction.setText(R.string.empty);
        mTvProbability.setText(R.string.empty);
        mTvTimeCost.setText(R.string.empty);

    }

    private void init() {
        try {
            mClassifier = new Clasificador(this);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Failed to create classifier.", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void renderResult(Resultado result) {
        mTvPrediction.setText(String.valueOf(result.getNumber()));
        operamat = operacion.getText().toString();

        mTvProbability.setText(String.valueOf(result.getProbability()));
        mTvTimeCost.setText(String.format(getString(R.string.timecost_value), result.getTimeCost()));

        if(result.getNumber() == Integer.parseInt(resultado)){
            operacion.setText(operamat+" "+resultado);
            comprobacion.setText("Correcto!!");
            comprobacion.setVisibility(View.VISIBLE);
            mediadibujo = mediadibujo+ (int) (result.getProbability()*10);
            timer.cancel();
            aciertos++;
            contadorAciertos();
            handler.postDelayed(dormir, 1000);
        }else{
            comprobacion.setText("Incorrecto");
            comprobacion.setVisibility(View.VISIBLE);
            Limpiar();
        }
    }

    private void lecturaOperaciones(){
        db.collection(dificultad)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int cont = 0;
                            int operacionrandom = new Random().nextInt(task.getResult().size()) + 1;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                cont ++;
                                if(cont == operacionrandom){
                                    operacion.setText(document.get("sumando1")+" "+document.get("calculo")+" "+document.get("sumando2")+" "+"=");
                                    resultado =  document.get("resultado").toString();
                                    timer.start();
                                }
                            }
                        } else {
                            operacion.setText(task.getException().toString());
                        }
                    }
                });
    }

    private void contadorAciertos(){
        if(newgame) {
            aciertos = 0;
            newgame = false;
        }
        if(aciertos == 5){
            dificultad = "CALCULOSN2";
            establecerNivel();
        }
        if(aciertos == 6){
            ganarjuego();
        }
        score.setText("Aciertos: "+aciertos);

    }

    private void ganarjuego() {
        timer.cancel();
        showChangeLangDialog("Has ganado\n"+getEmojiByUnicode(0x1F389)+getEmojiByUnicode(0x1F389)+getEmojiByUnicode(0x1F389), "#0099BF");
    }

    public static void guardadoPuntuacion(){

        Map<String, Object> city = new HashMap<>();
        city.put("nombre", user);
        city.put("aciertos", aciertos);
        city.put("nivel", dificultad);
        city.put("mediaDibujo", mediadibujo/6);


        db.collection("RESULTADOS").document(user)
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context.getApplicationContext(), "Datos guardados", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context.getApplicationContext(), "Ocurrio un error al guardado", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void establecerNivel(){
        if(dificultad.equals("CALCULOSN1")){
            nivel.setText("Nivel 1");
        }else if (dificultad.equals("CALCULOSN2")){
            nivel.setText("Nivel 2");
        }
    }

    public void showChangeLangDialog(String encabezadotext, String color) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.end_game, null);
        dialogBuilder.setView(dialogView);

        final EditText username = (EditText) dialogView.findViewById(R.id.username);
        final TextView encabezado=dialogView.findViewById(R.id.encabezado);
        final TextView txtaciertos=dialogView.findViewById(R.id.txtAciertos);
        final TextView txtnivel=dialogView.findViewById(R.id.txtNivel);
        txtnivel.setText("Nivel de dificultad:  "+dificultad);
        txtaciertos.setText("Aciertos:  "+String.valueOf(aciertos));

        encabezado.setText(encabezadotext);
        encabezado.setTextColor(Color.parseColor(color));

        dialogBuilder.setPositiveButton("GUARDAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                user = username.getText().toString();
                guardadoPuntuacion();
                finish();
            }
        });
        dialogBuilder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        AlertDialog b = dialogBuilder.create();
        if(!Juego_new.this.isFinishing())
        {
            b.show();
        }

    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}
