package maxdep.robotino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class control_robot extends AppCompatActivity {
    String value_ordre;
    int vitesse;
    String address_device = null;
    BluetoothAdapter myBluetooth = null;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    BluetoothSocket btSocket = null;
    Handler myHandler = new Handler();
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_robot);
        Intent newint = getIntent();
        address_device = newint.getStringExtra(Choix_Bluetooth.EXTRA_ADDRESS); //addresse bluetooth
        final EditText terminal = (EditText) findViewById(R.id.input_msg_test); //Mini console du mode test pour l'envoie de msg spécifique
        final TextView txt_term = (TextView) findViewById(R.id.text_command); //Texte coder de la console
        final Switch bt_test = (Switch) findViewById(R.id.switch_modeTest); //Switch pour passer en mode test
        Button btn_deco = (Button) findViewById(R.id.button_deco); //Button de déconnexion
        Button bt_avant = (Button) findViewById(R.id.bt_avant); //Button pour faire avancer le robot
        Button bt_arriere = (Button) findViewById(R.id.bt_arrire); //Button pour faire reculer le robot
        Button bt_stop = (Button) findViewById(R.id.bt_stop); //Button pour arreter le robot
        final TextView value_progress = (TextView) findViewById(R.id.textView2); //Affichage de la vitesse
        SeekBar progress_value = (SeekBar) findViewById(R.id.seekBar); //Barre de progression de la vitesse
        new ConnectBT().execute(); //Connection du bluetooth
        bt_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt_test.isChecked()) {
                    findViewById(R.id.txt_input).setEnabled(true);
                    findViewById(R.id.txt_input).setVisibility(View.VISIBLE);
                    terminal.setEnabled(true);
                    terminal.setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.txt_input).setEnabled(false);
                    findViewById(R.id.txt_input).setVisibility(View.INVISIBLE);
                    terminal.setEnabled(false);
                    terminal.setVisibility(View.INVISIBLE);
                }
            }
        });
        progress_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_progress.setText("Vitesse : " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                vitesse = seekBar.getProgress();
            }
        });
        btn_deco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
        bt_avant.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                myHandler.postDelayed(motor_2, 0);
            }
        });
        bt_arriere.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                myHandler.postDelayed(motor_1, 0);
                myHandler.postDelayed(motor_2, 2000);
            }
        });
        bt_stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                send_msg_bt("1m0000" + vitesse);
            }
        });
        terminal.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        String ordre_txt = terminal.getText().toString();
                        value_ordre =  ordre_txt;
                        send_msg_bt(value_ordre);
                        txt_term.setText("$: " + value_ordre.getBytes());
                        terminal.setText("");
                        msg("Ordre envoyé");
                    }
                    return true;
                }
                return false;
            }
        });
    }
    private Runnable motor_1 = new Runnable()
    {
        @Override
        public void run()
        {
            if(vitesse < 100 && vitesse >= 10){
                send_msg_bt("1m10" + vitesse);
            }else if(vitesse < 10){
                send_msg_bt("1m100" + vitesse);
            }else {
                send_msg_bt("1m1" + vitesse);
            }
        }
    };
    private Runnable motor_2 = new Runnable()
    {
        @Override
        public void run() {
            if(vitesse < 100 && vitesse >= 10){
                send_msg_bt("1m20" + vitesse);
            }else if(vitesse < 10){
                send_msg_bt("1m200" + vitesse);
            }else {
                send_msg_bt("1m2" + vitesse);
            }
        }
    };
    private void Disconnect()
    {
        if (btSocket!=null) {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish();
    }
    private void send_msg_bt(String msg)
    {
        if (btSocket!=null) {
            try
            {
                btSocket.getOutputStream().write(msg.getBytes());
                msg(msg);
            }
            catch (IOException e)
            { msg("Error, message : " + msg);}
        }
    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(control_robot.this, "Connection...", "En cours de traitement");
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address_device);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("La connexion a échoué. Est-ce un Bluetooth SPP? Réessayer.");
                //finish();
            }
            else
            {
                msg("Connecté.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
