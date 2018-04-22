package com.example.xp.buhardillascrew;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

import static android.support.v4.content.FileProvider.getUriForFile;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout principal = (LinearLayout) findViewById(R.id.botones);

        int numeroLinea = 0;
        LinearLayout auxiliar = creaLineaBotones(numeroLinea);
        principal.addView(auxiliar);

        Field[] listaCanciones = R.raw.class.getFields();
        int columnas = 4;
        for (int i = 0; i < listaCanciones.length; i++) {
            //creamos un botón por código y lo añadimos a la pantalla principal
            Button b = creaBoton(i, listaCanciones);
            //añadimos el botón al layout
            auxiliar.addView(b);
            if (i % columnas == columnas - 1) {
                auxiliar = creaLineaBotones(i);
                principal.addView(auxiliar);
            }
        }


    }

    private static final String SHARED_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".myfileprovider";
    private static final String SHARED_FOLDER = "shared";
    MediaPlayer m = new MediaPlayer();

    public void sonidoCopiar(View view) throws IOException {
        Button b = (Button) findViewById(view.getId());
        String nombre = b.getText().toString();
        String extension = ".mp3";
        String tipo = "audio/mpeg";
        if (nombre.substring(0, 2).contains("v_")) {
            extension = ".mp4";
            tipo = "video/mp4";
        }
        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier(nombre,
                        "raw", getPackageName()));

        final File sharedFolder = new File(getFilesDir(), SHARED_FOLDER);
        sharedFolder.mkdirs();

        final File sharedFile = File.createTempFile(nombre, extension, sharedFolder);
        sharedFile.createNewFile();

        copyInputStreamToFile(ins, sharedFile);
        final Uri uri = FileProvider.getUriForFile(this, SHARED_PROVIDER_AUTHORITY, sharedFile);
        final ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                .setType(tipo)
                .addStream(uri);
        final Intent chooserIntent = intentBuilder.createChooserIntent();
        startActivity(chooserIntent);
    }


    public void sonido(View view) {
        //Log.i("etiqueta: ", findViewById(view.getId()).getTag().toString());
        Button b = (Button) findViewById(view.getId());
        String nombre = b.getText().toString();

        m = MediaPlayer.create(this, (int) findViewById(view.getId()).getTag());
        m.start();
        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
            }
        });
    }

    private LinearLayout creaLineaBotones(int numeroLinea) {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        parametros.weight = 1;
        LinearLayout linea = new LinearLayout(this);

        linea.setOrientation(LinearLayout.HORIZONTAL);
        linea.setLayoutParams(parametros);
        linea.setId(numeroLinea);
        return linea;
    }

    private Button creaBoton(int i, Field[] _listaCanciones) {
        LinearLayout.LayoutParams parametrosBotones = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        parametrosBotones.weight = 1;
        parametrosBotones.setMargins(5, 5, 5, 5);
        parametrosBotones.gravity = Gravity.CENTER_HORIZONTAL;
        Button b = new Button(this);
        b.setLayoutParams(parametrosBotones);
        b.setText(_listaCanciones[i].getName());
        b.setTextSize(10);
        b.setTextColor(Color.BLACK);
        b.setBackgroundColor(Color.WHITE);
        b.setAllCaps(true); //todas las letras del botón en minúscula
        int id = this.getResources().getIdentifier(_listaCanciones[i].getName(), "raw", this.getPackageName());
        b.setTag(id);

        b.setId(i + 50);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sonido(view);
            }
        });

        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    sonidoCopiar(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        return b;
    }

    // Copy an InputStream to a File.
    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if (out != null) {
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}