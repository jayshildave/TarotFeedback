package com.apex.tarot.feedback.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Custom implementation of EditText to override the inbuilt Android
 * functionality of overriding imeOption "Done" when input-type is
 * selected as "textMultiLine"
 *
 * @author Jayshil Dave
 */
@SuppressWarnings("NullableProblems")
public class CommentEditText extends EditText {

    public CommentEditText(Context context) {
        super(context);

    }

    public CommentEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * Creating a new InputConnection for an InputMethod to interact with the view.
     * @param outAttrs - Fill in with attribute information about the connection
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {



        // getting inputconnection
        InputConnection connection = super.onCreateInputConnection(outAttrs);


        int imeActions = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;


        // checking for actions
        if ((imeActions & EditorInfo.IME_ACTION_DONE) != 0) {

            // clear the existing action
            outAttrs.imeOptions ^= imeActions;
            // set the DONE action
            outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
        }

        // checking for enter action from keyboard by user
        if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {

            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }
        return connection;
    }
}
