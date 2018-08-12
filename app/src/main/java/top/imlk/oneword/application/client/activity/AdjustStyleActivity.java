package top.imlk.oneword.application.client.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import top.imlk.oneword.R;
import top.imlk.oneword.bean.WordBean;
import top.imlk.oneword.bean.WordViewConfig;
import top.imlk.oneword.systemui.view.OneWordView;
import top.imlk.oneword.util.BroadcastSender;
import top.imlk.oneword.util.OneWordFileStation;

public class AdjustStyleActivity extends BaseActivity implements ColorPickerDialogListener {

    public static String ADJUST_SAMPLE_ONEWORD = "ADJUST_SAMPLE_ONEWORD";

    protected Toolbar toolbar;
    protected LinearLayout llOnewordviewContent;

    protected OneWordView oneWordView;
    protected WordBean wordBean;

    private WordViewConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getClass() != AdjustStyleActivity.class) {
            return;
        }


        setContentView(R.layout.activity_adjust_style);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        llOnewordviewContent = findViewById(R.id.ll_onewordview_content);
        oneWordView = new OneWordView(this);
        llOnewordviewContent.addView(oneWordView);


        wordBean = getIntent().getParcelableExtra(ADJUST_SAMPLE_ONEWORD);
        if (wordBean == null) {
            wordBean = new WordBean("星星只有在夜里才璀璨夺目啊。", "四月是你的谎言");
        }


        initConfig();

        notifyWordBeanChanged();
        notifyWordViewConfigChanged();

        initEditText();
        initTextSizeModule();
        initTextColorModule();
        initDisModule();
        initAlignModule();
        initItalicModule();
        initStartLineIntoModule();
        initRefAddLineModule();

        syncValue();

        initFAB();
    }

    FloatingActionButton fabDefaultConfig;
    FloatingActionButton fabSaveConfig;

    private void initFAB() {

        fabDefaultConfig = findViewById(R.id.fab_default_config);
        fabSaveConfig = findViewById(R.id.fab_save_config);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fab_default_config:
                        config = WordViewConfig.generateDefaultBean();
                        syncValue();
                        notifyWordBeanChanged();
                        Toast.makeText(AdjustStyleActivity.this, "恢复当前布局为默认配置", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.fab_save_config:
                        Toast.makeText(AdjustStyleActivity.this, "正在应用当前布局", Toast.LENGTH_SHORT).show();
                        BroadcastSender.sendUseNewWordViewConfigBroadcast(AdjustStyleActivity.this, config);
                        OneWordFileStation.saveWordViewConfigJSON(config);
                        finish();
                        break;
                }
            }
        };
        fabDefaultConfig.setOnClickListener(listener);
        fabSaveConfig.setOnClickListener(listener);
    }

    private void syncValue() {

//        tvDisL.setText(String.valueOf(config.disL));
//        tvDisT.setText(String.valueOf(config.disT));
//        tvDisR.setText(String.valueOf(config.disR));
//        tvDisB.setText(String.valueOf(config.disB));
//        tvDisC.setText(String.valueOf(config.disC));
        sbDisL.setProgress(config.disL);
        sbDisT.setProgress(config.disT);
        sbDisR.setProgress(config.disR);
        sbDisB.setProgress(config.disB);
        sbDisC.setProgress(config.disC + 50);

        cpTextColor.setColor(config.textColor);
        tvTextColor.setText("#" + (Integer.toHexString(config.textColor).toUpperCase()));

//        tvTextSize.setText(String.valueOf(config.textSize));
        sbTextSize.setProgress(config.textSize);

        sbConPos.setProgress(config.conPos);
        sbRefPos.setProgress(config.refPos);

        cbItalicRef.setChecked(config.refItalic);

        cbStartLineInto.setChecked(config.startLineInto);

        cbRefAddLine.setChecked(config.refAddLine);

        etContent.setText(wordBean.content);
        etReference.setText(wordBean.reference);
    }


    /**
     * 首行缩进
     */

    CheckBox cbRefAddLine;
    LinearLayout llRefAddLine;

    private void initRefAddLineModule() {
        cbRefAddLine = findViewById(R.id.cb_ref_add_line);
        llRefAddLine = findViewById(R.id.ll_ref_add_line);

        llRefAddLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbRefAddLine.setChecked(!cbRefAddLine.isChecked());
            }
        });

        cbRefAddLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.refAddLine = isChecked;
                notifyWordViewConfigChanged();
            }
        });
    }


    /**
     * 首行缩进
     */

    CheckBox cbStartLineInto;
    LinearLayout llStartLineInto;

    private void initStartLineIntoModule() {
        cbStartLineInto = findViewById(R.id.cb_startline_into);
        llStartLineInto = findViewById(R.id.ll_startline_into);

        llStartLineInto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbStartLineInto.setChecked(!cbStartLineInto.isChecked());
            }
        });

        cbStartLineInto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.startLineInto = isChecked;
                notifyWordViewConfigChanged();
            }
        });
    }


    /**
     * 来源斜体
     */

    CheckBox cbItalicRef;
    LinearLayout llItalicRef;

    private void initItalicModule() {
        cbItalicRef = findViewById(R.id.cb_italic_ref);
        llItalicRef = findViewById(R.id.ll_italic_ref);

        llItalicRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbItalicRef.setChecked(!cbItalicRef.isChecked());
            }
        });

        cbItalicRef.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.refItalic = isChecked;
                notifyWordViewConfigChanged();
            }
        });
    }


    /**
     * 文字对齐
     */

    private SeekBar sbConPos;
    private TextView tvConPos;
    private SeekBar sbRefPos;
    private TextView tvRefPos;

    private void initAlignModule() {
        sbConPos = findViewById(R.id.sb_con_pos);
        tvConPos = findViewById(R.id.tv_con_pos);
        sbRefPos = findViewById(R.id.sb_ref_pos);
        tvRefPos = findViewById(R.id.tv_ref_pos);

        sbConPos.setMax(2);
        sbRefPos.setMax(2);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView textView = null;

                if (seekBar == sbConPos) {
                    config.conPos = progress;
                    textView = tvConPos;
                } else if (seekBar == sbRefPos) {
                    config.refPos = progress;
                    textView = tvRefPos;
                }

                if (textView != null) {
                    switch (progress) {
                        default:
                        case 0:
                            textView.setText("靠左");
                            break;
                        case 1:
                            textView.setText("居中");
                            break;
                        case 2:
                            textView.setText("靠右");
                            break;
                    }

                    notifyWordViewConfigChanged();
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };


        sbConPos.setOnSeekBarChangeListener(listener);
        sbRefPos.setOnSeekBarChangeListener(listener);

    }


    /**
     * 边距
     */

    private SeekBar sbDisL;
    private SeekBar sbDisT;
    private SeekBar sbDisR;
    private SeekBar sbDisB;
    private SeekBar sbDisC;

    private TextView tvDisL;
    private TextView tvDisT;
    private TextView tvDisR;
    private TextView tvDisB;
    private TextView tvDisC;

    private void initDisModule() {
        sbDisL = findViewById(R.id.sb_dis_left);
        tvDisL = findViewById(R.id.tv_dis_left);

        sbDisT = findViewById(R.id.sb_dis_top);
        tvDisT = findViewById(R.id.tv_dis_top);

        sbDisR = findViewById(R.id.sb_dis_right);
        tvDisR = findViewById(R.id.tv_dis_right);

        sbDisB = findViewById(R.id.sb_dis_bottom);
        tvDisB = findViewById(R.id.tv_dis_bottom);

        sbDisC = findViewById(R.id.sb_dis_center);
        tvDisC = findViewById(R.id.tv_dis_center);

        sbDisL.setMax(200);
        sbDisT.setMax(200);
        sbDisR.setMax(200);
        sbDisB.setMax(200);
        sbDisC.setMax(200);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == sbDisL) {
                    config.disL = progress;
                    tvDisL.setText(String.valueOf(config.disL));
                    notifyWordViewConfigChanged();
                } else if (seekBar == sbDisT) {
                    config.disT = progress;
                    tvDisT.setText(String.valueOf(config.disT));
                    notifyWordViewConfigChanged();
                } else if (seekBar == sbDisR) {
                    config.disR = progress;
                    tvDisR.setText(String.valueOf(config.disR));
                    notifyWordViewConfigChanged();
                } else if (seekBar == sbDisB) {
                    config.disB = progress;
                    tvDisB.setText(String.valueOf(config.disB));
                    notifyWordViewConfigChanged();
                } else if (seekBar == sbDisC) {
                    config.disC = progress - 50;
                    tvDisC.setText(String.valueOf(config.disC));
                    notifyWordViewConfigChanged();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };


        sbDisL.setOnSeekBarChangeListener(listener);
        sbDisT.setOnSeekBarChangeListener(listener);
        sbDisR.setOnSeekBarChangeListener(listener);
        sbDisB.setOnSeekBarChangeListener(listener);
        sbDisC.setOnSeekBarChangeListener(listener);

    }


    /**
     * TextColor
     */

    private LinearLayout llTextColor;
    private ColorPanelView cpTextColor;
    private TextView tvTextColor;

    private void initTextColorModule() {
        llTextColor = findViewById(R.id.ll_text_color);
        cpTextColor = findViewById(R.id.cp_text_color);
        tvTextColor = findViewById(R.id.tv_text_color);

        llTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setDialogType(ColorPickerDialog.TYPE_PRESETS).setShowAlphaSlider(true).setColor(config.textColor).show(AdjustStyleActivity.this);
            }
        });

    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        cpTextColor.setColor(color);
        tvTextColor.setText("#" + (Integer.toHexString(color).toUpperCase()));
        config.textColor = color;
        notifyWordViewConfigChanged();
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    /**
     * TextSize
     */
    private SeekBar sbTextSize;
    private TextView tvTextSize;

    private void initTextSizeModule() {
        sbTextSize = findViewById(R.id.sb_text_size);
        tvTextSize = findViewById(R.id.tv_text_size);


        sbTextSize.setMax(49);
        sbTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.textSize = progress;
                tvTextSize.setText(String.valueOf(progress));
                notifyWordViewConfigChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void notifyWordBeanChanged() {
        oneWordView.setOneWord(wordBean);
    }

    protected void notifyWordViewConfigChanged() {
        oneWordView.applyWordViewConfig(config);
    }

    /**
     * EditText
     */
    private EditText etContent;
    private EditText etReference;

    protected void initEditText() {
        etContent = findViewById(R.id.et_content);
        etReference = findViewById(R.id.et_reference);

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                wordBean.content = s.toString();
                notifyWordBeanChanged();
            }
        });

        etReference.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                wordBean.reference = s.toString();
                notifyWordBeanChanged();
            }
        });

    }


    public void initConfig() {
        config = OneWordFileStation.readWordViewConfigJSON();
        if (config == null) {
            config = WordViewConfig.generateDefaultBean();
        }

    }


    @Override
    public void onBackPressed() {
        alertDoesNotSave();
    }

}