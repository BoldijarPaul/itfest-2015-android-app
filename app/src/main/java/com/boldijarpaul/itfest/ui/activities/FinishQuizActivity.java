package com.boldijarpaul.itfest.ui.activities;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boldijarpaul.itfest.R;
import com.boldijarpaul.itfest.api.ApiModule;
import com.boldijarpaul.itfest.data.models.Answer;
import com.boldijarpaul.itfest.data.models.Quiz;
import com.boldijarpaul.itfest.presenter.presenters.AddAnswerPresenter;
import com.boldijarpaul.itfest.presenter.views.AddAnswerView;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FinishQuizActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, AddAnswerView {


    public static final String KEY_CHOOSEN_URL = "CHOSEURL";
    public static final String KEY_QUIZ = "QUIZKEY";
    @Bind(R.id.activity_finish_quiz_message)
    TextView mMessage;
    @Bind(R.id.activity_finish_quiz_image)
    ImageView mImage;


    private String mChoosenUrl;
    private Quiz mQuiz;
    private TextToSpeech mTextToSpeech;

    private AddAnswerPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_quiz);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        mTextToSpeech = new TextToSpeech(this, this);

        mPresenter = new AddAnswerPresenter(this, this);
        mChoosenUrl = getIntent().getStringExtra(KEY_CHOOSEN_URL);
        mQuiz = (Quiz) getIntent().getSerializableExtra(KEY_QUIZ);

        setUpViews();

        sendAnswerToServer();

    }


    private void sendAnswerToServer() {
        Answer answer = new Answer();
        answer.quizId = mQuiz.id;
        answer.success = goodAnswer();
        answer.deviceId = getUniquePsuedoID();

        mPresenter.addAnswer(answer);
    }

    public static String getUniquePsuedoID() {
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            serial = "serial"; // some value
        }
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    private void setUpViews() {
        if (goodAnswer()) setUpGoodAnswerViews();
        else setUpWrongAnswerViews();
    }

    private void setUpWrongAnswerViews() {
        mMessage.setText(getWrongAnswerMessage());
        Picasso.with(this)
                .load(mQuiz.answer1.replace("localhost", ApiModule.IP))
                .into(mImage);

    }

    private void setUpGoodAnswerViews() {
        mMessage.setText(getString(R.string.msg_congratulation_right));
        mImage.setImageResource(R.drawable.gold_blue);
        mTextToSpeech.speak(getString(R.string.msg_congratulation_right), TextToSpeech.QUEUE_FLUSH, null);

    }

    private String getWrongAnswerMessage() {
        String result = "";
        result += getString(R.string.msg_wrong);
        return result;
    }


    private boolean goodAnswer() {
        return mQuiz.answer1.equals(mChoosenUrl);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTextToSpeech.setLanguage(Locale.US);
            mTextToSpeech.setSpeechRate(0.6f);
            mTextToSpeech.speak(getString(R.string.msg_wrong_play), TextToSpeech.QUEUE_FLUSH, null);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {
        Toast.makeText(getApplicationContext(), "Internet error", Toast.LENGTH_SHORT).show();
    }
}
