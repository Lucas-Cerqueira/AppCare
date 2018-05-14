package com.lf.appcare;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;


//NADA AQUI FUNCIONA, IGNORAR POR ENQUANTO
public class EmergencyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        KeyEvent ke = (KeyEvent)intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
        if (ke .getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            System.out.println("I got volume up event");
        }else if (ke .getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            System.out.println("I got volume key down event");
        }

    }        // TODO: This method is called when the BroadcastReceiver is receiving

}
