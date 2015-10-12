package com.stayfprod.utter.ui.activity;


import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.stayfprod.utter.App;
import com.stayfprod.utter.Constant;
import com.stayfprod.utter.R;
import com.stayfprod.utter.service.AudioPlayer;
import com.stayfprod.utter.ui.view.VisualizerView;
import com.stayfprod.utter.util.AndroidUtil;

import java.util.ArrayList;

public class EqualizerActivity extends AbstractActivity {

    private static final String LOG = EqualizerActivity.class.getSimpleName();

    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private Toolbar mToolbar;
    private Spinner mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isBadAppContext(this))
            return;

        setContentView(R.layout.activity_equalizer);

        try {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            setToolbar();

            RelativeLayout mainLayout = (RelativeLayout) mToolbar.getParent();

            mLinearLayout = new LinearLayout(this);
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);

            mainLayout.addView(mLinearLayout);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLinearLayout.getLayoutParams();
            layoutParams.leftMargin = Constant.DP_18;
            layoutParams.rightMargin = Constant.DP_18;
            layoutParams.topMargin = Constant.DP_18;
            layoutParams.addRule(RelativeLayout.BELOW, mToolbar.getId());

            setupVisualizerFxAndUI();
            setupEqualizerFxAndUI();

            mSpinner = (Spinner) findViewById(R.id.a_equalizer_spinner);
            mSpinner.setBackgroundColor(0xFF5B95C2);

            ArrayList<String> items = new ArrayList<String>();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setTypeface(AndroidUtil.TF_ROBOTO_MEDIUM);
                    view.setTextColor(0xffffffff);
                    view.setTextSize(20);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    view.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                    view.setTextColor(0xFF212121);
                    view.setTextSize(20);
                    view.setBackgroundResource(R.drawable.item_click_white);
                    return view;
                }
            };

            AudioPlayer audioPlayer = AudioPlayer.getPlayer();
            Equalizer equalizer = audioPlayer.getEqualizer();

            for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
                items.add(equalizer.getPresetName(i));
            }

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    AudioPlayer audioPlayer = AudioPlayer.getPlayer();
                    Equalizer equalizer = audioPlayer.getEqualizer();
                    try {
                        if (audioPlayer.getMediaPlayer() != null && equalizer != null) {
                            equalizer.usePreset((short) position);
                            audioPlayer.setEqualizerPreset((short) position);
                            short bands = equalizer.getNumberOfBands();
                            short lowerBandLvl = equalizer.getBandLevelRange()[0];
                            for (short i = 0; i < bands; i++) {
                                short band = i;
                                SeekBar seekBar = (SeekBar) findViewById(band);
                                seekBar.setProgress(equalizer.getBandLevel(band) - lowerBandLvl);
                            }
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mSpinner.setSelection(audioPlayer.getEqualizerPreset());
        } catch (Exception e) {
            AndroidUtil.showToastShort(e.getMessage());
        }
    }

    private void setupEqualizerFxAndUI() {

        AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        if (audioPlayer.getMediaPlayer() != null && audioPlayer.getEqualizer() != null) {
            final Equalizer equalizer = audioPlayer.getEqualizer();
            short bands = equalizer.getNumberOfBands();

            final short minEQLevel = equalizer.getBandLevelRange()[0];
            final short maxEQLevel = equalizer.getBandLevelRange()[1];

            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + AndroidUtil.getResourceString(R.string.db));
            minDbTextView.setTextSize(16);
            minDbTextView.setTextColor(0xFF222222);
            minDbTextView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);

            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + AndroidUtil.getResourceString(R.string.db));
            maxDbTextView.setTextSize(16);
            maxDbTextView.setTextColor(0xFF222222);
            maxDbTextView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);

            RelativeLayout topRow = new RelativeLayout(this);
            topRow.addView(minDbTextView);
            topRow.addView(maxDbTextView);
            mLinearLayout.addView(topRow);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) maxDbTextView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (short i = 0; i < bands; i++) {
                final short band = i;

                TextView freqTextView = new TextView(this);
                freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                freqTextView.setText((equalizer.getCenterFreq(band) / 1000) + AndroidUtil.getResourceString(R.string.hz));
                freqTextView.setTextSize(16);
                freqTextView.setTextColor(0xFF222222);
                freqTextView.setTypeface(AndroidUtil.TF_ROBOTO_REGULAR);
                mLinearLayout.addView(freqTextView);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;

                SeekBar bar = (SeekBar) layoutInflater.inflate(R.layout.seekbar, mLinearLayout, false);
                bar.setId(i);
                bar.setLayoutParams(layoutParams);
                bar.setMax(maxEQLevel - minEQLevel);
                //bar.setProgress(mEqualizer.getBandLevel(band));

                bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        try {
                            equalizer.setBandLevel(band, (short) (progress + minEQLevel));
                        } catch (Exception e) {
                            //
                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                row.addView(bar);
                mLinearLayout.addView(row);
            }
        }
    }

    private void setupVisualizerFxAndUI() {
        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Constant.DP_54));
        mLinearLayout.addView(mVisualizerView);

        AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        if (audioPlayer.getMediaPlayer() != null) {
            Visualizer mVisualizer = new Visualizer(audioPlayer.getMediaPlayer().getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    mVisualizerView.updateVisualizer(bytes);
                }

                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {

                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            audioPlayer.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.a_action_bar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setNavigationIcon(R.mipmap.ic_back);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();
        if (audioPlayer.getVisualizer() != null) {
            audioPlayer.getVisualizer().setEnabled(false);
            audioPlayer.getVisualizer().release();
            audioPlayer.setVisualizer(null);
        }
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    protected void onDestroy() {
        AudioPlayer audioPlayer = AudioPlayer.getPlayer();

        if (audioPlayer.getVisualizer() != null) {
            audioPlayer.getVisualizer().setEnabled(false);
            audioPlayer.getVisualizer().release();
            audioPlayer.setVisualizer(null);
        }
        super.onDestroy();
    }
}
