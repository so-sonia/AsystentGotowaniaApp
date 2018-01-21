package com.example.sonia.asystentgotowania.onerecipe.questions;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.sonia.asystentgotowania.Constants;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QuestionsHandler {
    private static final String TAG = Constants.APP_TAG.concat(QuestionsHandler.class.getSimpleName());
    String mIngredients;
    String mInstruction;
    String mQuestion;
    TextToSpeech mTextToSpeech;
    Context mContext;


    public QuestionsHandler(Context context, String ingredients, String instruction) {
        mContext = context;
        mIngredients = ingredients.toLowerCase();
        mInstruction = instruction.toLowerCase();
        mTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(new Locale("pl_PL"));
                }
            }
        });

    }

    public void ask(String question) {
        mQuestion = question.toLowerCase();
        String answer;
        //ile składnika, czy jest składnik, jaka temperatura, ile porcji, osób, jak długo gotować, piec, itd.
        //w jakiej temperaturze (temp., temperatura, stopni st.
        //ile czasu, jak długo liczbah h (ale czasem bez spacji)
        Pattern p = Pattern.compile("^.*ile.*(porcji|osób|wyjdzie).*$");
        Matcher m = p.matcher(mQuestion);
        if (m.find()) {
            answer = portionHandler();
        } else {
            p = Pattern.compile("^.*((długo|ile).*(gotow|gotuj|piec|smaż| czek|du(s|ś)|maryn|(zo|od)staw))|czas.*$");
            m = p.matcher(mQuestion);

            if (m.find()) {
                answer = timeHandler();
            } else {
                p = Pattern.compile("^.*temperat|stopni[^o]|nastaw.*$");
                m = p.matcher(mQuestion);

                if (m.find()) {
                    answer = temperatureHandler();
                } else {
                    p = Pattern.compile("^.*(ile|dużo).*$");
                    m = p.matcher(mQuestion);

                    if (m.find()) {
                        answer = ingredientsHandler();
                    } else {
                        p = Pattern.compile("^.*(czy|jest|potrzeb).*$");
                        m = p.matcher(mQuestion);

                        if (m.find()) {
                            answer = ingredientsHandler();
                        } else {
                            answer = noUnderstandHandler();
                        }
                    }
                }
            }
        }
        mTextToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);

        Log.d(TAG, answer);
    }

    private String portionHandler() {
        Log.d(TAG, "portion handler");
        String content = mIngredients + " " + mInstruction;
        content = content.replaceAll("(st|ok|temp)\\.", "$1");
        Pattern p = Pattern.compile("[^.\\n]*(porcj|osób)[^.\\n]*");
        Matcher m = p.matcher(content);
        String answer;
        if (m.find()) {
            answer = m.group(); // or group(0)
        } else {
            String question = mQuestion;
            if (mQuestion.indexOf("ile")<0 & mQuestion.indexOf("czy")<0) {
                question = "czy " + mQuestion;
            }
            answer = notKnowHandler(question);
        }
        return answer;
    }

    private String timeHandler(){
        Log.d(TAG, "time handler");
        String answer="";
        String content = mIngredients + " " + mInstruction;
        content = content.replaceAll("(st|ok|temp)\\.", "$1");
        Pattern p = Pattern.compile("(gotow|gotuj|piec|smaż|czek|du(s|ś)|maryn|(zo|od)staw|piek)");
        Matcher m = p.matcher(mQuestion);
        if (m.find()) {
            String keyword = m.group(1); //m.group(0)
            if (keyword.equals("piec")| keyword.equals("piek")) {
                keyword = "(piec|piek)";
            } else if (keyword.equals("gotow")| keyword.equals("gotuj")) {
                keyword = "(gotow|gotuj)";
            } else if (keyword.equals("zostaw")| keyword.equals("odstaw")|keyword.equals("czek")) {
                keyword = "(zostaw|odstaw|czek)";
            } else if (keyword.equals("smaż")|keyword.contains("du")) {
                keyword = "(smaż|du(s|ś))";
            }
            Log.d(TAG, "keyword = " + keyword);
            p = Pattern.compile("[^.\\n]*((" + keyword + "[^.\\n]*(godzin|minut|\\b\\d*h\\b|\\b\\d*min\\b))|(" +
                    "(godzin|minut|\\b\\d*h\\b|\\b\\d*min\\b)[^.\\n]*"
                + keyword + "))[^.\\n]*");
            m = p.matcher(content);

            while (m.find()) {
                answer += m.group() + ". ";
                Log.d(TAG, m.group());
            }
        }
        if ("".equals(answer)){
            p = Pattern.compile("[^.\\n]*(godzin|minut|\\b\\d*h\\b|\\b\\d*min\\b)[^.\\n]*");
            m = p.matcher(content);

            while (m.find()) {
                answer += m.group() + ". ";
                Log.d(TAG, m.group());
            }
        }
        if ("".equals(answer)){
            String question = mQuestion;
            if (mQuestion.indexOf("ile")<0 & mQuestion.indexOf("jak")<0) {
                question = "jak " + mQuestion;
            }
            answer = notKnowHandler(question);
        }
        return answer;
    }

    private String temperatureHandler(){
        Log.d(TAG, "temperature handler");
        String answer = "";
        String content =  mInstruction;
        content = content.replaceAll("(st|ok|temp)\\.", "$1");
        Pattern p = Pattern.compile("[^.\\n]*(temperat|stopni[^o]|nastaw|\\btemp\\b|\\b\\d*st\\b|\\b\\d*c ?\\b)[^.\\n]*");
        Matcher m = p.matcher(content);
        while (m.find()) {
            answer += m.group() + ". ";
            Log.d(TAG, m.group());
        }
        if ("".equals(answer)){
            answer = "nie wiem jaka powinna być temperatura";
        }
        return answer;
    }

    private String ingredientsHandler(){
        Log.d(TAG, "ingredients handler");
        String answer = "";
        String content = mIngredients;
        String questionWords = mQuestion.replaceAll("\\W{1,}", " ")
                .replaceAll("\\b.*?(\\bgr\\b|gram|litr|potrzeb|ile|łyże|szklan|jak|" +
                        "dużo|jest|będzie|\\b(mi|nam|się|czy)\\b|przyda).*?\\b", "")
                .replaceAll("^\\s{1,}|\\s{1,}$", "")
                .replaceAll("\\s{1,}", "|");
        Log.d(TAG, "questionWords = " + questionWords);
        if (!"".equals(questionWords)){
            Pattern p = Pattern.compile("[^.\\n]*(" + questionWords + ")[^.\\n]*");
            Matcher m = p.matcher(content);
            while (m.find()) {
                answer += m.group() + ". ";
                Log.d(TAG, m.group());
            }
        }
        if ("".equals(answer)){
            String question = mQuestion;
            if (mQuestion.indexOf("ile")<0 & mQuestion.indexOf("jak")<0 & mQuestion.indexOf("czy")<0) {
                question = "czy " + mQuestion;
            }
            answer = notKnowHandler(question);
        }
        return answer;
    }

//    private String ingredientsHandler(){
//        String content = mIngredients;
//        content = content.replaceAll("(st|ok|temp)\\.", "$1");
//        String answer = "";
//        if ("".equals(answer)){
//            String question = mQuestion;
//            if (mQuestion.indexOf("ile")<0 & mQuestion.indexOf("jak")<0) {
//                question = "jak " + mQuestion;
//            }
//            answer = notKnowHandler(question);
//        }
//        return answer;
//    }

    private String notKnowHandler(String question){
    return("nie wiem " + question);
    }

    private String noUnderstandHandler(){
        return("przepraszam, nie rozumiem pytania");
    }

}

