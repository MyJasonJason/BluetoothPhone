package com.autochips.bluetooth;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ATC6026 on 2018/3/31.
 */

public class myDialog extends Dialog {

    public myDialog(Context context) {
        super(context);
    }

    public myDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        public final static int DIALOG_TWO_BUTTON = 0;
        public final static int DIALOG_THREE_BUTTON = 1;

        private Context mContext;
        private int mTheme;
        private View mLayout;
        private String mBTName;
        private String mBTPincode;
        myDialog sDialog;

        private LayoutInflater mInflater;
        private DialogInterface.OnClickListener mPositiveButtonClickListener;
        private DialogInterface.OnClickListener mNegativeButtonClickListener;
        private DialogInterface.OnClickListener mNeutralButtonClickListener;
        private Button btnPositive;
        private Button btnNegative;
        private Button btnNeutral;
        private View mContentView;
        private String mTextPositve;

        public void dismiss() {
            sDialog.dismiss();
        }

        public Builder setBTName(String name) {
            mBTName = name;
            return this;
        }

        public Builder setBTPincode(String pincode) {
            mBTPincode = pincode;
            return this;
        }

        public Builder(Context context, int theme) {
            this.mContext = context;
            this.mTheme = theme;
        }

        public Builder setPositiveButton(DialogInterface.OnClickListener listener) {
            this.mPositiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(DialogInterface.OnClickListener listener) {
            this.mNegativeButtonClickListener = listener;
            return this;
        }

        public Builder setNeutralButton(DialogInterface.OnClickListener listener) {
            this.mNeutralButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButtonText(String positiveButtonText) {
            mTextPositve = positiveButtonText;
            return this;
        }

        public Builder setContentView(View v) {
            this.mContentView = v;
            return this;
        }

        public myDialog create() {
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (mTheme) {
                case DIALOG_TWO_BUTTON:
                    dialogWithTwoBtn();
                    break;

                case DIALOG_THREE_BUTTON:
                    dialogWithThreeBtn();
                    break;
            }
            sDialog.setContentView(mLayout);
            return sDialog;
        }

        public void dialogWithTwoBtn() {
            sDialog = new myDialog(mContext, R.style.Dialog_translucent);
            mLayout = mInflater.inflate(R.layout.pairdialog, null);
            sDialog.addContentView(mLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            sDialog.setCanceledOnTouchOutside(false);

            if (mBTName != null) {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_name)).setText(mBTName);
            } else {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_name)).setText(R.string.bluetooth_name);
            }

            if (mBTPincode != null) {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_pincode)).setText(mBTPincode);
            } else {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_pincode)).setText(R.string.bluetooth_pincode);
            }

            if (mPositiveButtonClickListener != null) {
                btnPositive = (Button)mLayout.findViewById(R.id.btn_bluetooth_pair);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mPositiveButtonClickListener.onClick(sDialog, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }


            if (mNegativeButtonClickListener != null) {
                btnNegative = (Button) mLayout.findViewById(R.id.btn_bluetooth_pair_cancel);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mNegativeButtonClickListener.onClick(sDialog, DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
        }

        public void dialogWithThreeBtn() {
            sDialog = new myDialog(mContext, R.style.Dialog_translucent);
            mLayout = mInflater.inflate(R.layout.pairdialog, null);
            sDialog.addContentView(mLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            sDialog.setCanceledOnTouchOutside(false);

            if (mBTName != null) {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_name)).setText(mBTName);
            } else {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_name)).setText(R.string.bluetooth_name);
            }

            if (mBTPincode != null) {
                ((TextView)mLayout.findViewById(R.id.text_dialog_bluetooth_pincode)).setText(mBTPincode);
            }

            if (mTextPositve == null) {
                mTextPositve = "连接";
            }

            if (mPositiveButtonClickListener != null) {
                btnPositive = (Button)mLayout.findViewById(R.id.btn_bluetooth_pair);
                btnPositive.setText(mTextPositve);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mPositiveButtonClickListener.onClick(sDialog, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }

            if (mNeutralButtonClickListener != null) {
                btnNeutral = (Button)mLayout.findViewById(R.id.btn_bluetooth_pair_delete);
                btnNeutral.setVisibility(View.VISIBLE);
                btnNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNeutralButtonClickListener.onClick(sDialog, DialogInterface.BUTTON_NEUTRAL);
                    }
                });
            }

            if (mNegativeButtonClickListener != null) {
                btnNegative = (Button) mLayout.findViewById(R.id.btn_bluetooth_pair_cancel);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mNegativeButtonClickListener.onClick(sDialog, DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
        }
    }
}
