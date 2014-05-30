package edu.dartmouth.cs.dtutor;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class DtutorDialogFragment extends DialogFragment {

    private static final String TAG = Globals.TAG + ".DtutorDialogFragment";

    /**
     * Keys
     */
    public static final String DIALOG_ID_KEY = "dialog-id";
    public static final String DIALOG_MESSAGE_KEY = "dialog-message";

    /**
     * Dialogs
     */
    public static final int DIALOG_ID_BASIC_MESSAGE = 1;
    public static final String DIALOG_ID_BASIC_MESSAGE_TAG = "basic-message";

    /**
     * This should be the primary method that any callers have to interact with. displayDialog will get a new
     * instance of DtutorDialogFragment and display the dialog associated with 'id'.
     * 
     * @param fragManager FragmentManager for a particular view/context
     * @param id identifier for dialog to show
     * @param tag for this fragment
     */
    public static void displayDialog(FragmentManager fragManager, int id, String tag, String message) {
        DialogFragment dfragment = DtutorDialogFragment.newInstance(id, message);
        if(dfragment != null) {
            Log.d(TAG, "displayDialog() [id: " + String.valueOf(id) + "; tag: " + tag + "]");
            dfragment.show(fragManager, tag);
        } else {
            Log.e(TAG, "unrecognized dialog id " + "(" + id + ")" + " - unable to build dialog");
        }
    }

    /**
     * Create a new instance of the DtutorDialogFragment
     * 
     * @param dialogId ID of desired dialog to show
     * @return requested DialogFragment
     */
    public static DtutorDialogFragment newInstance(int dialogId, String message) {
        DtutorDialogFragment frag = new DtutorDialogFragment();

        // Set dialog's ID
        Bundle args = new Bundle();
        args.putInt(DIALOG_ID_KEY, dialogId);
        args.putString(DIALOG_MESSAGE_KEY, message);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Set Activity that the dialog is contained within
        final Activity parent = getActivity();

        // Requested dialog id
        int dialogId = getArguments().getInt(DIALOG_ID_KEY);

        // Requested dialog message
        String dialogMessage = getArguments().getString(DIALOG_MESSAGE_KEY);

        // Configure dialog(s)
        switch (dialogId) {
        case DIALOG_ID_BASIC_MESSAGE:
            Dialog dialog = new Dialog(parent);

            // Full-screen option
            final Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

            // Dialog background
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Dialog contents
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            TextView view = new TextView(parent);
            view.setText(dialogMessage);
            view.setGravity(Gravity.CENTER);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            view.setTextColor(Color.WHITE);
            view.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
            dialog.setContentView(view);

            return dialog;
        default:
            return null;
        }
    }

}